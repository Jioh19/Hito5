package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(description = "Generated user id", example = "1")
        Integer id,
        @Schema(description = "Username", example = "jioh")
        String username,
        @Schema(description = "Email address", example = "jioh@example.com")
        String email
) {
}
