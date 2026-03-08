package ru.yandex.practicum.commerce.interactionapi.store.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interactionapi.store.dto.PageProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.QuantityState;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store")
@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    PageProductDto getProducts(@RequestParam("category") ProductCategory category,
                               @RequestParam(value = "page", required = false) Integer page,
                               @RequestParam(value = "size", required = false) Integer size,
                               @RequestParam(value = "sort", required = false) List<String> sort);

    @PutMapping
    ProductDto createProduct(@RequestBody ProductDto product);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto product);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);

    @PostMapping("/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    Boolean setQuantityState(@RequestParam("productId") UUID productId,
                             @RequestParam("quantityState") QuantityState quantityState);
}