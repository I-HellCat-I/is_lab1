package com.hellcat.movie_app.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheMonitoringAspect {

    private CacheManager cacheManager;

    // Включаем/отключаем логирование (можно вынести в application.properties)
    private boolean loggingEnabled = true;

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCache(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        // Пытаемся определить имя кэша и ключ (упрощенно)
        // В реальности нужно парсить аннотацию, но для лабы хватит хардкода или простой эвристики
        String cacheName = "persons";
        Object key = joinPoint.getArgs()[0]; // Предполагаем, что первый аргумент - ID

        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null && cache.get(key) != null) {
            log.info("CACHE HIT: Cache '{}', Key '{}'", cacheName, key);
        } else {
            log.info("CACHE MISS: Cache '{}', Key '{}'", cacheName, key);
        }

        return joinPoint.proceed();
    }
}