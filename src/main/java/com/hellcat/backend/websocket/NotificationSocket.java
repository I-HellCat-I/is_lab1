package com.hellcat.backend.websocket;

import com.hellcat.backend.dto.WebSocketMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@ApplicationScoped
// ОБНОВЛЕНИЕ: Подключаем наш кодировщик
@ServerEndpoint(value = "/notifications", encoders = {MessageEncoder.class})
public class NotificationSocket {

    private final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Новый товарищ на связи! ID сессии: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Товарищ отключился. ID сессии: " + session.getId());
    }

    /**
     * Отправляет структурированное сообщение ВСЕМ подключенным клиентам.
     * @param message Объект WebSocketMessage для трансляции.
     */
    public void broadcast(WebSocketMessage<?> message) {
        System.out.println("Транслирую сообщение: тип=" + message.getType());

        sessions.forEach(session -> {
            synchronized (session) {
                try {
                    // Теперь мы отправляем объект, а кодер сам превратит его в JSON
                    session.getBasicRemote().sendObject(message);
                } catch (Exception e) {
                    System.err.println("Блядь! Не могу отправить объект клиенту " + session.getId() + ": " + e.getMessage());
                }
            }
        });
    }
}