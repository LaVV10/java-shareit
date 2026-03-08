package ru.practicum.shareit.request;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.request.dto.ItemRequestIncomingDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(JavaTimeModule.class)
class ItemRequestIncomingDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestIncomingDto> json;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldSerializeItemRequestIncomingDto() throws Exception {
        ItemRequestIncomingDto dto = new ItemRequestIncomingDto();
        dto.setDescription("Need a power drill for home repairs");

        JsonContent<ItemRequestIncomingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a power drill for home repairs");
    }

    @Test
    void shouldDeserializeItemRequestIncomingDto() throws Exception {
        String content = "{\"description\": \"Looking for a circular saw\"}";

        ItemRequestIncomingDto dto = json.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("Looking for a circular saw");
    }

    @Test
    void shouldSerializeWithEmptyDescription() throws Exception {
        ItemRequestIncomingDto dto = new ItemRequestIncomingDto();
        dto.setDescription("");

        JsonContent<ItemRequestIncomingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("");
    }
}
