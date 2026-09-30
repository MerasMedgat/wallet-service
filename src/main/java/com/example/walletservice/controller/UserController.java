package com.example.walletservice.controller;

import com.example.walletservice.dto.request.LoginRequest;
import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.request.UpdateUserRequest;
import com.example.walletservice.dto.response.AuthResponse;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.service.AuthService;
import com.example.walletservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request){
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(){
        return userService.getCurrentUser();
    }

    @PatchMapping("/me")
    public UserResponse updateCurrentUser(@Valid @RequestBody UpdateUserRequest request){
        return userService.updateCurrentUser(request);
    }
}
