package ru.yandex.practicum.commerce.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductState;
import ru.yandex.practicum.commerce.interactionapi.store.dto.QuantityState;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.mapper.ProductMapper;
import ru.yandex.practicum.commerce.store.model.ProductEntity;
import ru.yandex.practicum.commerce.store.repo.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    public Page<ProductDto> findActiveByCategory(ProductCategory category, Pageable pageable) {
        return repo.findByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable)
                .map(mapper::toDto);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        ProductEntity entity = mapper.toEntity(dto);
        if (entity.getProductState() == null) {
            entity.setProductState(ProductState.ACTIVE);
        }
        ProductEntity saved = repo.save(entity);
        return mapper.toDto(saved);
    }

    @Transactional
    public ProductDto update(ProductDto dto) {
        UUID id = dto.getProductId();
        if (id == null) {
            throw new IllegalArgumentException("productId must be provided for update");
        }

        ProductEntity entity = repo.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        mapper.updateEntity(entity, dto);
        return mapper.toDto(repo.save(entity));
    }

    @Transactional(readOnly = true)
    public ProductDto get(UUID productId) {
        ProductEntity entity = repo.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        return mapper.toDto(entity);
    }

    @Transactional
    public boolean deactivate(UUID productId) {
        ProductEntity entity = repo.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        entity.setProductState(ProductState.DEACTIVATE);
        repo.save(entity);
        return true;
    }

    @Transactional
    public boolean setQuantityState(UUID productId, QuantityState state) {
        ProductEntity entity = repo.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        entity.setQuantityState(state);
        repo.save(entity);
        return true;
    }
}