package com.SpringBoot.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUnexpected_devuelve500_yNoFiltraElMensajeInterno() {
        ProblemDetail problemDetail = handler.handleUnexpected(new NullPointerException("boom interno"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getDetail()).isEqualTo("Ocurrió un error inesperado");
        assertThat(problemDetail.getDetail()).doesNotContain("boom interno");
    }

    @Test
    void handleOptimisticLock_devuelve409() {
        ProblemDetail problemDetail = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("version mismatch"));

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }
}
