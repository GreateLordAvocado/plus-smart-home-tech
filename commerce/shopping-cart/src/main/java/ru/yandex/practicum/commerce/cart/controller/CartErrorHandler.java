package ru.yandex.practicum.commerce.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interactionapi.common.dto.ApiErrorDto;

@RestControllerAdvice
public class CartErrorHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<ApiErrorDto> handleAuth(NotAuthorizedUserException ex) {
        ApiErrorDto dto = new ApiErrorDto(
                HttpStatus.UNAUTHORIZED.name(),
                "Пользователь не указан или не авторизован",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<ApiErrorDto> handleNoProducts(NoProductsInShoppingCartException ex) {
        ApiErrorDto dto = new ApiErrorDto(
                HttpStatus.BAD_REQUEST.name(),
                "В корзине нет запрошенных товаров",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
}