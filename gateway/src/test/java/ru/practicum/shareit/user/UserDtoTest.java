package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserDtoTest {

    private final Validator validator;

    public UserDtoTest() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void shouldCreateValidUserDto() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name(null)
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenEmailIsNull() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email(null)
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Должно быть нарушение валидации");
        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void shouldAllowNullId() {
        UserDto dto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Не должно быть нарушений валидации при отсутствии id");
        assertNull(dto.getId());
    }

    @Test
    void shouldCreateUserDtoWithAllArgsConstructor() {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void shouldCreateUserDtoWithNoArgsConstructor() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("John Doe", dto.getName());
        assertEquals("john@example.com", dto.getEmail());
    }
}
