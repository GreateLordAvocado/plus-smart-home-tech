package ru.yandex.practicum.commerce.interactionapi.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseFeignClient extends WarehouseClient {
}