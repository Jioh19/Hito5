package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterUserRequest(
        @Schema(description = "Unique username, at least 4 alphanumeric characters", example = "jioh")
        String username,
        @Schema(description = "Account password", example = "pass123")
        String password,
        @Schema(description = "Unique email address", example = "jioh@example.com")
        String email) {
}
