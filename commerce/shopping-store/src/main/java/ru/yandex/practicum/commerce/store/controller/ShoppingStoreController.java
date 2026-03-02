package ru.yandex.practicum.commerce.store.controller;

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
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) {
        return service.setQuantityState(productId, quantityState);
    }

    private Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }

        List<String> normalized = normalizeSort(sort);

        Sort result = Sort.unsorted();
        for (String raw : normalized) {
            if (raw == null || raw.isBlank()) continue;

            String[] parts = raw.split(",", -1);
            String field = parts[0].trim();
            if (field.isBlank()) continue;

            Sort.Direction dir = Sort.Direction.ASC;
            if (parts.length > 1) {
                String d = parts[1].trim();
                if ("desc".equalsIgnoreCase(d)) dir = Sort.Direction.DESC;
                if ("asc".equalsIgnoreCase(d)) dir = Sort.Direction.ASC;
            }

            result = result.and(Sort.by(dir, field));
        }
        return result;
    }

    private List<String> normalizeSort(List<String> sort) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < sort.size()) {
            String a = sort.get(i);
            if (a == null || a.isBlank()) {
                i++;
                continue;
            }
            String aa = a.trim();

            if (aa.contains(",")) {
                out.add(aa);
                i++;
                continue;
            }

            if (i + 1 < sort.size()) {
                String b = sort.get(i + 1);
                if (b != null) {
                    String bb = b.trim();
                    if ("asc".equalsIgnoreCase(bb) || "desc".equalsIgnoreCase(bb)) {
                        out.add(aa + "," + bb);
                        i += 2;
                        continue;
                    }
                }
            }

            out.add(aa);
            i++;
        }
        return out;
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

        List<SortObject> sortObjects = toSortObjects(page.getSort());
        dto.setSort(sortObjects);

        PageableObject pageableObject = new PageableObject();
        pageableObject.setOffset(page.getPageable().getOffset());
        pageableObject.setPageNumber(page.getNumber());
        pageableObject.setPageSize(page.getSize());
        pageableObject.setPaged(page.getPageable().isPaged());
        pageableObject.setUnpaged(page.getPageable().isUnpaged());
        pageableObject.setSort(sortObjects);
        dto.setPageable(pageableObject);

        return dto;
    }

    private List<SortObject> toSortObjects(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return List.of();
        }

        List<SortObject> out = new ArrayList<>();
        for (Sort.Order o : sort) {
            SortObject so = new SortObject();
            so.setProperty(o.getProperty());
            so.setDirection(o.getDirection().name());
            so.setAscending(o.isAscending());
            so.setIgnoreCase(o.isIgnoreCase());
            so.setNullHandling(o.getNullHandling().name());
            out.add(so);
        }
        return out;
    }
}