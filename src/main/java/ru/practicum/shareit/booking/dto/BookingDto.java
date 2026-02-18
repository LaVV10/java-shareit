package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @NotNull(message = "Время начала бронирования не может быть пустым")
    @FutureOrPresent(message = "Время начала должно быть в настоящем или будущем")
    private LocalDateTime start;

    @NotNull(message = "Время окончания бронирования не может быть пустым")
    @Future(message = "Время окончания должно быть в будущем")
    private LocalDateTime end;

    @NotNull(message = "ID вещи обязательный")
    private Long itemId;
}
