package ru.yandex.practicum.commerce.interactionapi.cart.client;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/api/v1/shopping-cart")
public interface ShoppingCartClient {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam("username") String username);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(@RequestParam("username") String username,
                                             @RequestBody Map<UUID, Long> products);

    @PostMapping("/remove")
    ShoppingCartDto removeFromShoppingCart(@RequestParam("username") String username,
                                           @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestParam("username") String username,
                                          @RequestBody ChangeProductQuantityRequest request);

    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam("username") String username);
}