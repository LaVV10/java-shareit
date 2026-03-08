package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid ItemDto itemDto) {
        log.info("Post new item. Item is {}, owner id is {}", itemDto.getName(), userId);
        return itemClient.createItem(userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "20") Integer size) {
        log.info("Getting all items for user with id = {}", userId);
        return itemClient.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("Getting information about item with id = {} for user {}", itemId, userId);
        return itemClient.getItem(itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        log.info("Edit item with id {}, owner id is {}", itemId, userId);
        return itemClient.updateItem(itemId, userId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @RequestParam String text,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "20") Integer size) {
        log.info("Getting items by request: {}", text);
        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody @Valid CommentCreateDto commentDto) {
        log.info("Adding comment to item {} by user {}", itemId, userId);
        return itemClient.addComment(itemId, userId, commentDto);
    }
}

