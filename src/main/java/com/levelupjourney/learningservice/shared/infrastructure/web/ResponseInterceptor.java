package com.levelupjourney.learningservice.shared.infrastructure.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseInterceptor implements ResponseBodyAdvice<Object> {
    
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Skip wrapping for already wrapped responses, swagger endpoints, and actuator
        String declaringClass = returnType.getDeclaringClass().getName();
        return !declaringClass.contains("springdoc") 
               && !declaringClass.contains("actuator")
               && !returnType.getParameterType().equals(ApiResponse.class);
    }
    
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        
        // If already wrapped, return as is
        if (body instanceof ApiResponse) {
            return body;
        }
        
        // Wrap the response
        int statusCode = 200;
        if (response instanceof ServletServerHttpResponse) {
            statusCode = ((ServletServerHttpResponse) response).getServletResponse().getStatus();
        }
        
        return ApiResponse.success(body, statusCode);
    }
}
