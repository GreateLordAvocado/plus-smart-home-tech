package ru.yandex.practicum.commerce.interactionapi.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProductQuantityRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Long newQuantity;
}