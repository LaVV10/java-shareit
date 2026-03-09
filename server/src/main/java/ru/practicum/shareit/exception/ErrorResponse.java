package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final String error;

    public ErrorResponse(String error) {
        if (error == null || error.trim().isEmpty()) {
            throw new IllegalArgumentException("error");
        }
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "error='" + error + '\'' +
                '}';
    }
}
