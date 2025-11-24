package com.hellcat.movie_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Разрешаем все запросы с адреса, где обычно работает React-приложение в режиме разработки
        registry.addMapping("/api/**") // Применяем CORS только к нашему API
                .allowedOrigins("http://localhost:80", "http://localhost:8081") // Добавьте сюда URL вашего фронтенда
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}