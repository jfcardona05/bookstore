package com.example.bookstore.repository;

import com.example.bookstore.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(String email);
}
