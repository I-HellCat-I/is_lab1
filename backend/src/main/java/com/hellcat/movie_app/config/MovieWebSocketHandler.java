package com.hellcat.movie_app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    // Список для хранения всех активных сессий (пользователей)
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("Новое WebSocket соединение: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket соединение закрыто: {}", session.getId());
    }

    // Метод для рассылки сообщений всем
    public void broadcast(Object payload) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(jsonMessage);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    synchronized (session) {
                        try {
                            session.sendMessage(message);
                        } catch (Exception e) {
                            log.error("Failed to send message to session {}", session.getId(), e);
                        }
                    }
                    // -----------------------------
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при рассылке WebSocket сообщения", e);
        }
    }
}