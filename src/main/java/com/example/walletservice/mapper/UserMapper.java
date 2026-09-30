package com.example.walletservice.mapper;

import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.request.UpdateUserRequest;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    User toEntity(RegisterRequest registerRequest);

    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    void updateEntity(UpdateUserRequest request,@MappingTarget User user);

}
