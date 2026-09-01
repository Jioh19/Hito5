package com.jioh.hito4.domain.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
