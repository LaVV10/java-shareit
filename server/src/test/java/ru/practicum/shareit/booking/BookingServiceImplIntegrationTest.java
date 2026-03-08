package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private EntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;
    private Item unavailableItem;

    @BeforeEach
    void setUp() {
        // Создание владельца
        owner = User.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        entityManager.persist(owner);

        // Создание бронирующего
        booker = User.builder()
                .name("Booker")
                .email("booker@example.com")
                .build();
        entityManager.persist(booker);

        // Доступная вещь
        item = Item.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(owner.getId())
                .build();
        entityManager.persist(item);

        // Недоступная вещь
        unavailableItem = Item.builder()
                .name("Broken Drill")
                .description("Not working")
                .available(false)
                .ownerId(owner.getId())
                .build();
        entityManager.persist(unavailableItem);

        entityManager.flush();
    }

    @Test
    void shouldMakeBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto response = bookingService.createBooking(dto, booker.getId());

        assertNotNull(response);
        assertEquals(item.getId(), response.getItem().getId());
        assertEquals(booker.getId(), response.getBooker().getId());
        assertEquals(BookingStatus.WAITING, response.getStatus());
        assertNotNull(response.getStart());
        assertNotNull(response.getEnd());
    }

    @Test
    void shouldThrowExceptionWhenBookingUnavailableItem() {
        BookingDto dto = BookingDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(dto, booker.getId()));
        assertEquals("Вещь недоступна для бронирования", exception.getMessage());
    }

    @Test
    void shouldApproveBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(dto, booker.getId());

        BookingResponseDto approvedBooking = bookingService.approveBooking(booking.getId(), true, owner.getId());

        assertNotNull(approvedBooking);
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void shouldRejectBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(dto, booker.getId());

        BookingResponseDto rejectedBooking = bookingService.approveBooking(booking.getId(), false, owner.getId());

        assertNotNull(rejectedBooking);
        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenNonOwnerApprovesBooking() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(dto, booker.getId());

        User anotherUser = User.builder()
                .name("Another")
                .email("another@example.com")
                .build();
        entityManager.persist(anotherUser);
        entityManager.flush();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.approveBooking(booking.getId(), true, anotherUser.getId()));
        assertEquals("Только владелец может подтвердить бронирование", exception.getMessage());
    }

    @Test
    void shouldGetBookingById() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto createdBooking = bookingService.createBooking(dto, booker.getId());

        BookingResponseDto foundBooking = bookingService.getBookingById(createdBooking.getId(), booker.getId());

        assertNotNull(foundBooking);
        assertEquals(createdBooking.getId(), foundBooking.getId());
        assertEquals(item.getId(), foundBooking.getItem().getId());
    }

    @Test
    void shouldThrowExceptionWhenGettingBookingWithoutAccess() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(dto, booker.getId());

        User stranger = User.builder()
                .name("Stranger")
                .email("stranger@example.com")
                .build();
        entityManager.persist(stranger);
        entityManager.flush();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingById(booking.getId(), stranger.getId()));
        assertEquals("Доступ запрещён", exception.getMessage());
    }

    @Test
    void shouldGetAllUserBookings() {
        BookingDto dto1 = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(dto1, booker.getId());

        Item item2 = Item.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .ownerId(owner.getId())
                .build();
        entityManager.persist(item2);
        entityManager.flush();

        BookingDto dto2 = BookingDto.builder()
                .itemId(item2.getId())
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        bookingService.createBooking(dto2, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getBookingsByUser(booker.getId(), "ALL", null);

        assertNotNull(bookings);
        assertEquals(2, bookings.size());
    }

    @Test
    void shouldGetAllOwnerItemBookings() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(dto, booker.getId());

        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), "ALL", null);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(item.getId(), bookings.get(0).getItem().getId());
    }

    @Test
    void shouldGetUserBookingsByState() {
        // Прошлое бронирование
        BookingDto pastDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
        bookingService.createBooking(pastDto, booker.getId());

        // Текущее
        BookingDto currentDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .build();
        bookingService.createBooking(currentDto, booker.getId());

        // Будущее
        BookingDto futureDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(futureDto, booker.getId());

        // Проверка по состояниям
        List<BookingResponseDto> past = bookingService.getBookingsByUser(booker.getId(), "PAST", null);
        assertEquals(1, past.size());

        List<BookingResponseDto> current = bookingService.getBookingsByUser(booker.getId(), "CURRENT", null);
        assertEquals(1, current.size());

        List<BookingResponseDto> future = bookingService.getBookingsByUser(booker.getId(), "FUTURE", null);
        assertEquals(1, future.size());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        BookingDto dto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.createBooking(dto, 999L));
        assertTrue(exception.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        BookingDto dto = BookingDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        ItemNotFoundException exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.createBooking(dto, booker.getId()));
        assertTrue(exception.getMessage().contains("Вещь не найдена"));
    }
}
