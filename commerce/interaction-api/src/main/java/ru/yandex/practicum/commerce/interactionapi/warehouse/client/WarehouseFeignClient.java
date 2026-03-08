package ru.yandex.practicum.commerce.interactionapi.warehouse.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.*;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseFeignClient {

    @PutMapping
    void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/add")
    void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}