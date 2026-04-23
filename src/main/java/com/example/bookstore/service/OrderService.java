package com.example.bookstore.service;

import com.example.bookstore.dto.request.OrderRequest;
import com.example.bookstore.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest request);

    List<OrderResponse> findMine();

    List<OrderResponse> findAll();

    OrderResponse findById(Long id);
}
