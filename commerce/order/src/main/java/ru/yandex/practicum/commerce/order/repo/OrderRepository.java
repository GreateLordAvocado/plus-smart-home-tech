package ru.yandex.practicum.commerce.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}