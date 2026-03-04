package ru.yandex.practicum.commerce.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interactionapi.common.dto.ApiErrorDto;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(ProductNotFoundException ex) {
        ApiErrorDto dto = new ApiErrorDto(
                HttpStatus.NOT_FOUND.name(),
                "Товар не найден",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }
}