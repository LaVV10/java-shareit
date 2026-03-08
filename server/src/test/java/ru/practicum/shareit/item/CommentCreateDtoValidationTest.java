package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.item.comment.CommentCreateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentCreateDtoValidationTest {

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
    }

    @Test
    void shouldNotAllowNullText() {
        CommentCreateDto dto = new CommentCreateDto(null);

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Текст комментария не может быть пустым");
    }

    @Test
    void shouldNotAllowBlankText() {
        CommentCreateDto dto = new CommentCreateDto("   ");

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Текст комментария не может быть пустым");
    }

    @Test
    void shouldAllowValidText() {
        CommentCreateDto dto = new CommentCreateDto("Good!");

        Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
