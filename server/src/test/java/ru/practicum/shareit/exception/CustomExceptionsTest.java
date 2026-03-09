package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionsTest {

    @Test
    void duplicateEmailExceptionShouldReturnMessage() {
        String expectedMessage = "Пользователь с таким email уже существует";
        DuplicateEmailException ex = new DuplicateEmailException(expectedMessage);
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void accessDeniedExceptionShouldReturnMessage() {
        String expectedMessage = "Доступ запрещён";
        AccessDeniedException ex = new AccessDeniedException(expectedMessage);
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void itemNotFoundExceptionShouldReturnMessage() {
        String expectedMessage = "Вещь не найдена";
        ItemNotFoundException ex = new ItemNotFoundException(expectedMessage);
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void userNotFoundExceptionShouldReturnMessage() {
        String expectedMessage = "Пользователь не найден";
        UserNotFoundException ex = new UserNotFoundException(expectedMessage);
        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }
}
