package com.example.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String status;
    private String customerEmail;
    private BigDecimal total;
    private Instant createdAt;
    private List<OrderItemResponse> items;
}
