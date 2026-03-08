package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeItemDto() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill for home use")
                .available(true)
                .requestId(10L)
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Powerful drill for home use");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(10);
    }

    @Test
    void shouldSerializeWithoutRequestId() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(2L)
                .name("Hammer")
                .description("Heavy duty hammer")
                .available(false)
                .requestId(null)
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(2);
        assertThat(result).hasEmptyJsonPathValue("$.requestId");
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {
        String content = "{\"id\": 3, \"name\": \"Saw\", \"description\": \"Circular saw\", " +
                "\"available\": true, \"requestId\": 5}";

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Saw");
        assertThat(dto.getDescription()).isEqualTo("Circular saw");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }
}
