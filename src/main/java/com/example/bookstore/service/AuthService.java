package com.example.bookstore.service;

import com.example.bookstore.dto.request.LoginRequest;
import com.example.bookstore.dto.request.RegisterRequest;
import com.example.bookstore.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
