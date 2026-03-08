package ru.yandex.practicum.commerce.interactionapi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorDto {

    private String httpStatus;

    private String userMessage;

    private String message;
}