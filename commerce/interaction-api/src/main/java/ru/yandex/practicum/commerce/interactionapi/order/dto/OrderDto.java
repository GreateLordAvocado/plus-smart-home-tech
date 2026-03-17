package ru.yandex.practicum.commerce.interactionapi.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @NotNull
    private UUID orderId;

    private UUID shoppingCartId;

    @NotNull
    private Map<UUID, Long> products;

    private UUID paymentId;

    private UUID deliveryId;

    private OrderState state;

    private Double deliveryWeight;

    private Double deliveryVolume;

    private Boolean fragile;

    private BigDecimal totalPrice;

    private BigDecimal deliveryPrice;

    private BigDecimal productPrice;

    private AddressDto deliveryAddress;
}