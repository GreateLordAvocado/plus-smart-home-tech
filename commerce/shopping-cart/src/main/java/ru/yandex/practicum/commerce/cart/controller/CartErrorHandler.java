package ru.yandex.practicum.commerce.cart.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.NoProductsInShoppingCartExceptionDto;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.NotAuthorizedUserExceptionDto;

@RestControllerAdvice
public class CartErrorHandler {

    @ExceptionHandler(NotAuthorizedUserException.class)
    public ResponseEntity<NotAuthorizedUserExceptionDto> handleAuth(NotAuthorizedUserException ex) {
        NotAuthorizedUserExceptionDto dto = new NotAuthorizedUserExceptionDto();
        dto.setHttpStatus(HttpStatus.UNAUTHORIZED.name());
        dto.setMessage(ex.getMessage());
        dto.setUserMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    public ResponseEntity<NoProductsInShoppingCartExceptionDto> handleNoProducts(NoProductsInShoppingCartException ex) {
        NoProductsInShoppingCartExceptionDto dto = new NoProductsInShoppingCartExceptionDto();
        dto.setHttpStatus(HttpStatus.BAD_REQUEST.name());
        dto.setMessage(ex.getMessage());
        dto.setUserMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
}