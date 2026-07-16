package com.SpringBoot.infrastructure.adapter.customer;

import com.SpringBoot.domain.customer.CustomerInfo;
import com.SpringBoot.infrastructure.adapter.customer.dto.AddressDTO;
import com.SpringBoot.infrastructure.adapter.customer.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    @Mapping(target = "address", expression = "java(formatAddress(user.address()))")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.zipcode", target = "zipcode")
    @Mapping(source = "company.name", target = "companyName")
    CustomerInfo toCustomerInfo(UserDTO user);

    default String formatAddress(AddressDTO address) {
        if (address == null) {
            return null;
        }
        return address.street() + ", " + address.suite();
    }
}
