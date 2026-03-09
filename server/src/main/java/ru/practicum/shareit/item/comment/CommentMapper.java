package ru.practicum.shareit.item.comment;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Component
public class CommentMapper {

    public Comment mapNewCommentToComment(CommentCreateDto dto, User user, Item item, LocalDateTime created) {
        Comment comment = new Comment();
        if (dto != null) {
            comment.setText(dto.getText());
        } else {
            comment.setText(null);
        }
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(created);
        return comment;
    }

    public CommentDto mapCommentToResponse(Comment comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null ? comment.getAuthor().getName() : null)
                .created(comment.getCreated())
                .build();
    }
}
