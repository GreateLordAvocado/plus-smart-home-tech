package ru.yandex.practicum.commerce.interactionapi.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {

    @NotNull
    private Double width;

    @NotNull
    private Double height;

    @NotNull
    private Double depth;
}