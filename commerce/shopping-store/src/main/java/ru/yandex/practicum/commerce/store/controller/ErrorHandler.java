package ru.yandex.practicum.commerce.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public org.springframework.http.ResponseEntity<Map<String, Object>> handleNotFound(ProductNotFoundException ex) {
        return org.springframework.http.ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "ProductNotFoundException",
                        "message", ex.getMessage()
                ));
    }
}