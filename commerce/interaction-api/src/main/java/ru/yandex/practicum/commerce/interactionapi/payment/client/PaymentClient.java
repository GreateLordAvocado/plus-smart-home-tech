package ru.yandex.practicum.commerce.interactionapi.payment.client;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentClient {

    @PostMapping
    PaymentDto payment(@RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    void paymentSuccess(@RequestBody UUID paymentId);

    @PostMapping("/productCost")
    BigDecimal productCost(@RequestBody OrderDto orderDto);

    @PostMapping("/failed")
    void paymentFailed(@RequestBody UUID paymentId);
}