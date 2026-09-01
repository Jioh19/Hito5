package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailTest {

    @Test
    void constructor_succeeds_whenFormatIsValid() {
        // Arrange & Act
        Email email = new Email("jioh@example.com");

        // Assert
        assertNotNull(email);
        assertEquals("jioh@example.com", email.value());
    }

    @Test
    void constructor_trimsAndLowercases_whenGivenPaddedMixedCaseInput() {
        // Arrange & Act
        Email email = new Email("  Jioh@Example.Com  ");

        // Assert
        assertEquals("jioh@example.com", email.value());
    }

    @Test
    void constructor_throws_whenFormatIsInvalid() {
        // Arrange & Act & Assert
        assertThrows(InvalidEmailException.class, () -> new Email("invalid-email"));
    }

    @Test
    void constructor_throws_whenValueIsNull() {
        // Arrange & Act & Assert
        assertThrows(InvalidEmailException.class, () -> new Email(null));
    }

    @Test
    void constructor_throws_whenValueIsBlank() {
        // Arrange & Act & Assert
        assertThrows(InvalidEmailException.class, () -> new Email(""));
    }
}
