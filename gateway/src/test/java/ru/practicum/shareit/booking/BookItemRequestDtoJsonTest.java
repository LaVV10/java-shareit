package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(BookItemRequestDto.class)
class BookItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingRequestDto() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withNano(0);
        LocalDateTime end = LocalDateTime.now().plusDays(2).withNano(0);

        BookItemRequestDto dto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        JsonContent<BookItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void shouldDeserializeBookingRequestDto() throws Exception {
        String jsonContent = "{\"itemId\":1,\"start\":\"2024-12-01T10:00:00\",\"end\":\"2024-12-02T10:00:00\"}";

        BookItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 2, 10, 0, 0));
    }

    @Test
    void shouldHandleMissingFields() throws Exception {
        String jsonContent = "{\"itemId\":1}";

        BookItemRequestDto dto = json.parse(jsonContent).getObject();

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isNull();
        assertThat(dto.getEnd()).isNull();
    }
}
