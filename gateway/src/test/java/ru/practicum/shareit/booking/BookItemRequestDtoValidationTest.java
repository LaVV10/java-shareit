package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookItemRequestDtoValidationTest {

    private final Validator validator;

    public BookItemRequestDtoValidationTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidBookingRequestDto() {
        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailWhenStartIsInPast() {
        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("start", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenEndIsNotFuture() {
        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("end", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateIfEndBeforeStart_WhenNoCustomValidation() {
        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        Set<ConstraintViolation<BookItemRequestDto>> violations = validator.validate(dto);

        // Ошибок нет — потому что @Future выполняется
        assertTrue(violations.isEmpty(), "Валидация пройдёт, даже если end < start");
        assertFalse(dto.getEnd().isAfter(dto.getStart()), "Логически end до start");
    }
}
