package com.hellcat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Стандартизированный объект для передачи информации об ошибках через REST API.
 * Это наш официальный протокол о происшествии.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    /**
     * Краткое, понятное для человека, описание ошибки.
     * Это то, что мы покажем пользователю.
     * Например: "Поле 'name' не может быть пустым" или "Фильм с таким ID не найден".
     */
    private String message;

    /**
     * Временная метка, когда произошла эта контрреволюционная вылазка.
     * Полезна для логирования и отладки на стороне клиента.
     */
    private Instant timestamp;

    /**
     * Упрощенный конструктор, который автоматически устанавливает текущее время.
     * @param message Описание ошибки.
     */
    public ErrorDTO(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}
