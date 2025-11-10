package com.hellcat.backend.dto;

import com.hellcat.backend.model.Color;
import com.hellcat.backend.model.Country;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PersonDTO {
    private Integer id;
    private String name;
    private Color eyeColor;
    private Color hairColor;
    private LocationDTO location;
    private Long weight;
    private Country nationality;

    public void setLocationName(String name) {
        location.setName(name);
    }
}