package com.example.walletservice.mapper;

import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface WalletMapper {
    Wallet toEntity(WalletRequest request);
    @Mapping(source="user.id",target="userId")
    WalletResponse toResponse(Wallet wallet);
}
