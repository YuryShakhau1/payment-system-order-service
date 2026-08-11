package by.shakhau.ps.order.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY) // Скроет поле errors, если оно пустое
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path) {
}
