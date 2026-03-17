package ru.yandex.practicum.commerce.delivery.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.interactionapi.common.dto.ApiErrorDto;

@RestControllerAdvice
public class DeliveryErrorHandler {

    @ExceptionHandler
    public ApiErrorDto handleDeliveryNotFound(DeliveryNotFoundException e) {
        ApiErrorDto errorDto = new ApiErrorDto();
        errorDto.setHttpStatus(HttpStatus.NOT_FOUND.name());
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