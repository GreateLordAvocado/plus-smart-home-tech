package ru.yandex.practicum.commerce.interactionapi.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortObject {

    private String direction;

    private String nullHandling;

    private Boolean ascending;

    private String property;

    private Boolean ignoreCase;
}