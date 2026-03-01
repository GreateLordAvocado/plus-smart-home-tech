package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.Map;
import java.util.UUID;

public class ProductInShoppingCartLowQuantityInWarehouseException extends RuntimeException {

    private final Map<UUID, Long> missing;

    public ProductInShoppingCartLowQuantityInWarehouseException(Map<UUID, Long> missing) {
        super("Not enough products in warehouse. Missing: " + missing);
        this.missing = missing;
    }

    public Map<UUID, Long> getMissing() {
        return missing;
    }
}