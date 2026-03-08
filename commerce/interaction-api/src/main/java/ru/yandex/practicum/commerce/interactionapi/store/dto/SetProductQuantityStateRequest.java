package ru.yandex.practicum.commerce.interactionapi.store.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SetProductQuantityStateRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private QuantityState quantityState;

    public SetProductQuantityStateRequest() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public QuantityState getQuantityState() {
        return quantityState;
    }

    public void setQuantityState(QuantityState quantityState) {
        this.quantityState = quantityState;
    }
}