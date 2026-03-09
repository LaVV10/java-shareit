package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemServiceImpl itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void shouldAddItemAndReturnCreated() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("desc")
                .available(true)
                .build();

        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("desc")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void shouldReturnAllUserItems() throws Exception {
        when(itemService.getItemsByOwner(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnItemById() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("desc")
                .available(true)
                .comments(List.of())
                .build();

        when(itemService.getItemById(1L, 1L))
                .thenReturn(dto);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldEditItemAndReturnUpdatedItem() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Updated")
                .build();

        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Updated")
                .description("desc")
                .available(true)
                .build();

        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldSearchItemsAndReturnList() throws Exception {
        when(itemService.searchItems("drill"))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddCommentAndReturnComment() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("text");

        CommentDto response = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("user")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(String.class)))
                .thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("text"));
    }
}
