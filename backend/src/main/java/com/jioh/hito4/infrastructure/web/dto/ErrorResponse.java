package com.jioh.hito4.infrastructure.web.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String code,
        LocalDateTime timestamp
) {
}
