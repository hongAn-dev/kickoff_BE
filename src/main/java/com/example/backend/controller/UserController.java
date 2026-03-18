package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.request.UserRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "/api/*")

public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .status(200)
                .message("Get users success")
                .data(userService.getAllUsers())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") Long id) {
        return ApiResponse.<UserResponse>builder()
                .status(200)
                .message("Get user success")
                .data(userService.getUserById(id))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .status(201)
                .message("User created")
                .data(userService.createUser(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserRequest request) {

        return ApiResponse.<UserResponse>builder()
                .status(200)
                .message("User updated")
                .data(userService.updateUser(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);

        return ApiResponse.<Void>builder()
                .status(204)
                .message("User deleted")
                .data(null)
                .build();
    }
}
