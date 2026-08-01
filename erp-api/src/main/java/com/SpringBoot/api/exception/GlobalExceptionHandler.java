package com.SpringBoot.api.exception;

import com.SpringBoot.domain.customer.CustomerLookupException;
import com.SpringBoot.domain.customer.CustomerNotFoundException;
import com.SpringBoot.domain.document.CategoryInUseException;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.document.DuplicateCategoryException;
import com.SpringBoot.domain.entity.DuplicateSkuException;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.entity.ProductInUseException;
import com.SpringBoot.domain.entity.ProductInactiveException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            ProductNotFoundException.class,
            OrderNotFoundException.class,
            CategoryNotFoundException.class,
            CustomerNotFoundException.class
    })
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            DuplicateSkuException.class,
            DuplicateCategoryException.class,
            ProductInUseException.class,
            CategoryInUseException.class,
            ProductInactiveException.class
    })
    public ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Category (Mongo) usa @Version para bloqueo optimista: dos escrituras concurrentes sobre
    // el mismo catálogo hacen que la segunda falle acá en vez de pisar en silencio a la primera.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El recurso fue modificado por otra solicitud, reintentá la operación");
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    public ProblemDetail handleBadRequest(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CustomerLookupException.class)
    public ProblemDetail handleUpstreamFailure(CustomerLookupException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // Red de seguridad: cualquier excepción no mapeada arriba (ej. una NullPointerException por
    // un campo requerido ausente en el JSON) devuelve un ProblemDetail limpio en vez de la
    // whitelabel page por defecto de Spring Boot, sin filtrar el mensaje/stacktrace interno.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Excepción no manejada", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }
}
