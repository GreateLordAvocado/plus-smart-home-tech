package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super("Product already exists in warehouse: " + productId);
    }
}