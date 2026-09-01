package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidEmailException;

public record Email(String value) {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9]+([._%+-][a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public Email {
        String cleanValue = value == null ? null : value.trim().toLowerCase();

        if (cleanValue == null || !cleanValue.matches(EMAIL_REGEX)) {
            throw new InvalidEmailException("Invalid email address: " + value);
        }

        value = cleanValue;
    }
}
