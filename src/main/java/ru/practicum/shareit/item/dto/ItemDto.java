package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.item.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название вещи не может быть пустым")
    private String name;
    @NotBlank(message = "Описание вещи не может быть пустым")
    private String description;
    @NotNull(message = "Статус доступности должен быть указан")
    private Boolean available;
    private Long requestId;
    private BookingShort lastBooking;
    private BookingShort nextBooking;
    private List<CommentDto> comments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingShort {
        private Long id;
        private Long bookerId;
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
