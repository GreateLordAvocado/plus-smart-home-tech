package ru.yandex.practicum.commerce.store.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.store.model.ProductEntity;

@Component
public class ProductMapper {

    public ProductDto toDto(ProductEntity e) {
        ProductDto dto = new ProductDto();
        dto.setProductId(e.getProductId());
        dto.setProductName(e.getProductName());
        dto.setDescription(e.getDescription());
        dto.setImageSrc(e.getImageSrc());
        dto.setQuantityState(e.getQuantityState());
        dto.setProductState(e.getProductState());
        dto.setProductCategory(e.getProductCategory());
        dto.setPrice(e.getPrice());
        return dto;
    }

    public ProductEntity toEntity(ProductDto dto) {
        ProductEntity e = new ProductEntity();
        e.setProductId(dto.getProductId());
        e.setProductName(dto.getProductName());
        e.setDescription(dto.getDescription());
        e.setImageSrc(dto.getImageSrc());
        e.setQuantityState(dto.getQuantityState());
        e.setProductState(dto.getProductState());
        e.setProductCategory(dto.getProductCategory());
        e.setPrice(dto.getPrice());
        return e;
    }

    public void updateEntity(ProductEntity e, ProductDto dto) {
        e.setProductName(dto.getProductName());
        e.setDescription(dto.getDescription());
        e.setImageSrc(dto.getImageSrc());
        e.setQuantityState(dto.getQuantityState());
        e.setProductState(dto.getProductState());
        e.setProductCategory(dto.getProductCategory());
        e.setPrice(dto.getPrice());
    }
}