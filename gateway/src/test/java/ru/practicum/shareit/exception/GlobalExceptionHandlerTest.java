package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleDuplicateEmail() {
        DuplicateEmailException ex = new DuplicateEmailException("Email уже существует");

        ErrorResponse response = exceptionHandler.handleDuplicateEmail(ex);

        assertThat(response.getError()).isEqualTo("Email уже существует");
    }

    @Test
    void handleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("Пользователь не найден");

        ErrorResponse response = exceptionHandler.handleUserNotFound(ex);

        assertThat(response.getError()).isEqualTo("Пользователь не найден");
    }

    @Test
    void handleItemNotFound() {
        ItemNotFoundException ex = new ItemNotFoundException("Вещь не найдена");

        ErrorResponse response = exceptionHandler.handleItemNotFound(ex);

        assertThat(response.getError()).isEqualTo("Вещь не найдена");
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Доступ запрещён");

        ErrorResponse response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getError()).isEqualTo("Доступ запрещён");
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Unknown state: INVALID");

        ErrorResponse response = exceptionHandler.handleUnknownStateException(ex);

        assertThat(response.getError()).isEqualTo("Unknown state: INVALID");
    }
}
