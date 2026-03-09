package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ErrorResponseTest {

    @Test
    void testConstructorAndGetters() {
        ErrorResponse response = new ErrorResponse("Ошибка");
        assertThat(response.getError()).isEqualTo("Ошибка");
    }

    @Test
    void testConstructorWithNullError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ErrorResponse(null)
        );
        assertThat(exception.getMessage()).contains("error");
    }

    @Test
    void testConstructorThrowsWhenErrorIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ErrorResponse("")
        );
        assertThat(exception.getMessage()).contains("error");
    }

    @Test
    void testToString() {
        ErrorResponse response = new ErrorResponse("Ошибка");
        String result = response.toString();
        assertThat(result).contains("error='Ошибка'");
    }
}
