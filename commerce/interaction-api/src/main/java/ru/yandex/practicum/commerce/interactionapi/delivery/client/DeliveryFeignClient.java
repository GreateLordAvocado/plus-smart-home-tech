package ru.yandex.practicum.commerce.interactionapi.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "delivery", path = "/api/v1/delivery")
public interface DeliveryFeignClient extends DeliveryClient {
}