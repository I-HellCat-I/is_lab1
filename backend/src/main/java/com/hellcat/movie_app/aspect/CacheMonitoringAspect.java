package com.hellcat.movie_app.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class CacheMonitoringAspect {

    private final CacheManager cacheManager;
    private final boolean loggingEnabled = true;

    /**
     * Перехватываем любой метод, помеченный @Cacheable.
     * Привязываем саму аннотацию к переменной cacheableAnnotation, чтобы читать её свойства.
     */
    @Around("@annotation(cacheableAnnotation)")
    public Object monitorCache(ProceedingJoinPoint joinPoint, Cacheable cacheableAnnotation) throws Throwable {
        if (!loggingEnabled) {
            return joinPoint.proceed();
        }

        // 1. Получаем имена кэшей из аннотации (например, "persons" или "locations")
        String[] cacheNames = cacheableAnnotation.value();
        if (cacheNames.length == 0) {
            cacheNames = cacheableAnnotation.cacheNames();
        }

        // 2. Определяем ключ кэширования.
        // В простых случаях (findById) ключом является первый аргумент метода.
        Object[] args = joinPoint.getArgs();
        Object key = (args.length > 0) ? args[0] : "SimpleKey[]";

        boolean isHit = false;

        // 3. Проверяем наличие значения в реальном кэше Ehcache
        if (cacheNames.length > 0) {
            Cache cache = cacheManager.getCache(cacheNames[0]);

            // Если кэш существует и в нем есть значение по этому ключу -> HIT
            if (cache != null && cache.get(key) != null) {
                isHit = true;
            }
        }

        // 4. Логируем результат
        String cachesString = Arrays.toString(cacheNames);
        String methodName = joinPoint.getSignature().toShortString();

        if (isHit) {
            log.info("🟢 CACHE HIT  | Cache: {} | Key: {} | Method: {}", cachesString, key, methodName);
        } else {
            log.info("🔴 CACHE MISS | Cache: {} | Key: {} | Method: {}", cachesString, key, methodName);
        }

        // 5. Выполняем метод (если был MISS, Spring сам запишет результат в кэш после возврата)
        return joinPoint.proceed();
    }
}