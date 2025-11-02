package com.ntt.orders.shared.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;
    private LocalDateTime timestamp;


    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Thành công")
                .code(ResponseCode.SUCCESS)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .code(ResponseCode.SUCCESS)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Tạo thành công")
                .code(ResponseCode.CREATED)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> updated(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Cập nhật thành công")
                .code(ResponseCode.UPDATED)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> deleted() {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Xóa thành công")
                .code(ResponseCode.DELETED)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }


    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

}