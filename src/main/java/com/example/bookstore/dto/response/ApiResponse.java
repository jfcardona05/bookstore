package com.example.bookstore.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiResponse<T> {

    private final String status;
    private final int code;
    private final String message;
    private final T data;
    private final String timestamp;

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .code(code)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static ApiResponse<Void> error(int code, String message) {
        return ApiResponse.<Void>builder()
                .status("error")
                .code(code)
                .message(message)
                .timestamp(Instant.now().toString())
                .build();
    }
}
