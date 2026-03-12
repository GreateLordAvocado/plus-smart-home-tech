package ru.yandex.practicum.commerce.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.order.client.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderService orderService;

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return orderService.getClientOrders(username);
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    @Override
    public OrderDto payment(OrderDto orderDto) {
        return orderService.payment(orderDto);
    }

    @Override
    public OrderDto paymentFailed(OrderDto orderDto) {
        return orderService.paymentFailed(orderDto);
    }

    @Override
    public OrderDto delivery(OrderDto orderDto) {
        return orderService.delivery(orderDto);
    }

    @Override
    public OrderDto deliveryFailed(OrderDto orderDto) {
        return orderService.deliveryFailed(orderDto);
    }

    @Override
    public OrderDto complete(OrderDto orderDto) {
        return orderService.complete(orderDto);
    }

    @Override
    public OrderDto calculateTotalCost(OrderDto orderDto) {
        return orderService.calculateTotalCost(orderDto);
    }

    @Override
    public OrderDto calculateDeliveryCost(OrderDto orderDto) {
        return orderService.calculateDeliveryCost(orderDto);
    }

    @Override
    public OrderDto assembly(OrderDto orderDto) {
        return orderService.assembly(orderDto);
    }

    @Override
    public OrderDto assemblyFailed(OrderDto orderDto) {
        return orderService.assemblyFailed(orderDto);
    }
}