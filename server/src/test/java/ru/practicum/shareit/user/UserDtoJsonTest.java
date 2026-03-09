package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void shouldSerializeUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldDeserializeUserDto() throws Exception {
        String content = "{\"id\": 2, \"name\": \"Jane Smith\", \"email\": \"jane.smith@example.com\"}";

        UserDto userDto = json.parseObject(content);

        assertThat(userDto.getId()).isEqualTo(2L);
        assertThat(userDto.getName()).isEqualTo("Jane Smith");
        assertThat(userDto.getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    void shouldDeserializeUserDtoWithoutId() throws Exception {
        String content = "{\"name\": \"New User\", \"email\": \"new.user@example.com\"}";

        UserDto userDto = json.parseObject(content);

        assertThat(userDto.getId()).isNull();
        assertThat(userDto.getName()).isEqualTo("New User");
        assertThat(userDto.getEmail()).isEqualTo("new.user@example.com");
    }

    @Test
    void shouldSerializeUserDtoWithNullId() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(null)
                .name("Test User")
                .email("test@example.com")
                .build();

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).hasEmptyJsonPathValue("$.id");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test User");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test@example.com");
    }
}
