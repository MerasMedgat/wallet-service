package com.example.walletservice.mapper;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface DepositMapper {
    Deposit  toEntity(DepositRequest depositRequest);
    @Mapping(source="wallet.id",target="walletId")
    DepositResponse toResponse(Deposit deposit);
}
