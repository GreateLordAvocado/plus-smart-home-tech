package ru.yandex.practicum.commerce.interactionapi.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewProductInWarehouseRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private DimensionDto dimension;

    @NotNull
    @Min(0)
    private Double weight;

    @NotNull
    private Boolean fragile;
}