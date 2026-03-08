package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.*;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repo.WarehouseProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseProductRepository repo;

    public WarehouseService(WarehouseProductRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void newProduct(NewProductInWarehouseRequest req) {
        UUID productId = req.getProductId();
        if (repo.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }

        WarehouseProductEntity e = new WarehouseProductEntity();
        e.setProductId(productId);
        e.setFragile(Boolean.TRUE.equals(req.getFragile()));
        e.setWidth(req.getDimension().getWidth());
        e.setHeight(req.getDimension().getHeight());
        e.setDepth(req.getDimension().getDepth());
        e.setWeight(req.getWeight());
        e.setQuantity(0L);

        repo.save(e);
    }

    @Transactional
    public void add(AddProductToWarehouseRequest req) {
        UUID productId = req.getProductId();
        WarehouseProductEntity e = repo.findById(productId).orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

        long add = req.getQuantity();
        e.setQuantity(e.getQuantity() + add);
        repo.save(e);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto check(ShoppingCartDto cart) {
        Map<UUID, Long> products = cart.getProducts();
        Map<UUID, Long> missing = new HashMap<>();

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean anyFragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            long qty = entry.getValue() == null ? 0L : entry.getValue();

            WarehouseProductEntity e = repo.findById(productId).orElse(null);
            if (e == null) {
                missing.put(productId, qty);
                continue;
            }

            long available = e.getQuantity();
            if (available < qty) {
                missing.put(productId, qty - available);
                continue;
            }

            totalWeight += e.getWeight() * qty;
            totalVolume += (e.getWidth() * e.getHeight() * e.getDepth()) * qty;
            anyFragile = anyFragile || e.isFragile();
        }

        if (!missing.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(missing);
        }

        BookedProductsDto dto = new BookedProductsDto();
        dto.setDeliveryWeight(totalWeight);
        dto.setDeliveryVolume(totalVolume);
        dto.setFragile(anyFragile);
        return dto;
    }
}