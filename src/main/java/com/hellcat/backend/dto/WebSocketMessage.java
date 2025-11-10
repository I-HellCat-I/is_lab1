package com.hellcat.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {
    private MessageType type;
    private T payload; // Полезная нагрузка

    /**
     * Типы приказов, которые наш ЦК (бэкенд) может отправлять на места (клиентам).
     */
    public enum MessageType {
        /**
         * Создан новый объект. В payload - полный DTO нового объекта.
         */
        CREATED,

        /**
         * Объект был обновлен. В payload - полный, свежий DTO объекта.
         */
        UPDATED,

        /**
         * Объект был удален. В payload - объект, содержащий ID удаленного объекта,
         * например, Map.of("id", 123L).
         */
        DELETED,

        /**
         * Произошла массовая операция обновления (например, награждение Оскарами).
         * Клиенту рекомендуется полностью перезапросить данные.
         * В payload - строка с описанием операции.
         */
        BULK_UPDATE,

        /**
         * Произошла массовая операция удаления.
         * Клиенту рекомендуется полностью перезапросить данные.
         * В payload - строка с описанием операции.
         */
        BULK_DELETE,

        /**
         * Простое информационное сообщение или ошибка для отображения пользователю.
         * В payload - строка с сообщением.
         */
        INFO
    }
}