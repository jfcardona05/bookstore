package com.example.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorResponse {

    private Long id;
    private String name;
    private String biography;
}
