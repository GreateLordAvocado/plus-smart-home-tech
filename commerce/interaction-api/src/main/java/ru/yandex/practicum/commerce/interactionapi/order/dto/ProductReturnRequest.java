package ru.yandex.practicum.commerce.interactionapi.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {

    private UUID orderId;

    @NotNull
    private Map<UUID, Long> products;
}