package com.SpringBoot.api.controller;

import com.SpringBoot.api.exception.GlobalExceptionHandler;
import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.audit.ListAuditLogsQueryHandler;
import com.SpringBoot.application.query.audit.view.AuditLogView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Ver nota en CategoryControllerTest: ErpApiApplication hace @ComponentScan de todo
// com.SpringBoot + @Enable{Jpa,Mongo}Repositories, lo que rompe el aislamiento de @WebMvcTest
// si se lo deja autodetectar esa clase — se acota el contexto explícitamente.
@WebMvcTest(AuditLogController.class)
@ContextConfiguration(classes = AuditLogController.class)
@Import(GlobalExceptionHandler.class)
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListAuditLogsQueryHandler listAuditLogsQueryHandler;

    @Test
    void list_devuelveLaPaginaDeAuditLogs() throws Exception {
        AuditLogView logView = new AuditLogView("audit-1", "CreateProductCommandHandler", "handle",
                Instant.parse("2026-01-01T00:00:00Z"), 12L, true, null);
        PageView<AuditLogView> page = new PageView<>(List.of(logView), 0, 20, 1, 1);
        when(listAuditLogsQueryHandler.handle(any())).thenReturn(page);

        mockMvc.perform(get("/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].className").value("CreateProductCommandHandler"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void list_devuelve400_cuandoSizeExcedeElMaximo() throws Exception {
        when(listAuditLogsQueryHandler.handle(any()))
                .thenThrow(new IllegalArgumentException("size must not exceed 100"));

        mockMvc.perform(get("/audit-logs").param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
