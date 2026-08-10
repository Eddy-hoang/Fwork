package com.intern.fwork.exceptions;

public class BoardColumnNotFoundException extends RuntimeException {
    public BoardColumnNotFoundException(String message) {
        super(message);
    }
}
