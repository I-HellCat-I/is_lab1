package com.hellcat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    private List<T> content;        // Содержимое текущей страницы
    private int currentPage;        // Номер текущей страницы (начиная с 0)
    private int pageSize;           // Размер страницы
    private long totalElements;     // Всего элементов в базе по данному запросу
    private int totalPages;         // Всего страниц
}