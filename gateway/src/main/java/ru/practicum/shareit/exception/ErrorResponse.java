package ru.practicum.shareit.exception;

public class ErrorResponse {
    private final String error;

    public ErrorResponse(String error) {
        if (error == null || error.trim().isEmpty()) {
            throw new IllegalArgumentException("error cannot be null or empty");
        }
        this.error = error.trim();
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
