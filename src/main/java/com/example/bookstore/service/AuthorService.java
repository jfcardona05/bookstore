package com.example.bookstore.service;

import com.example.bookstore.dto.request.AuthorRequest;
import com.example.bookstore.dto.response.AuthorResponse;

import java.util.List;

public interface AuthorService {

    AuthorResponse create(AuthorRequest request);

    List<AuthorResponse> findAll();

    AuthorResponse findById(Long id);

    AuthorResponse update(Long id, AuthorRequest request);

    void delete(Long id);
}
