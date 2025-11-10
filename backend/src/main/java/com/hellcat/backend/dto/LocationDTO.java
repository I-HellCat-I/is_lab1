package com.hellcat.backend.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationDTO {
    private Long id;
    private float x;
    private Double y;
    private String name;
}
