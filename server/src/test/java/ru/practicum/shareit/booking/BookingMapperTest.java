package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingMapperTest {

    @Autowired
    private BookingMapper bookingMapper;

    private final LocalDateTime now = LocalDateTime.now().withNano(0);

    @Test
    void shouldMapBookingToResponseDto() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);

        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking, item, booker);

        assertNotNull(dto);
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getEnd(), dto.getEnd());
        assertEquals(booking.getStatus(), dto.getStatus());

        assertNotNull(dto.getItem());
        assertEquals(item.getId(), dto.getItem().getId());
        assertEquals(item.getName(), dto.getItem().getName());

        assertNotNull(dto.getBooker());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(booker.getName(), dto.getBooker().getName());
    }

    @Test
    void shouldHandleNullBookingInToResponseDto() {
        BookingResponseDto dto = bookingMapper.toResponseDto(null, null, null);
        assertNull(dto);
    }

    @Test
    void shouldHandleNullItemInToResponseDto() {
        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(BookingStatus.APPROVED);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking, null, booker);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getItem()); // item = null → itemDto = null
        assertNotNull(dto.getBooker());
        assertEquals("Booker", dto.getBooker().getName());
    }

    @Test
    void shouldHandleNullBookerInToResponseDto() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(BookingStatus.REJECTED);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking, item, null);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNotNull(dto.getItem());
        assertEquals("Drill", dto.getItem().getName());
        assertNull(dto.getBooker()); // booker = null → userDto = null
    }

    @Test
    void shouldHandleNullDatesInToResponseDto() {
        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(null);
        booking.setEnd(null);
        booking.setStatus(BookingStatus.WAITING);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking, item, booker);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getStart());
        assertNull(dto.getEnd());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
    }

    @Test
    void shouldWorkWithMinimalData() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now);
        booking.setEnd(now.plusDays(1));
        booking.setStatus(BookingStatus.WAITING);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking, null, null);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(now, dto.getStart());
        assertEquals(now.plusDays(1), dto.getEnd());
        assertEquals(BookingStatus.WAITING, dto.getStatus());
        assertNull(dto.getItem());
        assertNull(dto.getBooker());
    }
}
