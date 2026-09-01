package com.jioh.hito4.domain.service;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationPolicyTest {

    private final AuthenticationPolicy authenticationPolicy = new AuthenticationPolicy();

    @Test
    void authenticate_throwsUserNotFoundException_whenUserIsNull() {
        // Arrange & Act & Assert
        assertThrows(UserNotFoundException.class, () -> authenticationPolicy.authenticate(null, "pass123"));
    }

    @Test
    void authenticate_throwsUserNotFoundException_whenPasswordDoesNotMatch() {
        // Arrange
        User user = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> authenticationPolicy.authenticate(user, "wrongpass"));
    }

    @Test
    void authenticate_doesNotThrow_whenCredentialsAreValid() {
        // Arrange
        User user = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);

        // Act & Assert
        assertDoesNotThrow(() -> authenticationPolicy.authenticate(user, "pass123"));
    }
}
