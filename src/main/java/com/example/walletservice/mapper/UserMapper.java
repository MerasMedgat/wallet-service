package com.example.walletservice.mapper;

import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(RegisterRequest registerRequest);

    UserResponse toResponse(User user);


}
