package ru.practicum.shareit.item.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 500, message = "Текст комментария не может быть длиннее 500 символов")
    private String text;
}
