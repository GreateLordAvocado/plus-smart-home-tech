package ru.yandex.practicum.commerce.cart.exception;

public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Username is not provided");
    }

    public NotAuthorizedUserException(String message) {
        super(message);
    }
}