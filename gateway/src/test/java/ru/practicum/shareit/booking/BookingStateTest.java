package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingStateTest {

    @Test
    void shouldContainAllStates() {
        BookingState[] states = BookingState.values();
        assertEquals(6, states.length);
        assertArrayEquals(
                new BookingState[]{BookingState.ALL, BookingState.CURRENT,
                        BookingState.PAST, BookingState.FUTURE,
                        BookingState.WAITING, BookingState.REJECTED},
                states
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED"})
    void shouldParseValidState(String stateName) {
        Optional<BookingState> state = BookingState.from(stateName);
        assertTrue(state.isPresent());
        assertEquals(stateName, state.get().name());
    }

    @Test
    void shouldHandleCaseInsensitiveInController() {
        Optional<BookingState> state = BookingState.from("all");
        assertTrue(state.isPresent());
        assertEquals(BookingState.ALL, state.get());
    }

    @Test
    void shouldReturnAllForNullState() {
        Optional<BookingState> state = BookingState.from(null);
        assertTrue(state.isPresent());
        assertEquals(BookingState.ALL, state.get());
    }

    @Test
    void shouldReturnAllForEmptyState() {
        Optional<BookingState> state = BookingState.from("");
        assertTrue(state.isPresent());
        assertEquals(BookingState.ALL, state.get());
    }

    @Test
    void shouldReturnEmptyForUnknownState() {
        Optional<BookingState> state = BookingState.from("UNKNOWN");
        assertFalse(state.isPresent());
    }
}
