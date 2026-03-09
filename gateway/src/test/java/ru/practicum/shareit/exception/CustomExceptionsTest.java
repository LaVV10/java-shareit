package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionsTest {

    @Test
    void duplicateEmailException() {
        DuplicateEmailException ex = new DuplicateEmailException("Email уже существует");
        assertThat(ex.getMessage()).isEqualTo("Email уже существует");
    }

    @Test
    void userNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("Пользователь не найден");
        assertThat(ex.getMessage()).isEqualTo("Пользователь не найден");
    }

    @Test
    void itemNotFoundException() {
        ItemNotFoundException ex = new ItemNotFoundException("Вещь не найдена");
        assertThat(ex.getMessage()).isEqualTo("Вещь не найдена");
    }

    @Test
    void accessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Доступ запрещён");
        assertThat(ex.getMessage()).isEqualTo("Доступ запрещён");
    }
}
