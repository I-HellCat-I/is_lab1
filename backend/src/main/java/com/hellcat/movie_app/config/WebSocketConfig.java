package com.hellcat.movie_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Включаем простой брокер сообщений, который будет отправлять сообщения клиентам по адресам, начинающимся с /topic
        config.enableSimpleBroker("/topic");
        // Адреса, на которые клиенты будут отправлять сообщения на сервер, будут начинаться с /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Меняем эндпоинт на '/notifications' и разрешаем все источники
        registry.addEndpoint("/notifications").setAllowedOriginPatterns("*");
    }
}