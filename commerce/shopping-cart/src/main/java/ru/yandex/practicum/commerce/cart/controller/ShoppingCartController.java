package ru.yandex.practicum.commerce.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;
import ru.yandex.practicum.commerce.interactionapi.cart.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService service;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);
        return service.getOrCreate(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        validateUsername(username);
        return service.addProducts(username, products);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        validateUsername(username);
        return service.remove(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);
        return service.changeQuantity(username, request);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        validateUsername(username);
        service.deactivate(username);
    }

    private void validateUsername(String username) {
        if (username == null) {
            throw new NotAuthorizedUserException("Username is null");
        }
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Username is blank");
        }
    }
}