package ru.yandex.practicum.commerce.warehouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.NoSpecifiedProductInWarehouseExceptionDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.ProductInShoppingCartLowQuantityInWarehouseDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.SpecifiedProductAlreadyInWarehouseExceptionDto;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;

@RestControllerAdvice
public class WarehouseErrorHandler {

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    public ResponseEntity<SpecifiedProductAlreadyInWarehouseExceptionDto> handleAlreadyExists(SpecifiedProductAlreadyInWarehouseException ex) {
        SpecifiedProductAlreadyInWarehouseExceptionDto dto = new SpecifiedProductAlreadyInWarehouseExceptionDto();
        dto.setHttpStatus(HttpStatus.BAD_REQUEST.name());
        dto.setMessage(ex.getMessage());
        dto.setUserMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    public ResponseEntity<NoSpecifiedProductInWarehouseExceptionDto> handleNotFound(NoSpecifiedProductInWarehouseException ex) {
        NoSpecifiedProductInWarehouseExceptionDto dto = new NoSpecifiedProductInWarehouseExceptionDto();
        dto.setHttpStatus(HttpStatus.BAD_REQUEST.name());
        dto.setMessage(ex.getMessage());
        dto.setUserMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouseException.class)
    public ResponseEntity<ProductInShoppingCartLowQuantityInWarehouseDto> handleLowQuantity(ProductInShoppingCartLowQuantityInWarehouseException ex) {
        ProductInShoppingCartLowQuantityInWarehouseDto dto = new ProductInShoppingCartLowQuantityInWarehouseDto();
        dto.setHttpStatus(HttpStatus.BAD_REQUEST.name());
        dto.setMessage(ex.getMessage());
        dto.setUserMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
}