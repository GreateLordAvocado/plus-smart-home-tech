package ru.yandex.practicum.commerce.interactionapi.delivery.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;

import java.math.BigDecimal;

public interface DeliveryClient {

    @PutMapping
    DeliveryDto planDelivery(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/successful")
    void deliverySuccessful(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/picked")
    void deliveryPicked(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/failed")
    void deliveryFailed(@RequestBody DeliveryDto deliveryDto);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@RequestBody OrderDto orderDto);
}