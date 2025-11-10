package com.hellcat.backend.util;

import com.hellcat.backend.dto.ErrorDTO;
import jakarta.validation.ConstraintViolationException; // Из Jakarta Bean Validation
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Собираем все ошибки валидации в одну понятную строку.
        String errorMessage = exception.getConstraintViolations().stream()
                .map(violation -> String.format("Поле '%s': %s",
                        violation.getPropertyPath(), violation.getMessage()))
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Неизвестная ошибка валидации.");

        // Создаем наш стандартизированный объект ошибки.
        ErrorDTO errorDTO = new ErrorDTO(errorMessage);

        // Возвращаем 400 Bad Request с JSON-телом в формате ErrorDTO.
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorDTO)
                .type(MediaType.APPLICATION_JSON) // Явно указываем тип контента
                .build();
    }
}