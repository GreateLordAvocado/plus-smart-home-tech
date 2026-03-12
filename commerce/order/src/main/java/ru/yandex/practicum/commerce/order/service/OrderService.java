package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.delivery.client.DeliveryFeignClient;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interactionapi.delivery.dto.DeliveryState;
import ru.yandex.practicum.commerce.interactionapi.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderState;
import ru.yandex.practicum.commerce.interactionapi.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.interactionapi.payment.client.PaymentFeignClient;
import ru.yandex.practicum.commerce.interactionapi.payment.dto.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.client.WarehouseFeignClient;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AddressDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repo.OrderRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseFeignClient;
    private final PaymentFeignClient paymentFeignClient;
    private final DeliveryFeignClient deliveryFeignClient;

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        BookedProductsDto bookedProductsDto = warehouseFeignClient
                .checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());

        OrderEntity entity = new OrderEntity();
        entity.setOrderId(UUID.randomUUID());
        entity.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        entity.setProducts(new HashMap<>(request.getShoppingCart().getProducts()));
        entity.setState(OrderState.NEW);
        entity.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        entity.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        entity.setFragile(bookedProductsDto.getFragile());

        applyDeliveryAddress(entity, request.getDeliveryAddress());

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        OrderEntity entity = findOrder(request.getOrderId());

        warehouseFeignClient.acceptReturn(request.getProducts());

        entity.setState(OrderState.PRODUCT_RETURNED);
        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto payment(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        if (OrderState.PAID.equals(orderDto.getState())) {
            entity.setPaymentId(orderDto.getPaymentId());
            entity.setState(OrderState.PAID);
            OrderEntity saved = orderRepository.save(entity);
            return toDto(saved);
        }

        ensureDeliveryPlanned(entity);

        if (entity.getDeliveryPrice() == null) {
            BigDecimal deliveryPrice = deliveryFeignClient.deliveryCost(toDto(entity));
            entity.setDeliveryPrice(deliveryPrice);
        }

        if (entity.getProductPrice() == null) {
            BigDecimal productPrice = paymentFeignClient.productCost(toDto(entity));
            entity.setProductPrice(productPrice);
        }

        if (entity.getTotalPrice() == null) {
            BigDecimal totalPrice = paymentFeignClient.getTotalCost(toDto(entity));
            entity.setTotalPrice(totalPrice);
        }

        PaymentDto paymentDto = paymentFeignClient.payment(toDto(entity));
        entity.setPaymentId(paymentDto.getPaymentId());
        entity.setState(OrderState.ON_PAYMENT);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto paymentFailed(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        if (orderDto.getPaymentId() != null) {
            entity.setPaymentId(orderDto.getPaymentId());
        }
        entity.setState(OrderState.PAYMENT_FAILED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto delivery(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        if (orderDto.getDeliveryId() != null) {
            entity.setDeliveryId(orderDto.getDeliveryId());
        }
        entity.setState(OrderState.DELIVERED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto deliveryFailed(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        if (orderDto.getDeliveryId() != null) {
            entity.setDeliveryId(orderDto.getDeliveryId());
        }
        entity.setState(OrderState.DELIVERY_FAILED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto complete(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());
        entity.setState(OrderState.COMPLETED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto calculateTotalCost(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        ensureDeliveryPlanned(entity);

        if (entity.getDeliveryPrice() == null) {
            BigDecimal deliveryPrice = deliveryFeignClient.deliveryCost(toDto(entity));
            entity.setDeliveryPrice(deliveryPrice);
        }

        BigDecimal productPrice = paymentFeignClient.productCost(toDto(entity));
        entity.setProductPrice(productPrice);

        BigDecimal totalPrice = paymentFeignClient.getTotalCost(toDto(entity));
        entity.setTotalPrice(totalPrice);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto calculateDeliveryCost(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        ensureDeliveryPlanned(entity);

        BigDecimal deliveryPrice = deliveryFeignClient.deliveryCost(toDto(entity));
        entity.setDeliveryPrice(deliveryPrice);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto assembly(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());

        if (orderDto.getDeliveryId() != null && orderDto.getProducts() == null) {
            entity.setDeliveryId(orderDto.getDeliveryId());
            entity.setState(OrderState.ASSEMBLED);
            OrderEntity saved = orderRepository.save(entity);
            return toDto(saved);
        }

        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest();
        request.setOrderId(entity.getOrderId());
        request.setProducts(new HashMap<>(entity.getProducts()));

        BookedProductsDto bookedProductsDto = warehouseFeignClient.assemblyProductsForOrder(request);

        entity.setDeliveryWeight(bookedProductsDto.getDeliveryWeight());
        entity.setDeliveryVolume(bookedProductsDto.getDeliveryVolume());
        entity.setFragile(bookedProductsDto.getFragile());
        entity.setState(OrderState.ASSEMBLED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public OrderDto assemblyFailed(OrderDto orderDto) {
        OrderEntity entity = findOrder(orderDto.getOrderId());
        entity.setState(OrderState.ASSEMBLY_FAILED);

        OrderEntity saved = orderRepository.save(entity);
        return toDto(saved);
    }

    private OrderEntity findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private void ensureDeliveryPlanned(OrderEntity entity) {
        if (entity.getDeliveryId() != null) {
            return;
        }

        AddressDto warehouseAddress = warehouseFeignClient.getWarehouseAddress();

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setOrderId(entity.getOrderId());
        deliveryDto.setFromAddress(warehouseAddress);
        deliveryDto.setToAddress(extractDeliveryAddress(entity));
        deliveryDto.setDeliveryState(DeliveryState.CREATED);

        DeliveryDto savedDelivery = deliveryFeignClient.planDelivery(deliveryDto);
        entity.setDeliveryId(savedDelivery.getDeliveryId());
    }

    private void applyDeliveryAddress(OrderEntity entity, AddressDto addressDto) {
        if (addressDto == null) {
            return;
        }

        entity.setDeliveryCountry(addressDto.getCountry());
        entity.setDeliveryCity(addressDto.getCity());
        entity.setDeliveryStreet(addressDto.getStreet());
        entity.setDeliveryHouse(addressDto.getHouse());
        entity.setDeliveryFlat(addressDto.getFlat());
    }

    private AddressDto extractDeliveryAddress(OrderEntity entity) {
        AddressDto addressDto = new AddressDto();
        addressDto.setCountry(entity.getDeliveryCountry());
        addressDto.setCity(entity.getDeliveryCity());
        addressDto.setStreet(entity.getDeliveryStreet());
        addressDto.setHouse(entity.getDeliveryHouse());
        addressDto.setFlat(entity.getDeliveryFlat());
        return addressDto;
    }

    private OrderDto toDto(OrderEntity entity) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(entity.getOrderId());
        dto.setShoppingCartId(entity.getShoppingCartId());
        dto.setProducts(new HashMap<>(entity.getProducts()));
        dto.setPaymentId(entity.getPaymentId());
        dto.setDeliveryId(entity.getDeliveryId());
        dto.setState(entity.getState());
        dto.setDeliveryWeight(entity.getDeliveryWeight());
        dto.setDeliveryVolume(entity.getDeliveryVolume());
        dto.setFragile(entity.getFragile());
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setDeliveryPrice(entity.getDeliveryPrice());
        dto.setProductPrice(entity.getProductPrice());
        dto.setDeliveryAddress(extractDeliveryAddress(entity));
        return dto;
    }
}