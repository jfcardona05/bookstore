package com.example.bookstore.controller;

import com.example.bookstore.dto.request.LoginRequest;
import com.example.bookstore.dto.request.RegisterRequest;
import com.example.bookstore.dto.response.ApiResponse;
import com.example.bookstore.dto.response.AuthResponse;
import com.example.bookstore.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(service.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(service.login(request));
    }
}
