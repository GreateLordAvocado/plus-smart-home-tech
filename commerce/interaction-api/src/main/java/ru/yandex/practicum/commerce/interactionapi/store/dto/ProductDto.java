package ru.yandex.practicum.commerce.interactionapi.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProductDto {

    private UUID productId;

    @NotBlank
    private String productName;

    @NotBlank
    private String description;

    @NotBlank
    private String imageSrc;

    @NotNull
    private QuantityState quantityState;

    @NotNull
    private ProductState productState;

    @NotNull
    private ProductCategory productCategory;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;
}