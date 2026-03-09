package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.Comment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CommentMapper.class})
class CommentMapperTest {

    @Autowired
    private CommentMapper mapper;

    @Test
    void shouldMapNewCommentDtoToComment() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        Item item = new Item();
        item.setId(10L);

        CommentCreateDto dto = new CommentCreateDto("Great item!");
        LocalDateTime created = LocalDateTime.now();

        Comment comment = mapper.mapNewCommentToComment(dto, user, item, created);

        assertNotNull(comment);
        assertEquals("Great item!", comment.getText());
        assertEquals(user, comment.getAuthor()); // ← setAuthor / getAuthor
        assertEquals(item, comment.getItem());
        assertEquals(created, comment.getCreated());
    }

    @Test
    void shouldMapCommentToCommentResponseDto() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");

        Item item = new Item();
        item.setId(10L);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Excellent quality!");
        comment.setAuthor(user); // ← исправлено: setAuthor
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = mapper.mapCommentToResponse(comment); // ← CommentDto, не CommentResponseDto

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Excellent quality!", dto.getText());
        assertEquals("John Doe", dto.getAuthorName());
        assertNotNull(dto.getCreated());
    }

    @Test
    void shouldHandleNullUserWhenMappingToResponse() {
        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Test comment");
        comment.setAuthor(null);
        comment.setCreated(LocalDateTime.now());

        CommentDto dto = mapper.mapCommentToResponse(comment);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Test comment", dto.getText());
        assertNull(dto.getAuthorName());
    }

    @Test
    void shouldHandleNullDtoInMapNewCommentToComment() {
        User user = new User();
        Item item = new Item();
        LocalDateTime created = LocalDateTime.now();

        Comment result = mapper.mapNewCommentToComment(null, user, item, created);

        assertNotNull(result);
        assertNull(result.getText());
        assertEquals(user, result.getAuthor());
        assertEquals(item, result.getItem());
        assertEquals(created, result.getCreated());
    }

    @Test
    void shouldReturnNullWhenMapCommentToResponseWithNullComment() {
        CommentDto result = mapper.mapCommentToResponse(null);
        assertNull(result);
    }

    @Test
    void shouldMapNewCommentToCommentWhenUserIsNull() {
        CommentCreateDto dto = new CommentCreateDto("Test");
        Item item = new Item();
        LocalDateTime created = LocalDateTime.now();

        Comment result = mapper.mapNewCommentToComment(dto, null, item, created);
        assertNotNull(result);
        assertEquals("Test", result.getText());
        assertNull(result.getAuthor());
        assertEquals(item, result.getItem());
        assertEquals(created, result.getCreated());
    }

    @Test
    void shouldMapNewCommentToCommentWhenItemIsNull() {
        CommentCreateDto dto = new CommentCreateDto("Test");
        User user = new User();
        LocalDateTime created = LocalDateTime.now();

        Comment result = mapper.mapNewCommentToComment(dto, user, null, created);
        assertNotNull(result);
        assertEquals("Test", result.getText());
        assertEquals(user, result.getAuthor());
        assertNull(result.getItem());
        assertEquals(created, result.getCreated());
    }

    @Test
    void shouldMapNewCommentToCommentWhenCreatedIsNull() {
        CommentCreateDto dto = new CommentCreateDto("Test");
        User user = new User();
        Item item = new Item();

        Comment result = mapper.mapNewCommentToComment(dto, user, item, null);
        assertNotNull(result);
        assertEquals("Test", result.getText());
        assertEquals(user, result.getAuthor());
        assertEquals(item, result.getItem());
        assertNull(result.getCreated());
    }
}
