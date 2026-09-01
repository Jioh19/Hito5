package com.jioh.hito4.domain.exception;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
