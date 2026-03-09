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
        DuplicateEmailException ex = new DuplicateEmailException("Пользователь с таким email уже существует");

        ErrorResponse response = exceptionHandler.handleDuplicateEmail(ex);

        assertThat(response.getError()).isEqualTo("Пользователь с таким email уже существует");
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Неверный аргумент");

        ErrorResponse response = exceptionHandler.handleIllegalArgument(ex);

        assertThat(response.getError()).isEqualTo("Неверный аргумент");
    }

    @Test
    void handleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("Пользователь не найден");

        ErrorResponse response = exceptionHandler.handleUserNotFound(ex);

        assertThat(response.getError()).isEqualTo("Пользователь не найден");
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Доступ запрещён");

        ErrorResponse response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getError()).isEqualTo("Доступ запрещён");
    }

    @Test
    void handleItemNotFound() {
        ItemNotFoundException ex = new ItemNotFoundException("Вещь не найдена");

        ErrorResponse response = exceptionHandler.handleItemNotFound(ex);

        assertThat(response.getError()).isEqualTo("Вещь не найдена");
    }
}
