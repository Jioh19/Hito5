package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidUsernameException;

public record Username(String value) {
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9]{4,}$";

    public Username {
        String cleanValue = value == null ? null : value.trim().toLowerCase();

        if (cleanValue == null || !cleanValue.matches(USERNAME_REGEX)) {
            throw new InvalidUsernameException("Invalid username: " + value);
        }

        value = cleanValue;
    }
}
