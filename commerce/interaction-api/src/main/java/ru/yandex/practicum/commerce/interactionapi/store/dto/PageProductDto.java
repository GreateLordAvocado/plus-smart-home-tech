package ru.yandex.practicum.commerce.interactionapi.store.dto;

import java.util.Collections;
import java.util.List;

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

    public PageProductDto() {
    }

    public List<ProductDto> getContent() {
        return content == null ? Collections.emptyList() : content;
    }

    public void setContent(List<ProductDto> content) {
        this.content = content;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }
}