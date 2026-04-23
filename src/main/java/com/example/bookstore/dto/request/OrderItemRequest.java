package com.example.bookstore.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull
    private Long bookId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
