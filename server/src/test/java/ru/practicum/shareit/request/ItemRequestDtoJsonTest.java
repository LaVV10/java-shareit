package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeItemRequestDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

        ItemRequestDto.ItemRequestItemDto itemResponse = ItemRequestDto.ItemRequestItemDto.builder()
                .id(10L)
                .name("Drill")
                .ownerId(1L)
                .requestId(1L)
                .build();

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a power drill")
                .created(created)
                .items(List.of(itemResponse))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a power drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-01-15T10:30:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
    }

    @Test
    void shouldDeserializeItemRequestDto() throws Exception {
        String content = "{\"id\": 1, \"description\": \"Need a hammer\", " +
                "\"created\": \"2024-01-16T14:45:00\"}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a hammer");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 16, 14, 45, 0));
        assertThat(dto.getItems()).isNull();
    }
}
