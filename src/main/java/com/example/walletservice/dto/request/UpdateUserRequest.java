package com.example.walletservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Partial update: null fields are left unchanged. */
public record UpdateUserRequest(
        @Email
        String email,
        @Size(max = 20)
        String phone,
        String firstName,
        String lastName
) {
}
