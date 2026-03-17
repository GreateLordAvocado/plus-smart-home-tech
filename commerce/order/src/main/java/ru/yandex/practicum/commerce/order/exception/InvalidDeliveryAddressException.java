package ru.yandex.practicum.commerce.order.exception;

public class InvalidDeliveryAddressException extends RuntimeException {

    public InvalidDeliveryAddressException(String message) {
        super(message);
    }
}