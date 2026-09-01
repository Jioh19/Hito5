package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidUsernameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsernameTest {

    @Test
    void constructor_succeeds_whenFormatIsValid() {
        // Arrange & Act
        Username username = new Username("jioh");

        // Assert
        assertNotNull(username);
        assertEquals("jioh", username.value());
    }

    @Test
    void constructor_trimsAndLowercases_whenGivenPaddedMixedCaseInput() {
        // Arrange & Act
        Username username = new Username("  Jioh  ");

        // Assert
        assertEquals("jioh", username.value());
    }

    @Test
    void constructor_succeeds_whenValueContainsDigits() {
        // Arrange & Act
        Username username = new Username("jioh1");

        // Assert
        assertEquals("jioh1", username.value());
    }

    @Test
    void constructor_throws_whenShorterThanFourCharacters() {
        // Arrange & Act & Assert
        assertThrows(InvalidUsernameException.class, () -> new Username("abc"));
    }

    @Test
    void constructor_throws_whenContainsSpecialCharacters() {
        // Arrange & Act & Assert
        assertThrows(InvalidUsernameException.class, () -> new Username("jioh!"));
    }

    @Test
    void constructor_throws_whenValueIsNull() {
        // Arrange & Act & Assert
        assertThrows(InvalidUsernameException.class, () -> new Username(null));
    }

    @Test
    void constructor_throws_whenValueIsBlank() {
        // Arrange & Act & Assert
        assertThrows(InvalidUsernameException.class, () -> new Username(""));
    }
}
