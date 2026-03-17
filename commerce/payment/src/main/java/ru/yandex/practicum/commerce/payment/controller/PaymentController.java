package ru.yandex.practicum.commerce.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.payment.client.PaymentClient;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentClient {

    private final PaymentService paymentService;

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}