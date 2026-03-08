package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestIncomingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestIncomingDto incomingDto = new ItemRequestIncomingDto("Need a drill");
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(now)
                .build();

        when(itemRequestService.createRequest(anyLong(), any(ItemRequestIncomingDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).createRequest(eq(1L), argThat(dto -> "Need a drill".equals(dto.getDescription())));
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderMissing() throws Exception {
        ItemRequestIncomingDto incomingDto = new ItemRequestIncomingDto("Need a drill");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomingDto)))
                .andExpect(status().isBadRequest());

        verify(itemRequestService, never()).createRequest(anyLong(), any());
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto request1 = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(now)
                .build();

        ItemRequestDto request2 = ItemRequestDto.builder()
                .id(2L)
                .description("Need a hammer")
                .created(now.plusHours(1))
                .build();

        when(itemRequestService.getOwnRequests(1L))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto request = ItemRequestDto.builder()
                .id(3L)
                .description("Other user's request")
                .created(now)
                .build();

        when(itemRequestService.getAllRequests(eq(1L), eq(0), eq(20)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, "1")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto request = ItemRequestDto.builder()
                .id(1L)
                .description("Specific request")
                .created(now)
                .items(List.of())
                .build();

        when(itemRequestService.getRequestById(eq(1L), eq(1L)))
                .thenReturn(request);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Specific request"));
    }

    @Test
    void shouldReturnNotFoundWhenRequestDoesNotExist() throws Exception {
        // Исправлено: используем правильный порядок аргументов (userId, requestId)
        when(itemRequestService.getRequestById(eq(1L), eq(999L)))
                .thenThrow(new ItemRequestNotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/999")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос не найден"));
    }
}
