package com.SpringBoot.infrastructure.adapter.customer;

import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.domain.customer.CustomerLookupException;
import com.SpringBoot.domain.customer.CustomerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Service
public class CustomerHttpAdapter implements CustomerProvider {

    private static final Logger log = LoggerFactory.getLogger(CustomerHttpAdapter.class);

    private final JsonPlaceholderClient client;
    private final CustomerMapper mapper;

    public CustomerHttpAdapter(JsonPlaceholderClient client, CustomerMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Override
    public Optional<CustomerInfo> findById(Long id) {
        try {
            return Optional.of(mapper.toCustomerInfo(client.findById(id)));
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        } catch (WebClientResponseException e) {
            log.error("Customer service respondió con estado {} al buscar el cliente {}",
                    e.getStatusCode(), id, e);
            throw new CustomerLookupException(
                    "El servicio de clientes respondió con estado " + e.getStatusCode() + " para id " + id, e);
        } catch (WebClientRequestException e) {
            log.error("Fallo de conexión con el servicio de clientes al buscar el cliente {}", id, e);
            throw new CustomerLookupException(
                    "No se pudo contactar al servicio de clientes para id " + id, e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }
}
