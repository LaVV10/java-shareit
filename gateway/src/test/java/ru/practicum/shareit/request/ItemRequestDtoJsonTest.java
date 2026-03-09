package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(ItemRequestDto.class)
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeItemRequestDto() throws Exception {
        ItemRequestDto.ItemDto item = new ItemRequestDto.ItemDto(1L, "Drill", 2L, 3L);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill for construction work")
                .created(LocalDateTime.of(2024, 12, 1, 10, 0, 0))
                .items(Collections.singletonList(item))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a drill for construction work");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-12-01T10:00:00");
        assertThat(result).extractingJsonPathValue("$.items.length()").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(3);
    }

    @Test
    void shouldDeserializeItemRequestDto() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"description\": \"Need a drill\",\n" +
                "  \"created\": \"2024-12-01T10:00:00\",\n" +
                "  \"items\": []\n" +
                "}";

        ItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void shouldDeserializeItemRequestDtoWithItems() throws Exception {
        String jsonContent = "{\n" +
                "  \"id\": 1,\n" +
                "  \"description\": \"Need a drill\",\n" +
                "  \"created\": \"2024-12-01T10:00:00\",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"Drill\",\n" +
                "      \"ownerId\": 2,\n" +
                "      \"requestId\": 3\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        ItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(1L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Drill");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
        assertThat(dto.getItems().get(0).getRequestId()).isEqualTo(3L);
    }
}
