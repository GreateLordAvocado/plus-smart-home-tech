package ru.yandex.practicum.commerce.cart.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;
import ru.yandex.practicum.commerce.interactionapi.cart.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService service;

    public ShoppingCartController(ShoppingCartService service) {
        this.service = service;
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return service.getOrCreate(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        return service.addProducts(username, products);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        return service.remove(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        return service.changeQuantity(username, request);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        service.deactivate(username);
    }
}