package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Product not found in warehouse: " + productId);
    }
}