package ru.yandex.practicum.commerce.interactionapi.order.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderFeignClient extends OrderClient {
}