package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.CommentCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentCreateDtoJsonTest {

    @Autowired
    private JacksonTester<CommentCreateDto> json;

    @Test
    void shouldSerializeCommentCreateDto() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Great item! Works perfectly.");

        JsonContent<CommentCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("Great item! Works perfectly.");
    }

    @Test
    void shouldDeserializeCommentCreateDto() throws Exception {
        String content = "{\"text\":\"Excellent condition, would recommend!\"}";

        CommentCreateDto dto = json.parseObject(content);

        assertThat(dto.getText()).isEqualTo("Excellent condition, would recommend!");
    }

    @Test
    void shouldSerializeWithEmptyText() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("");

        JsonContent<CommentCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("");
    }

    @Test
    void shouldDeserializeNullTextAsNull() throws Exception {
        String content = "{\"text\":null}";

        CommentCreateDto dto = json.parseObject(content);

        assertThat(dto.getText()).isNull();
    }
}
