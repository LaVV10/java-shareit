package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;

    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.now().withNano(0);

        bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(baseTime.plusDays(1));
        bookingDto.setEnd(baseTime.plusDays(2));

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.WAITING)
                .item(new ru.practicum.shareit.item.dto.ItemDto())
                .booker(new ru.practicum.shareit.user.UserDto())
                .build();

        // Просто заполним минимальные данные
        bookingResponseDto.getItem().setId(1L);
        bookingResponseDto.getItem().setName("Drill");
        bookingResponseDto.getBooker().setId(2L);
        bookingResponseDto.getBooker().setName("Booker");
    }

    @Test
    void shouldCreateBookingWhenValidRequest() throws Exception {
        when(bookingService.createBooking(any(BookingDto.class), eq(1L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldReturnBadRequestWhenStartDateInPast() throws Exception {
        bookingDto.setStart(baseTime.minusDays(1));

        when(bookingService.createBooking(any(BookingDto.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Время начала должно быть в настоящем или будущем"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEndDate() throws Exception {
        bookingDto.setEnd(baseTime);

        when(bookingService.createBooking(any(BookingDto.class), eq(1L)))
                .thenThrow(new IllegalArgumentException("Время окончания должно быть в будущем"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        BookingResponseDto approved = bookingResponseDto.toBuilder()
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approveBooking(eq(1L), eq(true), eq(1L)))
                .thenReturn(approved);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldRejectBooking() throws Exception {
        BookingResponseDto rejected = bookingResponseDto.toBuilder()
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.approveBooking(eq(1L), eq(false), eq(1L)))
                .thenReturn(rejected);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldReturnBookingById() throws Exception {
        when(bookingService.getBookingById(eq(1L), eq(1L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnAllUserBookings() throws Exception {
        when(bookingService.getBookingsByUser(eq(1L), eq("ALL"), any()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldUseDefaultStateWhenStateNotProvided() throws Exception {
        when(bookingService.getBookingsByUser(eq(1L), eq("ALL"), any()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldReturnAllOwnerBookings() throws Exception {
        when(bookingService.getBookingsByOwner(eq(1L), eq("ALL"), any()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void shouldReturnBadRequestForInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }
}
