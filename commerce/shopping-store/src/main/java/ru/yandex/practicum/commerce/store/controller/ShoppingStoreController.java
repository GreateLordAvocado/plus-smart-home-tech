package ru.yandex.practicum.commerce.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interactionapi.store.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.store.dto.PageProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.PageableObject;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductCategory;
import ru.yandex.practicum.commerce.interactionapi.store.dto.ProductDto;
import ru.yandex.practicum.commerce.interactionapi.store.dto.QuantityState;
import ru.yandex.practicum.commerce.interactionapi.store.dto.SortObject;
import ru.yandex.practicum.commerce.store.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ProductService service;

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
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) {
        return service.setQuantityState(productId, quantityState);
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }

        Sort result = Sort.unsorted();

        for (int i = 0; i < sort.size(); i++) {
            String token = sort.get(i);
            if (token == null || token.isBlank()) {
                continue;
            }

            SortToken parsed = parseSortToken(sort, i);
            if (parsed == null) {
                continue;
            }

            i = parsed.nextIndex;

            result = result.and(Sort.by(parsed.direction, parsed.field));
        }

        return result;
    }

    private SortToken parseSortToken(List<String> sort, int index) {
        String raw = sort.get(index);
        if (raw == null) {
            return null;
        }

        String token = raw.trim();
        if (token.isBlank()) {
            return null;
        }

        if (token.contains(",")) {
            String[] parts = token.split(",", -1);
            String field = parts[0].trim();
            if (field.isBlank()) {
                return null;
            }

            Sort.Direction dir = parseDirection(parts.length > 1 ? parts[1] : null);
            return new SortToken(field, dir, index);
        }

        String field = token;
        if (field.isBlank()) {
            return null;
        }

        Sort.Direction dir = Sort.Direction.ASC;
        int nextIndex = index;

        if (index + 1 < sort.size()) {
            String next = sort.get(index + 1);
            Sort.Direction parsedDir = parseDirection(next);
            if (parsedDir != null) {
                dir = parsedDir;
                nextIndex = index + 1; // съели направление
            }
        }

        return new SortToken(field, dir, nextIndex);
    }

    private Sort.Direction parseDirection(String token) {
        if (token == null) {
            return null;
        }
        String d = token.trim();
        if ("asc".equalsIgnoreCase(d)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equalsIgnoreCase(d)) {
            return Sort.Direction.DESC;
        }
        return null;
    }

    private record SortToken(String field, Sort.Direction direction, int nextIndex) {
    }

    private PageProductDto toPageDto(Page<ProductDto> page) {
        List<SortObject> sortObjects = toSortObjects(page.getSort());

        PageableObject pageableObject = PageableObject.builder()
                .offset(page.getPageable().getOffset())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .paged(page.getPageable().isPaged())
                .unpaged(page.getPageable().isUnpaged())
                .sort(sortObjects)
                .build();

        return PageProductDto.builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .size(page.getSize())
                .number(page.getNumber())
                .numberOfElements(page.getNumberOfElements())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .sort(sortObjects)
                .pageable(pageableObject)
                .build();
    }

    private List<SortObject> toSortObjects(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }

        List<SortObject> out = new ArrayList<>();
        for (Sort.Order o : sort) {
            out.add(SortObject.builder()
                    .property(o.getProperty())
                    .direction(o.getDirection().name())
                    .ascending(o.isAscending())
                    .ignoreCase(o.isIgnoreCase())
                    .nullHandling(o.getNullHandling().name())
                    .build());
        }
        return out;
    }
}