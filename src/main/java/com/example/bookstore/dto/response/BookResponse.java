package com.example.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String isbn;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private AuthorResponse author;
    private CategoryResponse category;
    private String createdBy;
}
