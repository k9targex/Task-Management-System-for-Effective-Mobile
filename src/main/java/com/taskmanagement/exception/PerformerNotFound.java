package com.taskmanagement.exception;

public class PerformerNotFound extends RuntimeException {
    public PerformerNotFound(String message) {
        super(message);
    }
}