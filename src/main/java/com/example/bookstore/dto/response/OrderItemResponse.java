package com.example.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderItemResponse {

    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
