package com.example.bookstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private String status;
    private int code;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .code(200)
                .message("Operación completada")
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }
}
