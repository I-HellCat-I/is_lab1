package com.hellcat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialOpResultDTO {
    private String message;
    private Object payload; // Сюда можно класть любой результат, если он есть

    public SpecialOpResultDTO(String message) {
        this.message = message;
    }
}