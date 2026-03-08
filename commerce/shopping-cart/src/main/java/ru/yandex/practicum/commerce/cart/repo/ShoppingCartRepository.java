package ru.yandex.practicum.commerce.cart.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, UUID> {
    Optional<ShoppingCartEntity> findByUsername(String username);
}