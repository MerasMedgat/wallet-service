package com.example.walletservice.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
}
