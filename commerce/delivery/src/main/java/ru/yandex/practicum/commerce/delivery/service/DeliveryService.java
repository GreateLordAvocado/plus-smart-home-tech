package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repo.DeliveryRepository;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.order.client.OrderFeignClient;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderState;
import ru.yandex.practicum.commerce.interactionapi.warehouse.client.WarehouseFeignClient;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final BigDecimal BASE_COST = new BigDecimal("5.0");
    private static final BigDecimal ADDRESS_2_MULTIPLIER = new BigDecimal("2.0");
    private static final BigDecimal FRAGILE_RATE = new BigDecimal("0.2");
    private static final BigDecimal WEIGHT_RATE = new BigDecimal("0.3");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.2");
    private static final BigDecimal DIFFERENT_STREET_RATE = new BigDecimal("0.2");

    private final DeliveryRepository deliveryRepository;
    private final OrderFeignClient orderFeignClient;
    private final WarehouseFeignClient warehouseFeignClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        DeliveryEntity entity = new DeliveryEntity();
        entity.setDeliveryId(UUID.randomUUID());
        entity.setOrderId(deliveryDto.getOrderId());
        entity.setFromAddress(deliveryDto.getFromAddress());
        entity.setToAddress(deliveryDto.getToAddress());
        entity.setDeliveryState(DeliveryState.CREATED);

        DeliveryEntity saved = deliveryRepository.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto orderDto) {
        BigDecimal total = BASE_COST;

        AddressDto warehouseAddress = warehouseFeignClient.getWarehouseAddress();
        AddressDto deliveryAddress = orderDto.getDeliveryAddress();

        if (warehouseAddress != null
                && warehouseAddress.getStreet() != null
                && warehouseAddress.getStreet().contains("ADDRESS_2")) {
            total = total.add(BASE_COST.multiply(ADDRESS_2_MULTIPLIER));
        } else {
            total = total.add(BASE_COST);
        }

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            total = total.add(total.multiply(FRAGILE_RATE));
        }

        double weight = orderDto.getDeliveryWeight() == null ? 0.0 : orderDto.getDeliveryWeight();
        double volume = orderDto.getDeliveryVolume() == null ? 0.0 : orderDto.getDeliveryVolume();

        total = total.add(BigDecimal.valueOf(weight).multiply(WEIGHT_RATE));
        total = total.add(BigDecimal.valueOf(volume).multiply(VOLUME_RATE));

        if (!isSameStreet(warehouseAddress, deliveryAddress)) {
            total = total.add(total.multiply(DIFFERENT_STREET_RATE));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void deliveryPicked(DeliveryDto deliveryDto) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryDto.getDeliveryId())
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryDto.getDeliveryId()));

        entity.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(entity);

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(entity.getOrderId());
        request.setDeliveryId(entity.getDeliveryId());
        warehouseFeignClient.shippedToDelivery(request);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(entity.getOrderId());
        orderDto.setDeliveryId(entity.getDeliveryId());
        orderDto.setState(OrderState.ASSEMBLED);
        orderFeignClient.assembly(orderDto);
    }

    @Transactional
    public void deliverySuccessful(DeliveryDto deliveryDto) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryDto.getDeliveryId())
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryDto.getDeliveryId()));

        entity.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(entity);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(entity.getOrderId());
        orderDto.setDeliveryId(entity.getDeliveryId());
        orderDto.setState(OrderState.DELIVERED);
        orderFeignClient.delivery(orderDto);
    }

    @Transactional
    public void deliveryFailed(DeliveryDto deliveryDto) {
        DeliveryEntity entity = deliveryRepository.findById(deliveryDto.getDeliveryId())
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryDto.getDeliveryId()));

        entity.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(entity);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(entity.getOrderId());
        orderDto.setDeliveryId(entity.getDeliveryId());
        orderDto.setState(OrderState.DELIVERY_FAILED);
        orderFeignClient.deliveryFailed(orderDto);
    }

    private boolean isSameStreet(AddressDto from, AddressDto to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.getStreet() == null || to.getStreet() == null) {
            return false;
        }
        return from.getStreet().equalsIgnoreCase(to.getStreet());
    }

    private DeliveryDto toDto(DeliveryEntity entity) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(entity.getDeliveryId());
        dto.setOrderId(entity.getOrderId());
        dto.setFromAddress(entity.getFromAddress());
        dto.setToAddress(entity.getToAddress());
        dto.setDeliveryState(entity.getDeliveryState());
        return dto;
    }
}