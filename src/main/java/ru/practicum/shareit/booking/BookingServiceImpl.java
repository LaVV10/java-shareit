package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto createBooking(BookingDto bookingDto, Long userId) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        if (item.getOwnerId().equals(userId)) {
            throw new ItemNotFoundException("Нельзя забронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(saved, item, booker);
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = getBookingOrThrow(bookingId);
        Item item = getItemOrThrow(booking.getItem().getId());

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может подтвердить бронирование");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Можно подтверждать только ожидающие запросы");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);

        return BookingMapper.toResponseDto(updated, item, booking.getBooker());
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);
        Long ownerId = booking.getItem().getOwnerId();
        Long bookerId = booking.getBooker().getId();

        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        return BookingMapper.toResponseDto(booking, booking.getItem(), booking.getBooker());
    }

    @Override
    public List<BookingResponseDto> getBookingsByUser(Long userId, String state, Pageable pageable) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case "CURRENT" -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                    userId, now, now, pageable);
            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                    userId, now, pageable);
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                    userId, now, pageable);
            case "WAITING" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.WAITING, pageable);
            case "REJECTED" -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED, pageable);
            default -> bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
        };

        return bookings.stream()
                .map(b -> BookingMapper.toResponseDto(b, b.getItem(), b.getBooker()))
                .toList();
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long ownerId, String state, Pageable pageable) {
        getUserOrThrow(ownerId);
        List<Long> itemIds = itemRepository.findByOwnerIdOrderById(ownerId).stream()
                .map(Item::getId).toList();
        if (itemIds.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case "CURRENT" -> bookingRepository.findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                    itemIds, now, now);
            case "PAST" -> bookingRepository.findByItemIdInAndEndBeforeOrderByStartDesc(itemIds, now);
            case "FUTURE" -> bookingRepository.findByItemIdInAndStartAfterOrderByStartDesc(itemIds, now);
            case "WAITING" -> bookingRepository.findByItemIdInAndStatusOrderByStartDesc(
                    itemIds, BookingStatus.WAITING);
            case "REJECTED" -> bookingRepository.findByItemIdInAndStatusOrderByStartDesc(
                    itemIds, BookingStatus.REJECTED);
            default -> bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId, pageable);
        };

        return bookings.stream()
                .map(b -> BookingMapper.toResponseDto(b, b.getItem(), b.getBooker()))
                .toList();
    }

    // Вспомогательные методы
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ItemNotFoundException("Бронирование не найдено"));
    }
}