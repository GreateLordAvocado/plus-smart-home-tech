package ru.yandex.practicum.commerce.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderDto> getClientOrders(@RequestParam("username") String username) {
        return orderService.getClientOrders(username);
    }

    @PutMapping
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request) {
        return orderService.createNewOrder(request);
    }

    @PostMapping("/return")
    public OrderDto productReturn(@RequestBody ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    @PostMapping("/payment")
    public OrderDto payment(@RequestBody OrderDto orderDto) {
        return orderService.payment(orderDto);
    }

    @PostMapping("/payment/failed")
    public OrderDto paymentFailed(@RequestBody OrderDto orderDto) {
        return orderService.paymentFailed(orderDto);
    }

    @PostMapping("/delivery")
    public OrderDto delivery(@RequestBody OrderDto orderDto) {
        return orderService.delivery(orderDto);
    }

    @PostMapping("/delivery/failed")
    public OrderDto deliveryFailed(@RequestBody OrderDto orderDto) {
        return orderService.deliveryFailed(orderDto);
    }

    @PostMapping("/completed")
    public OrderDto complete(@RequestBody OrderDto orderDto) {
        return orderService.complete(orderDto);
    }

    @PostMapping("/calculate/total")
    public OrderDto calculateTotalCost(@RequestBody OrderDto orderDto) {
        return orderService.calculateTotalCost(orderDto);
    }

    @PostMapping("/calculate/delivery")
    public OrderDto calculateDeliveryCost(@RequestBody OrderDto orderDto) {
        return orderService.calculateDeliveryCost(orderDto);
    }

    @PostMapping("/assembly")
    public OrderDto assembly(@RequestBody OrderDto orderDto) {
        return orderService.assembly(orderDto);
    }

    @PostMapping("/assembly/failed")
    public OrderDto assemblyFailed(@RequestBody OrderDto orderDto) {
        return orderService.assemblyFailed(orderDto);
    }
}