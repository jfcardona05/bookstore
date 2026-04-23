package com.example.bookstore.service;

import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.response.BookResponse;

import java.util.List;

public interface BookService {

    BookResponse create(BookRequest request);

    List<BookResponse> findAll(String title);

    BookResponse findById(Long id);

    BookResponse update(Long id, BookRequest request);

    void delete(Long id);
}
