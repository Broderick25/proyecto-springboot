package com.SpringBoot.infrastructure.audit;

import com.SpringBoot.domain.document.AuditLog;
import com.SpringBoot.domain.repository.AuditLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HandlerAuditAspectTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private HandlerAuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new HandlerAuditAspect(auditLogRepository);
    }

    private static class DummyCommandHandler {
    }

    private static class DummyQueryHandler {
    }

    @Test
    void audit_devuelveElResultado_yRegistraExito_cuandoElCommandHandlerNoFalla() throws Throwable {
        when(joinPoint.getTarget()).thenReturn(new DummyCommandHandler());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("handle");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.audit(joinPoint);

        assertThat(result).isEqualTo("ok");
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getClassName()).isEqualTo("DummyCommandHandler");
        assertThat(captor.getValue().getMethodName()).isEqualTo("handle");
        assertThat(captor.getValue().getSuccess()).isTrue();
        assertThat(captor.getValue().getErrorMessage()).isNull();
        assertThat(captor.getValue().getExecutionTimeMs()).isNotNegative();
    }

    @Test
    void audit_relanzaLaExcepcion_yRegistraFalloConElMensaje() throws Throwable {
        when(joinPoint.getTarget()).thenReturn(new DummyCommandHandler());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("handle");
        RuntimeException failure = new IllegalArgumentException("sku duplicado");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.audit(joinPoint)).isSameAs(failure);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getSuccess()).isFalse();
        assertThat(captor.getValue().getErrorMessage()).isEqualTo("sku duplicado");
    }

    @Test
    void audit_registraExito_cuandoElQueryHandlerNoFalla() throws Throwable {
        when(joinPoint.getTarget()).thenReturn(new DummyQueryHandler());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("handle");
        when(joinPoint.proceed()).thenReturn("view");

        Object result = aspect.audit(joinPoint);

        assertThat(result).isEqualTo("view");
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getClassName()).isEqualTo("DummyQueryHandler");
        assertThat(captor.getValue().getSuccess()).isTrue();
    }

    @Test
    void audit_noAuditaLlamadasAnidadas_soloLaMasExterna() throws Throwable {
        when(joinPoint.getTarget()).thenReturn(new DummyCommandHandler());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("handle");

        ProceedingJoinPoint nestedJoinPoint = mock(ProceedingJoinPoint.class);
        when(nestedJoinPoint.proceed()).thenReturn("nested-result");

        // Simula un handler que, dentro de su propia ejecución, invoca a otro handler —
        // esa llamada anidada también pasa por el mismo aspecto (mismo hilo).
        when(joinPoint.proceed()).thenAnswer(invocation -> {
            aspect.audit(nestedJoinPoint);
            return "outer-result";
        });

        Object result = aspect.audit(joinPoint);

        assertThat(result).isEqualTo("outer-result");
        verify(nestedJoinPoint).proceed();
        verify(auditLogRepository, times(1)).save(any());
    }
}
