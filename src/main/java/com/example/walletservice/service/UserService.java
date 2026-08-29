package com.example.walletservice.service;


import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.request.UpdateUserRequest;
import com.example.walletservice.dto.response.UserResponse;
import com.example.walletservice.entity.User;
import com.example.walletservice.exception.EmailAlreadyExistsException;
import com.example.walletservice.exception.PhoneAlreadyExistsException;
import com.example.walletservice.exception.UserNotFoundException;
import com.example.walletservice.mapper.UserMapper;
import com.example.walletservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if(userRepository.existsByPhone(registerRequest.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone already exists");
        }


        User user = userMapper.toEntity(registerRequest);

        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser );
    }

    public UserResponse getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()->
                        new UserNotFoundException("User not Found"));
        return userMapper.toResponse(user);
    }

    public UserResponse updateUserProfile(Long id,UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (request.getPhone() != null
                && !request.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone already exists");
        }

        userMapper.updateEntity(request, user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public void deleteById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(()->
                        new UserNotFoundException("UserNotFound"));
        userRepository.delete(user);
    }
}
