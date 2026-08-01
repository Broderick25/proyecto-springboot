package com.SpringBoot.infrastructure.audit;

import com.SpringBoot.domain.document.AuditLog;
import com.SpringBoot.domain.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Auditoría de comandos y queries: envuelve cada {@code CommandHandler.handle(...)} y
 * {@code QueryHandler.handle(...)} y registra un {@link AuditLog} con el resultado real (éxito
 * o falla), a diferencia del enfoque anterior basado en
 * {@code @TransactionalEventListener(AFTER_COMMIT)} sobre eventos de dominio, que solo podía ver
 * comandos que llegaban a publicar un evento — un fallo de validación (ej.
 * {@code ProductNotFoundException}, que ocurre antes de que el agregado registre cualquier
 * evento) nunca quedaba auditado.
 *
 * <p>Se decidió auditar también las queries (lecturas), no solo comandos — el {@code className}
 * ya distingue el tipo (sufijo {@code CommandHandler} vs {@code QueryHandler}), así que no hace
 * falta un campo nuevo en {@link AuditLog}.
 *
 * <p>{@code @Order(0)}, no {@code HIGHEST_PRECEDENCE}: necesita quedar por FUERA del advice de
 * {@code @Transactional} (que sigue en su order por defecto, {@code LOWEST_PRECEDENCE}) para
 * que el resultado auditado en los Command sea el de la transacción ya confirmada/revertida —
 * pero por DENTRO del advice de {@code @Cacheable} (ver {@code CacheConfig}, order {@code -100}),
 * para que un cache hit en una Query no dispare una escritura de auditoría (el interceptor de
 * caché corta la llamada antes de llegar a este aspecto).
 *
 * <p>No registra {@code userId} — el proyecto no tiene todavía ningún mecanismo de identidad de
 * quien ejecuta el comando/query (no hay Spring Security ni un actor propagado).
 *
 * <p>Solo audita la llamada más externa por hilo: algunos handlers invocan a otros directamente
 * (ej. {@code ConfirmOrderCommandHandler} llama a {@code DecrementProductStockCommandHandler}
 * por cada línea de la orden) — sin esta guarda, una sola acción de usuario generaría N+1 filas
 * de auditoría (una por comando anidado) en vez de una sola fila representando la operación
 * completa.
 */
@Aspect
@Component
@Order(0)
public class HandlerAuditAspect {

    private static final ThreadLocal<Boolean> AUDITING_IN_PROGRESS = ThreadLocal.withInitial(() -> false);

    private final AuditLogRepository auditLogRepository;

    public HandlerAuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Around("execution(* com.SpringBoot.application.command.CommandHandler+.handle(..)) "
            + "|| execution(* com.SpringBoot.application.query.QueryHandler+.handle(..))")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        if (AUDITING_IN_PROGRESS.get()) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long startMillis = System.currentTimeMillis();

        AUDITING_IN_PROGRESS.set(true);
        try {
            Object result = joinPoint.proceed();
            save(className, methodName, startMillis, true, null);
            return result;
        } catch (Throwable ex) {
            save(className, methodName, startMillis, false, ex.getMessage());
            throw ex;
        } finally {
            AUDITING_IN_PROGRESS.remove();
        }
    }

    private void save(String className, String methodName, long startMillis, boolean success, String errorMessage) {
        auditLogRepository.save(AuditLog.builder()
                .className(className)
                .methodName(methodName)
                .timestamp(Instant.now())
                .executionTimeMs(System.currentTimeMillis() - startMillis)
                .success(success)
                .errorMessage(errorMessage)
                .build());
    }
}
