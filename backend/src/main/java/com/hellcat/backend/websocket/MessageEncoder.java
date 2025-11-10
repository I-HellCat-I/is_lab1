package com.hellcat.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import com.hellcat.backend.dto.WebSocketMessage;

public class MessageEncoder implements Encoder.Text<WebSocketMessage> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String encode(WebSocketMessage message) throws EncodeException {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            throw new EncodeException(message, "Ошибка кодирования WebSocket сообщения в JSON", e);
        }
    }
}