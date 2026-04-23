package com.example.bookstore.impl;

import com.example.bookstore.dto.request.OrderItemRequest;
import com.example.bookstore.dto.request.OrderRequest;
import com.example.bookstore.dto.response.OrderItemResponse;
import com.example.bookstore.dto.response.OrderResponse;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.CustomerOrder;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.OrderStatus;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.custom.InsufficientStockException;
import com.example.bookstore.exception.custom.ResourceNotFoundException;
import com.example.bookstore.exception.custom.UnauthorizedActionException;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CustomerOrderRepository;
import com.example.bookstore.service.CurrentUserService;
import com.example.bookstore.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerOrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CurrentUserService currentUserService;

    public OrderServiceImpl(CustomerOrderRepository orderRepository,
                            BookRepository bookRepository,
                            CurrentUserService currentUserService) {
        this.orderRepository = orderRepository;
        this.bookRepository = bookRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = currentUserService.getCurrentUser();

        CustomerOrder order = CustomerOrder.builder()
                .status(OrderStatus.CREATED)
                .total(BigDecimal.ZERO)
                .user(user)
                .build();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Book book = bookRepository.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + itemRequest.getBookId()));

            if (book.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Stock insuficiente para el libro: " + book.getTitle());
            }

            book.setStock(book.getStock() - itemRequest.getQuantity());
            BigDecimal subtotal = book.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                    .book(book)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(book.getPrice())
                    .order(order)
                    .build());
        }

        order.setTotal(total);
        order.setItems(items);
        return toResponse(orderRepository.save(order));
    }

    @Override
    public List<OrderResponse> findMine() {
        User user = currentUserService.getCurrentUser();
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(user.getEmail()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public OrderResponse findById(Long id) {
        User user = currentUserService.getCurrentUser();
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        boolean isAdmin = user.getRole().name().equals("ROLE_ADMIN");
        boolean isOwner = order.getUser().getEmail().equalsIgnoreCase(user.getEmail());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedActionException("No tienes permisos para ver este pedido");
        }

        return toResponse(order);
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getUser().getEmail(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getBook().getId(),
                                item.getBook().getTitle(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        ))
                        .toList()
        );
    }
}
