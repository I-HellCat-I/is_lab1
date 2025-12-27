package com.hellcat.movie_app.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching(order = 2)
public class CacheConfig {
}