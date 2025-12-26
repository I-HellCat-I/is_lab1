package com.hellcat.movie_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportTaskDto {
    private Long historyId;
    private String fileName;
    private List<MovieDto> movies; // Батч фильмов
}