package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestDtoTest {

    private final Validator validator;

    public ItemRequestDtoTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidItemRequestDto() {
        ItemRequestDto.ItemDto item = new ItemRequestDto.ItemDto(1L, "Drill", 2L, 3L);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill for construction work")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailWhenDescriptionIsBlank() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("")
                .created(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldAllowNullCreatedAndItems() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(null)
                .items(null)
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        // Ожидаем только ошибку от @NotBlank на description — но он заполнен
        assertTrue(violations.isEmpty(), "null created и items должны быть допустимы");
    }

    @Test
    void shouldCreateItemDtoWithAllFields() {
        ItemRequestDto.ItemDto item = new ItemRequestDto.ItemDto(1L, "Drill", 2L, 3L);

        assertEquals(1L, item.getId());
        assertEquals("Drill", item.getName());
        assertEquals(2L, item.getOwnerId());
        assertEquals(3L, item.getRequestId());
    }
}
