package ru.yandex.practicum.commerce.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yandex.practicum.commerce.cart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.cart.repo.ShoppingCartRepository;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interactionapi.warehouse.client.WarehouseFeignClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository repo;
    private final WarehouseFeignClient warehouseClient;
    private final TransactionTemplate tx;

    public ShoppingCartService(ShoppingCartRepository repo,
                               WarehouseFeignClient warehouseClient,
                               TransactionTemplate tx) {
        this.repo = repo;
        this.warehouseClient = warehouseClient;
        this.tx = tx;
    }

    public ShoppingCartDto getOrCreate(String username) {
        return tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            return toDto(cart);
        });
    }

    public ShoppingCartDto addProducts(String username, Map<UUID, Long> toAdd) {
        ShoppingCartDto candidate = tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            if (!cart.isActive()) {
                return toDto(cart);
            }

            if (toAdd != null) {
                for (Map.Entry<UUID, Long> e : toAdd.entrySet()) {
                    UUID productId = e.getKey();
                    Long qty = e.getValue();
                    if (productId == null || qty == null || qty <= 0) {
                        continue;
                    }
                    cart.getProducts().merge(productId, qty, Long::sum);
                }
            }

            return toDto(cart);
        });

        warehouseClient.checkProductQuantityEnoughForShoppingCart(candidate);

        return tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            if (!cart.isActive()) {
                return toDto(cart);
            }

            cart.setProducts(new HashMap<>(candidate.getProducts()));
            return toDto(repo.save(cart));
        });
    }

    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest req) {
        ShoppingCartDto candidate = tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            if (!cart.isActive()) {
                return toDto(cart);
            }

            UUID productId = req.getProductId();
            Long newQty = req.getNewQuantity();

            if (!cart.getProducts().containsKey(productId)) {
                throw new NoProductsInShoppingCartException("productId=" + productId + " not present in shopping cart");
            }

            cart.getProducts().put(productId, newQty);
            return toDto(cart);
        });

        warehouseClient.checkProductQuantityEnoughForShoppingCart(candidate);

        return tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            if (!cart.isActive()) {
                return toDto(cart);
            }

            cart.setProducts(new HashMap<>(candidate.getProducts()));
            return toDto(repo.save(cart));
        });
    }

    public ShoppingCartDto remove(String username, List<UUID> ids) {
        return tx.execute(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);

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
                throw new NoProductsInShoppingCartException("none of requested productIds found in shopping cart");
            }

            return toDto(repo.save(cart));
        });
    }

    public void deactivate(String username) {
        tx.executeWithoutResult(status -> {
            ShoppingCartEntity cart = getOrCreateCart(username);
            cart.setActive(false);
            repo.save(cart);
        });
    }

    private ShoppingCartEntity getOrCreateCart(String username) {
        return repo.findByUsername(username).orElseGet(() -> {
            ShoppingCartEntity e = new ShoppingCartEntity();
            e.setUsername(username);
            e.setActive(true);
            return repo.save(e);
        });
    }

    private ShoppingCartDto toDto(ShoppingCartEntity e) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(e.getShoppingCartId());
        dto.setProducts(new HashMap<>(e.getProducts()));
        return dto;
    }
}