package com.example.walletservice.mapper;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface TransactionMapper {
    @Mapping(source="wallet.id",target="walletId")
    TransactionResponse toResponse(Transaction transaction);
}
