package ru.yandex.practicum.commerce.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interactionapi.delivery.client.DeliveryClient;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryClient {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @Override
    public void deliverySuccessful(DeliveryDto deliveryDto) {
        deliveryService.deliverySuccessful(deliveryDto);
    }

    @Override
    public void deliveryPicked(DeliveryDto deliveryDto) {
        deliveryService.deliveryPicked(deliveryDto);
    }

    @Override
    public void deliveryFailed(DeliveryDto deliveryDto) {
        deliveryService.deliveryFailed(deliveryDto);
    }

    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }
}