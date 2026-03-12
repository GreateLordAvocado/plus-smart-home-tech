package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class NoOrderBookingFoundException extends RuntimeException {

    public NoOrderBookingFoundException(UUID orderId) {
        super("Order booking not found for orderId=" + orderId);
    }
}