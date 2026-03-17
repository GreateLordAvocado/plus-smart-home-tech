package ru.yandex.practicum.commerce.interactionapi.delivery.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    private UUID deliveryId;

    @NotNull
    private AddressDto fromAddress;

    @NotNull
    private AddressDto toAddress;

    @NotNull
    private UUID orderId;

    @NotNull
    private DeliveryState deliveryState;
}