package ru.yandex.practicum.commerce.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.cart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.cart.repo.ShoppingCartRepository;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.client.WarehouseFeignClient;

import java.util.*;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository repo;
    private final WarehouseFeignClient warehouseClient;

    public ShoppingCartService(ShoppingCartRepository repo, WarehouseFeignClient warehouseClient) {
        this.repo = repo;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public ShoppingCartDto getOrCreate(String username) {
        validateUsername(username);

        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        return toDto(cart);
    }

    public ShoppingCartDto addProducts(String username, Map<UUID, Long> toAdd) {
        validateUsername(username);

        ShoppingCartDto candidate = prepareAddProducts(username, toAdd);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(candidate);

        return applyProducts(username, candidate.getProducts());
    }

    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest req) {
        validateUsername(username);

        ShoppingCartDto candidate = prepareChangeQuantity(username, req);

        warehouseClient.checkProductQuantityEnoughForShoppingCart(candidate);

        return applyProducts(username, candidate.getProducts());
    }

    @Transactional
    public ShoppingCartDto remove(String username, List<UUID> ids) {
        validateUsername(username);

        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        if (!cart.isActive()) {
            return toDto(cart);
        }

        if (ids == null || ids.isEmpty()) {
            return toDto(cart);
        }

        boolean removedAny = false;
        for (UUID id : ids) {
            if (cart.getProducts().remove(id) != null) {
                removedAny = true;
            }
        }

        if (!removedAny) {
            throw new NoProductsInShoppingCartException("No requested products found in shopping cart");
        }

        return toDto(repo.save(cart));
    }

    @Transactional
    public void deactivate(String username) {
        validateUsername(username);

        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        cart.setActive(false);
        repo.save(cart);
    }

    @Transactional
    protected ShoppingCartDto prepareAddProducts(String username, Map<UUID, Long> toAdd) {
        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        if (!cart.isActive()) {
            return toDto(cart);
        }

        if (toAdd != null) {
            for (Map.Entry<UUID, Long> e : toAdd.entrySet()) {
                UUID productId = e.getKey();
                Long qty = e.getValue();
                if (productId == null || qty == null || qty <= 0) continue;

                cart.getProducts().merge(productId, qty, Long::sum);
            }
        }

        return toDto(cart);
    }

    @Transactional
    protected ShoppingCartDto prepareChangeQuantity(String username, ChangeProductQuantityRequest req) {
        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        if (!cart.isActive()) {
            return toDto(cart);
        }

        UUID productId = req.getProductId();
        Long newQty = req.getNewQuantity();

        if (!cart.getProducts().containsKey(productId)) {
            throw new NoProductsInShoppingCartException("No such product in shopping cart: " + productId);
        }

        cart.getProducts().put(productId, newQty);

        return toDto(cart);
    }

    @Transactional
    protected ShoppingCartDto applyProducts(String username, Map<UUID, Long> products) {
        ShoppingCartEntity cart = repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });

        if (!cart.isActive()) {
            return toDto(cart);
        }

        cart.setProducts(new HashMap<>(products));
        return toDto(repo.save(cart));
    }

    private void validateUsername(String username) {
        if (username == null) {
            throw new NotAuthorizedUserException("Username is null");
        }
        if (username.isBlank()) {
            throw new NotAuthorizedUserException("Username is blank");
        }
    }

    private ShoppingCartDto toDto(ShoppingCartEntity e) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(e.getShoppingCartId());
        dto.setProducts(new HashMap<>(e.getProducts()));
        return dto;
    }
}