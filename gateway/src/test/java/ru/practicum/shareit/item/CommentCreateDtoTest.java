package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.practicum.shareit.item.comment.CommentCreateDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentCreateDtoTest {

    private final Validator validator;

    public CommentCreateDtoTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidCommentCreateDto() {
        CommentCreateDto dto = new CommentCreateDto("Great item! Very useful.");

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailWhenTextIsBlank() {
        CommentCreateDto dto = new CommentCreateDto("");

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("text", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenTextIsNull() {
        CommentCreateDto dto = new CommentCreateDto(null);

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("text", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldAcceptMaxLengthText() {
        String maxLengthText = "a".repeat(500);
        CommentCreateDto dto = new CommentCreateDto(maxLengthText);

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации при длине 500 символов");
    }

    @Test
    void shouldFailWhenTextIsTooLong() {
        String longText = "a".repeat(501);
        CommentCreateDto dto = new CommentCreateDto(longText);

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации при длине > 500");
        assertEquals(1, violations.size());
        assertEquals("text", violations.iterator().next().getPropertyPath().toString());
    }
}
