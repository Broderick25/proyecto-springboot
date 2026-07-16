package com.SpringBoot.infrastructure.adapter.customer;

import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.domain.customer.CustomerLookupException;
import com.SpringBoot.infrastructure.adapter.customer.dto.AddressDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.CompanyDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.GeoDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerHttpAdapterTest {

    @Mock
    private JsonPlaceholderClient client;

    private CustomerHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerHttpAdapter(client, Mappers.getMapper(CustomerMapper.class));
    }

    private static UserDTO aUser(Long id) {
        return new UserDTO(id, "Leanne Graham", "Bret", "leanne@example.com",
                new AddressDTO("Kulas Light", "Apt. 556", "Gwenborough", "92998-3874",
                        new GeoDTO("-37.3159", "81.1496")),
                "1-770-736-8031", "hildegard.org",
                new CompanyDTO("Romaguera-Crona", "Multi-layered client-server neural-net", "harness real-time e-markets"));
    }

    @Test
    void findById_devuelveCustomerInfo_cuandoElClienteResponde() {
        when(client.findById(1L)).thenReturn(aUser(1L));

        Optional<CustomerInfo> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("Leanne Graham");
    }

    @Test
    void findById_devuelveEmpty_cuandoElClienteRespondeNotFound() {
        when(client.findById(99L)).thenThrow(notFound());

        Optional<CustomerInfo> result = adapter.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_lanzaCustomerLookupException_cuandoHayErrorDeRespuesta() {
        when(client.findById(2L)).thenThrow(serverError());

        assertThatThrownBy(() -> adapter.findById(2L))
                .isInstanceOf(CustomerLookupException.class)
                .hasCauseInstanceOf(WebClientResponseException.class);
    }

    @Test
    void findById_lanzaCustomerLookupException_cuandoHayErrorDeConexion() {
        when(client.findById(3L)).thenThrow(connectionError(3L));

        assertThatThrownBy(() -> adapter.findById(3L))
                .isInstanceOf(CustomerLookupException.class)
                .hasCauseInstanceOf(WebClientRequestException.class);
    }

    @Test
    void existsById_devuelveTrue_cuandoElClienteResponde() {
        when(client.findById(1L)).thenReturn(aUser(1L));

        assertThat(adapter.existsById(1L)).isTrue();
    }

    @Test
    void existsById_devuelveFalse_cuandoElClienteRespondeNotFound() {
        when(client.findById(99L)).thenThrow(notFound());

        assertThat(adapter.existsById(99L)).isFalse();
    }

    private static WebClientResponseException notFound() {
        return WebClientResponseException.create(404, "Not Found", new HttpHeaders(), new byte[0], null);
    }

    private static WebClientResponseException serverError() {
        return WebClientResponseException.create(500, "Internal Server Error", new HttpHeaders(), new byte[0], null);
    }

    private static WebClientRequestException connectionError(long id) {
        return new WebClientRequestException(new IOException("timeout"), HttpMethod.GET,
                URI.create("https://jsonplaceholder.typicode.com/users/" + id), new HttpHeaders());
    }
}
