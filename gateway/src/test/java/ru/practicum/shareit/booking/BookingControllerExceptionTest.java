package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerExceptionTest {

    @Mock
    private BookingClient bookingClient;

    @Test
    void shouldHandleClientErrors() {
        // Given
        BookingController controller = new BookingController(bookingClient);

        // Мокируем ответ от клиента — ошибка 400
        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.badRequest().build());

        // When
        ResponseEntity<Object> response = controller.getBookings(1L, "ALL", 0, 10);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void shouldHandleInvalidDates() {
        // Given
        BookingController controller = new BookingController(bookingClient);

        BookItemRequestDto requestDto = new BookItemRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(2)); // start > end — некорректно
        requestDto.setEnd(LocalDateTime.now().plusDays(1));

        // When
        ResponseEntity<Object> response = controller.bookItem(1L, requestDto);

        // Then
        // Проверяем, что запрос дошёл до клиента (валидация — на стороне сервера)
        verify(bookingClient).bookItem(eq(1L), argThat(dto ->
                dto.getItemId() == 1L &&
                        dto.getStart().isEqual(requestDto.getStart()) &&
                        dto.getEnd().isEqual(requestDto.getEnd())
        ));
    }
}
