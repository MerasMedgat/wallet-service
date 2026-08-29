package com.example.walletservice.controller;


import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.request.UpdateUserRequest;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request){
        return userService.register(request);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id){
        return userService.getUserById(id);
    }

    @PatchMapping("/{id}")
    public UserResponse updateUserProfile(@PathVariable Long id ,@Valid @RequestBody UpdateUserRequest request){
        return userService.updateUserProfile(id,request);
    }


}
