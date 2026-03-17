package ru.yandex.practicum.commerce.interactionapi.order.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interactionapi.order.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interactionapi.order.dto.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.order.dto.ProductReturnRequest;

import java.util.List;

public interface OrderClient {

    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam("username") String username);

    @PutMapping
    OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request);

    @PostMapping("/return")
    OrderDto productReturn(@RequestBody ProductReturnRequest request);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody OrderDto orderDto);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody OrderDto orderDto);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody OrderDto orderDto);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody OrderDto orderDto);

    @PostMapping("/completed")
    OrderDto complete(@RequestBody OrderDto orderDto);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody OrderDto orderDto);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody OrderDto orderDto);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody OrderDto orderDto);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody OrderDto orderDto);
}