package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.CommentCreateDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    public ItemClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {
        super(
                builder.build(),           // rest
                serverUrl + API_PREFIX     // serverUrl
        );
    }

    public ResponseEntity<Object> createItem(long userId, ItemDto itemDto) {
        return post("", userId, null, itemDto);
    }

    public ResponseEntity<Object> updateItem(long itemId, long userId, ItemDto itemDto) {
        return patch("/" + itemId, userId, null, itemDto);
    }

    public ResponseEntity<Object> getItem(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllUserItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> searchItems(String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from != null ? from : 0,
                "size", size != null ? size : 20
        );
        return get("/search", null, parameters);
    }

    public ResponseEntity<Object> addComment(long itemId, long userId, CommentCreateDto commentDto) {
        return post("/" + itemId + "/comment", userId, null, commentDto);
    }
}
