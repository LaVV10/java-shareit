package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingDto bookingDto, Long userId);

    BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingsByUser(Long userId, String state, Pageable pageable);

    List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, Pageable pageable);
}
