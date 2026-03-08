package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemDtoTest {

    private final Validator validator;

    public ItemDtoTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidItemDto() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill for home use")
                .available(true)
                .requestId(5L)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("")
                .description("Powerful drill for home use")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenAvailableIsNull() {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill for home use")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("available", violations.iterator().next().getPropertyPath().toString());
    }
}
