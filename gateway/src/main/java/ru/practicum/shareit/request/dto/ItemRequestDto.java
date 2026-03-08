package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    private LocalDateTime created;
    private List<ItemDto> items; // вещи в ответ на запрос

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDto {
        private Long id;
        private String name;
        private Long ownerId;
        private Long requestId;
    }
}
