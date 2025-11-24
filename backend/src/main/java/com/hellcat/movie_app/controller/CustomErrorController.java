package com.hellcat.movie_app.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        // Получаем статус ошибки (404, 500 и т.д.)
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; // По умолчанию 500
        if (status instanceof Integer) {
            try {
                httpStatus = HttpStatus.valueOf((Integer) status);
            } catch (Exception ex) {
                // Игнорируем, если код статуса некорректный
            }
        }

        // Формируем стандартный ответ
        String message;
        if (httpStatus == HttpStatus.NOT_FOUND) {
            message = "Запрошенный ресурс не найден.";
        } else {
            // Пытаемся получить сообщение об ошибке, если оно есть
            Object errorMessage = request.getAttribute("jakarta.servlet.error.message");
            message = (errorMessage != null) ? errorMessage.toString() : "Произошла внутренняя ошибка сервера.";
        }

        Map<String, Object> body = Map.of(
                "status", httpStatus.value(),
                "error", httpStatus.getReasonPhrase(),
                "message", message,
                "path", request.getAttribute("jakarta.servlet.error.request_uri")
        );

        return new ResponseEntity<>(body, httpStatus);
    }
}
