package com.SpringBoot.infrastructure.adapter.customer;

import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.infrastructure.adapter.customer.dto.AddressDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.CompanyDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.GeoDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void toCustomerInfo_mapeaCamposDirectosYAnidados() {
        UserDTO user = new UserDTO(1L, "Leanne Graham", "Bret", "leanne@example.com",
                new AddressDTO("Kulas Light", "Apt. 556", "Gwenborough", "92998-3874",
                        new GeoDTO("-37.3159", "81.1496")),
                "1-770-736-8031", "hildegard.org",
                new CompanyDTO("Romaguera-Crona", "Multi-layered client-server neural-net", "harness real-time e-markets"));

        CustomerInfo info = mapper.toCustomerInfo(user);

        assertThat(info.id()).isEqualTo(1L);
        assertThat(info.name()).isEqualTo("Leanne Graham");
        assertThat(info.email()).isEqualTo("leanne@example.com");
        assertThat(info.phone()).isEqualTo("1-770-736-8031");
        assertThat(info.address()).isEqualTo("Kulas Light, Apt. 556");
        assertThat(info.city()).isEqualTo("Gwenborough");
        assertThat(info.zipcode()).isEqualTo("92998-3874");
        assertThat(info.companyName()).isEqualTo("Romaguera-Crona");
    }

    @Test
    void toCustomerInfo_manejaAddressNula_sinLanzarExcepcion() {
        UserDTO user = new UserDTO(1L, "Leanne Graham", "Bret", "leanne@example.com",
                null, "1-770-736-8031", "hildegard.org",
                new CompanyDTO("Romaguera-Crona", "Multi-layered client-server neural-net", "harness real-time e-markets"));

        CustomerInfo info = mapper.toCustomerInfo(user);

        assertThat(info.address()).isNull();
        assertThat(info.city()).isNull();
        assertThat(info.zipcode()).isNull();
    }

    @Test
    void toCustomerInfo_manejaCompanyNula_sinLanzarExcepcion() {
        UserDTO user = new UserDTO(1L, "Leanne Graham", "Bret", "leanne@example.com",
                new AddressDTO("Kulas Light", "Apt. 556", "Gwenborough", "92998-3874", null),
                "1-770-736-8031", "hildegard.org", null);

        CustomerInfo info = mapper.toCustomerInfo(user);

        assertThat(info.companyName()).isNull();
    }
}
