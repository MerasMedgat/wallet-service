package com.example.walletservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Email
    private String email;
    @Size(max=20)
    private String phone;
    private String firstName;
    private String lastName;

}