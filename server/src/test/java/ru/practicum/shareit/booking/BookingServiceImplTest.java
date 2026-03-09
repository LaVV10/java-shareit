package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private Pageable pageable;
    private LocalDateTime fixedNow;

    @BeforeEach
    void setUp() {
        // Фиксируем время
        fixedNow = LocalDateTime.now().withNano(0).withSecond(0);
        pageable = PageRequest.of(0, 10);

        owner = User.builder().id(1L).name("Owner").email("owner@email.com").build();
        booker = User.builder().id(2L).name("Booker").email("booker@email.com").build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .ownerId(1L)
                .build();

        booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(fixedNow.plusDays(1))
                .end(fixedNow.plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(fixedNow.plusDays(1))
                .end(fixedNow.plusDays(2))
                .build();
    }

    @Test
    void createBookingShouldThrowWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь недоступна для бронирования");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingShouldThrowWhenUserIsOwner() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(bookingDto, 1L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Нельзя забронировать свою вещь");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBookingShouldThrowWhenUserNotOwner() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, true, 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Только владелец может подтвердить бронирование");
    }

    @Test
    void approveBookingShouldThrowWhenStatusNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));
        when(itemRepository.findById(eq(1L))).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, true, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Можно подтверждать только ожидающие запросы");
    }

    @Test
    void getBookingByIdShouldThrowWhenUserHasNoAccess() {
        when(bookingRepository.findById(eq(1L))).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 999L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Доступ запрещён");
    }

    @Test
    void getBookingsByUserShouldReturnAll() {
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), eq(pageable)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(2L, "ALL", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(eq(2L), eq(pageable));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByUserShouldReturnPast() {
        Booking pastBooking = booking.toBuilder()
                .start(fixedNow.minusDays(2))
                .end(fixedNow.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(pastBooking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(2L, "PAST", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndEndBeforeOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByUserShouldReturnFuture() {
        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(2L, "FUTURE", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStartAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), eq(pageable));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByUserShouldReturnCurrent() {
        Booking currentBooking = booking.toBuilder()
                .start(fixedNow.minusHours(1))
                .end(fixedNow.plusHours(1))
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.findById(eq(2L))).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(List.of(currentBooking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(2L, "CURRENT", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(2L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByOwnerShouldReturnAll() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(1L))).thenReturn(List.of(item));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(1L), eq(pageable)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(1L, "ALL", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(1L), eq(pageable));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByOwnerShouldReturnCurrent() {
        Booking currentBooking = booking.toBuilder()
                .start(fixedNow.minusHours(1))
                .end(fixedNow.plusHours(1))
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(1L))).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(currentBooking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(1L, "CURRENT", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                anyList(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByOwnerShouldReturnPast() {
        Booking pastBooking = booking.toBuilder()
                .start(fixedNow.minusDays(2))
                .end(fixedNow.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build();

        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(1L))).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndEndBeforeOrderByStartDesc(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(pastBooking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(1L, "PAST", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByItemIdInAndEndBeforeOrderByStartDesc(anyList(), any(LocalDateTime.class));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }

    @Test
    void getBookingsByOwnerShouldReturnFuture() {
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerIdOrderById(eq(1L))).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdInAndStartAfterOrderByStartDesc(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toResponseDto(any(Booking.class), any(Item.class), any(User.class)))
                .thenReturn(new BookingResponseDto());

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(1L, "FUTURE", pageable);

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByItemIdInAndStartAfterOrderByStartDesc(anyList(), any(LocalDateTime.class));
        verify(bookingMapper).toResponseDto(any(Booking.class), any(Item.class), any(User.class));
    }
}
