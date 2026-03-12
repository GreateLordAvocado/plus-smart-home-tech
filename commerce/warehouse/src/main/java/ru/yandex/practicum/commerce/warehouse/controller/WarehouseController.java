package ru.yandex.practicum.commerce.warehouse.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseAddressService;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
public class WarehouseController implements WarehouseClient {

    private final WarehouseService service;
    private final WarehouseAddressService addressService;

    public WarehouseController(WarehouseService service, WarehouseAddressService addressService) {
        this.service = service;
        this.addressService = addressService;
    }

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        service.newProduct(request);
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        service.add(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        return service.check(shoppingCartDto);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        return service.assemblyProductsForOrder(request);
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        service.acceptReturn(products);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return addressService.getAddress();
    }
}