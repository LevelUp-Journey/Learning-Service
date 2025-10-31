package com.levelupjourney.learningservice.shared.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private String error;
    private boolean success;
    private Integer statusCode;
    
    public static <T> ApiResponse<T> success(T data, int statusCode) {
        return new ApiResponse<>(data, null, true, statusCode);
    }
    
    public static <T> ApiResponse<T> error(String error, int statusCode) {
        return new ApiResponse<>(null, error, false, statusCode);
    }
}
