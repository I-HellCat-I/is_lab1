package com.hellcat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Удобно для создания объекта в одну строку
public class DirectorGroupDTO {
    private String directorName;
    private Long movieCount;
}