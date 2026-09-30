package com.example.walletservice.service;

import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.request.UpdateUserRequest;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.entity.User;
import com.example.walletservice.enums.RoleEnum;
import com.example.walletservice.exception.ConflictException;
import com.example.walletservice.mapper.UserMapper;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new ConflictException("Phone already exists");
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(RoleEnum.USER);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(){
        return userMapper.toResponse(userRepository.getOrThrow(SecurityUtils.currentUserId()));
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = userRepository.getOrThrow(SecurityUtils.currentUserId());
        if (request.email() != null
                && !request.email().equals(user.getEmail())
                && userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }
        if (request.phone() != null
                && !request.phone().equals(user.getPhone())
                && userRepository.existsByPhone(request.phone())) {
            throw new ConflictException("Phone already exists");
        }
        userMapper.updateEntity(request, user);
        return userMapper.toResponse(userRepository.saveAndFlush(user));
    }
}
