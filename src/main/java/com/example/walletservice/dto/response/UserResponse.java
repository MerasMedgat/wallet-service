package com.example.walletservice.dto.response;


import com.example.walletservice.enums.RoleEnum;
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
    private RoleEnum role;
}
