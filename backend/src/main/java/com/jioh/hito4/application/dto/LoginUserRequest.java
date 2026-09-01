package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginUserRequest(
        @Schema(description = "Username to authenticate", example = "jioh")
        String username,
        @Schema(description = "Account password", example = "pass123")
        String password
) {
}
