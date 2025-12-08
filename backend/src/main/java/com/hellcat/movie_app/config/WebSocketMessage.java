package com.hellcat.movie_app.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketMessage {
    private String type; // Поле 'type' вместо 'action'
    private Object payload;
}