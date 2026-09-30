package com.example.walletservice.dto.response;

import com.example.walletservice.enums.RoleEnum;

public record UserResponse(
        Long id,
        String email,
        String phone,
        String firstName,
        String lastName,
        RoleEnum role
) {
}
