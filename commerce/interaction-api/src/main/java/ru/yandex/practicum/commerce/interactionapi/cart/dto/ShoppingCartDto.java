package ru.yandex.practicum.commerce.interactionapi.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {

    @NotNull
    private UUID shoppingCartId;

    @NotNull
    private Map<UUID, Long> products;
}