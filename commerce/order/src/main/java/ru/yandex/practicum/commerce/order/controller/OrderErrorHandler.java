package ru.yandex.practicum.commerce.order.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interactionapi.common.dto.ApiErrorDto;
import ru.yandex.practicum.commerce.order.exception.InvalidDeliveryAddressException;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;

@RestControllerAdvice
public class OrderErrorHandler {

    @ExceptionHandler
    public ApiErrorDto handleNoOrderFound(NoOrderFoundException e) {
        ApiErrorDto errorDto = new ApiErrorDto();
        errorDto.setHttpStatus(HttpStatus.NOT_FOUND.name());
        errorDto.setMessage(e.getMessage());
        return errorDto;
    }

    @ExceptionHandler
    public ApiErrorDto handleInvalidDeliveryAddress(InvalidDeliveryAddressException e) {
        ApiErrorDto errorDto = new ApiErrorDto();
        errorDto.setHttpStatus(HttpStatus.BAD_REQUEST.name());
        errorDto.setMessage(e.getMessage());
        return errorDto;
    }

    @ExceptionHandler
    public ApiErrorDto handleThrowable(Throwable e) {
        ApiErrorDto errorDto = new ApiErrorDto();
        errorDto.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        errorDto.setMessage(e.getMessage());
        return errorDto;
    }
}