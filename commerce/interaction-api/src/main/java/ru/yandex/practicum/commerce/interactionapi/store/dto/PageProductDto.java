package ru.yandex.practicum.commerce.interactionapi.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageProductDto {

    private List<ProductDto> content;

    private Integer totalPages;

    private Long totalElements;

    private Integer size;

    private Integer number;

    private Integer numberOfElements;

    private Boolean first;

    private Boolean last;

    private Boolean empty;

    private List<SortObject> sort;

    private PageableObject pageable;
}