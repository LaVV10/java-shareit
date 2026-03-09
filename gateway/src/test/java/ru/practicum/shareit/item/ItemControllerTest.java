package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemClient itemClient;

    @InjectMocks
    private ItemController itemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void shouldAddItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemClient.createItem(anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).createItem(eq(1L), any(ItemDto.class));
    }

    @Test
    void shouldValidateItemDtoOnAdd() throws Exception {
        ItemDto invalidItemDto = ItemDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItemDto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).createItem(anyLong(), any(ItemDto.class));
    }

    @Test
    void shouldGetAllUserItems() throws Exception {
        when(itemClient.getAllUserItems(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemClient).getAllUserItems(1L);
    }

    @Test
    void shouldGetItem() throws Exception {
        when(itemClient.getItem(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/{itemId}", 100)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemClient).getItem(eq(100L), eq(1L));
    }

    @Test
    void shouldEditItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Drill")
                .description("More powerful")
                .available(false)
                .build();

        when(itemClient.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/{itemId}", 100)
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(100L), eq(1L), any(ItemDto.class));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems(eq("drill"), eq(0), eq(10));
    }

    @Test
    void shouldSearchItemsWithDefaultParams() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems(eq("drill"), eq(0), eq(20));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");

        when(itemClient.addComment(anyLong(), anyLong(), any(CommentCreateDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/{itemId}/comment", 100)
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(100L), eq(1L), any(CommentCreateDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenTextIsEmptyInSearch() throws Exception {
        when(itemClient.searchItems(anyString(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk());

        verify(itemClient).searchItems(eq(""), eq(0), eq(20));
    }
}
