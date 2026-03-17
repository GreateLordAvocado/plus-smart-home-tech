package ru.yandex.practicum.commerce.interactionapi.payment.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentFeignClient extends PaymentClient {
}