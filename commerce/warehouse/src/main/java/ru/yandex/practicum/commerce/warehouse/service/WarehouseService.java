package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.exception.NoOrderBookingFoundException;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.OrderBookingEntity;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.repo.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repo.WarehouseProductRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WarehouseService {

    private final WarehouseProductRepository productRepository;
    private final OrderBookingRepository orderBookingRepository;

    public WarehouseService(WarehouseProductRepository productRepository,
                            OrderBookingRepository orderBookingRepository) {
        this.productRepository = productRepository;
        this.orderBookingRepository = orderBookingRepository;
    }

    @Transactional
    public void newProduct(NewProductInWarehouseRequest request) {
        UUID productId = request.getProductId();
        if (productRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(productId);
        }

        WarehouseProductEntity entity = new WarehouseProductEntity();
        entity.setProductId(productId);
        entity.setFragile(Boolean.TRUE.equals(request.getFragile()));
        entity.setWidth(request.getDimension().getWidth());
        entity.setHeight(request.getDimension().getHeight());
        entity.setDepth(request.getDimension().getDepth());
        entity.setWeight(request.getWeight());
        entity.setQuantity(0L);

        productRepository.save(entity);
    }

    @Transactional
    public void add(AddProductToWarehouseRequest request) {
        UUID productId = request.getProductId();
        WarehouseProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

        long addQuantity = request.getQuantity();
        entity.setQuantity(entity.getQuantity() + addQuantity);
        productRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto check(ShoppingCartDto cart) {
        return calculateBooking(cart.getProducts(), false);
    }

    @Transactional
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        Map<UUID, Long> products = request.getProducts();
        BookedProductsDto bookedProductsDto = calculateBooking(products, true);

        OrderBookingEntity orderBookingEntity = new OrderBookingEntity();
        orderBookingEntity.setOrderId(request.getOrderId());
        orderBookingEntity.setProducts(new HashMap<>(products));
        orderBookingEntity.setDeliveryId(null);

        orderBookingRepository.save(orderBookingEntity);

        return bookedProductsDto;
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBookingEntity orderBookingEntity = orderBookingRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NoOrderBookingFoundException(request.getOrderId()));

        orderBookingEntity.setDeliveryId(request.getDeliveryId());
        orderBookingRepository.save(orderBookingEntity);
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantityValue = entry.getValue();

            if (productId == null || quantityValue == null || quantityValue <= 0L) {
                continue;
            }

            WarehouseProductEntity entity = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            entity.setQuantity(entity.getQuantity() + quantityValue);
            productRepository.save(entity);
        }
    }

    private BookedProductsDto calculateBooking(Map<UUID, Long> products, boolean decreaseQuantity) {
        Map<UUID, Long> missingProducts = new HashMap<>();

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;

        if (products == null || products.isEmpty()) {
            BookedProductsDto dto = new BookedProductsDto();
            dto.setDeliveryWeight(0.0);
            dto.setDeliveryVolume(0.0);
            dto.setFragile(false);
            return dto;
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantityValue = entry.getValue();
            long quantity = quantityValue == null ? 0L : quantityValue;

            WarehouseProductEntity entity = productRepository.findById(productId).orElse(null);
            if (entity == null) {
                missingProducts.put(productId, quantity);
                continue;
            }

            long availableQuantity = entity.getQuantity();
            if (availableQuantity < quantity) {
                missingProducts.put(productId, quantity - availableQuantity);
            }
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouseException(missingProducts);
        }

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantityValue = entry.getValue();
            long quantity = quantityValue == null ? 0L : quantityValue;

            WarehouseProductEntity entity = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            if (decreaseQuantity) {
                entity.setQuantity(entity.getQuantity() - quantity);
                productRepository.save(entity);
            }

            totalWeight += entity.getWeight() * quantity;
            totalVolume += entity.getWidth() * entity.getHeight() * entity.getDepth() * quantity;
            fragile = fragile || entity.isFragile();
        }

        BookedProductsDto dto = new BookedProductsDto();
        dto.setDeliveryWeight(totalWeight);
        dto.setDeliveryVolume(totalVolume);
        dto.setFragile(fragile);
        return dto;
    }
}