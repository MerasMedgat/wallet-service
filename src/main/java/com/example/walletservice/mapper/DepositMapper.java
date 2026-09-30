package com.example.walletservice.mapper;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface DepositMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Deposit toEntity(DepositRequest depositRequest);
    @Mapping(source="wallet.id",target="walletId")
    @Mapping(source="status",target="depositStatus")
    DepositResponse toResponse(Deposit deposit);
}
