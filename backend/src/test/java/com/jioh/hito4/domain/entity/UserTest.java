package com.jioh.hito4.domain.entity;

import com.jioh.hito4.domain.exception.InvalidEmailException;
import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final Instant TS = Instant.EPOCH;
    private static final Email VALID_EMAIL = new Email("jioh@example.com");
    private static final Username VALID_USERNAME = new Username("jioh");

    @Test
    void constructor_succeeds_whenAllFieldsAreValid() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> new User(1, VALID_USERNAME, "pass123", VALID_EMAIL, TS));
    }

    @Test
    void constructor_throws_whenUsernameIsNull() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new User(1, null, "pass123", VALID_EMAIL, TS));
    }

    @Test
    void constructor_throws_whenUsernameIsBlank() {
        // Arrange & Act & Assert
        assertThrows(InvalidUsernameException.class, () -> new User(1, new Username(""), "pass123", VALID_EMAIL, TS));
    }

    @Test
    void constructor_throws_whenPasswordIsNull() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new User(1, VALID_USERNAME, null, VALID_EMAIL, TS));
    }

    @Test
    void constructor_throws_whenPasswordIsBlank() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new User(1, VALID_USERNAME, "", VALID_EMAIL, TS));
    }

    @Test
    void constructor_throws_whenEmailIsNull() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new User(1, VALID_USERNAME, "pass123", null, TS));
    }

    @Test
    void constructor_throws_whenEmailIsBlank() {
        // Arrange & Act & Assert
        assertThrows(InvalidEmailException.class, () -> new User(1, VALID_USERNAME, "pass123", new Email(""), TS));
    }

    @Test
    void equals_returnsTrue_whenIdsMatchEvenIfOtherFieldsDiffer() {
        // Arrange
        User first = new User(1, VALID_USERNAME, "pass123", VALID_EMAIL, TS);
        User second = new User(1, new Username("naty"), "different", new Email("naty@example.com"), Instant.now());

        // Act & Assert
        assertEquals(first, second);
    }

    @Test
    void equals_returnsFalse_whenIdsDiffer() {
        // Arrange
        User first = new User(1, VALID_USERNAME, "pass123", VALID_EMAIL, TS);
        User second = new User(2, VALID_USERNAME, "pass123", VALID_EMAIL, TS);

        // Act & Assert
        assertNotEquals(first, second);
    }

    @Test
    void equals_returnsFalse_whenComparedToDifferentType() {
        // Arrange
        User user = new User(1, VALID_USERNAME, "pass123", VALID_EMAIL, TS);

        // Act & Assert
        assertNotEquals(user, "not a user");
    }

    @Test
    void hashCode_isConsistentForEqualIds() {
        // Arrange
        User first = new User(1, VALID_USERNAME, "pass123", VALID_EMAIL, TS);
        User second = new User(1, new Username("naty"), "different", new Email("naty@example.com"), Instant.now());

        // Act & Assert
        assertEquals(first.hashCode(), second.hashCode());
    }
}
