package ru.yandex.practicum.commerce.store.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.store.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.store.dto.PageProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.store.service.ProductService;

import java.util.List;
import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ProductService service;

    public ShoppingStoreController(ProductService service) {
        this.service = service;
    }

    @Override
    public PageProductDto getProducts(ProductCategory category, Integer page, Integer size, List<String> sort) {
        int p = page == null ? 0 : page;
        int s = size == null ? 10 : size;

        Sort springSort = parseSort(sort);
        PageRequest pageable = PageRequest.of(p, s, springSort);

        Page<ProductDto> result = service.findActiveByCategory(category, pageable);
        return toPageDto(result);
    }

    @Override
    public ProductDto createProduct(ProductDto product) {
        return service.create(product);
    }

    @Override
    public ProductDto updateProduct(ProductDto product) {
        return service.update(product);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return service.get(productId);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        return service.deactivate(productId);
    }

    @Override
    public Boolean setQuantityState(SetProductQuantityStateRequest request) {
        return service.setQuantityState(request.getProductId(), request.getQuantityState());
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }

        // OpenAPI подразумевает sort как массив строк, часто вида: "field,asc"
        Sort result = Sort.unsorted();
        for (String s : sort) {
            if (s == null || s.isBlank()) continue;

            String[] parts = s.split(",");
            String field = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            Sort one = Sort.by(dir, field);
            result = result.and(one);
        }
        return result;
    }

    private PageProductDto toPageDto(Page<ProductDto> page) {
        PageProductDto dto = new PageProductDto();
        dto.setContent(page.getContent());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        dto.setSize(page.getSize());
        dto.setNumber(page.getNumber());
        dto.setNumberOfElements(page.getNumberOfElements());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setEmpty(page.isEmpty());
        return dto;
    }
}