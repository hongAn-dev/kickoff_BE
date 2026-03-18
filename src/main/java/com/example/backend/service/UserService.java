package com.example.backend.service;

import com.example.backend.dto.request.UserRequest;
import com.example.backend.dto.response.UserResponse;

import jakarta.validation.Valid;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, @Valid UserRequest request);

    void deleteUser(Long id);

    UserResponse createUser(@Valid UserRequest request);
}