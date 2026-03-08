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
public class PageableObject {

    private Long offset;

    private List<SortObject> sort;

    private Boolean unpaged;

    private Boolean paged;

    private Integer pageNumber;

    private Integer pageSize;
}