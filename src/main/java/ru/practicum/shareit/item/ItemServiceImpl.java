package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        userService.getUserById(userId);
        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new UserNotFoundException("Вещь с ID=" + itemId + " не найдена"));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new UserNotFoundException("Вещь с ID=" + itemId + " не найдена"));

        ItemDto dto = ItemMapper.toItemDto(item);

        if (item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> bookings = bookingRepository.findByItemIdAndStatusNotOrderByStartAsc(
                    item.getId(), BookingStatus.WAITING);

            Booking last = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);

            Booking next = bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            dto.setLastBooking(last != null ? mapToBookingShort(last) : null);
            dto.setNextBooking(next != null ? mapToBookingShort(next) : null);
        }

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(c -> new CommentDto(
                        c.getId(),
                        c.getText(),
                        c.getAuthor().getName(),
                        c.getCreated() // ← 4 параметра
                ))
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        if (items.isEmpty()) return List.of();

        List<ItemDto> dtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        Set<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toSet());

        List<Booking> bookings = bookingRepository.findByItemIdInAndStatusNotOrderByStartAsc(
                new ArrayList<>(itemIds), BookingStatus.WAITING);

        Map<Long, List<Booking>> bookingsMap = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        List<Comment> allComments = commentRepository.findByItemIdIn(new ArrayList<>(itemIds));

        Map<Long, List<CommentDto>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(
                                comment -> new CommentDto(
                                        comment.getId(),
                                        comment.getText(),
                                        comment.getAuthor().getName(),
                                        comment.getCreated()
                                ),
                                Collectors.toList()
                        )
                ));

        for (ItemDto dto : dtos) {
            List<Booking> itemBookings = bookingsMap.getOrDefault(dto.getId(), Collections.emptyList());

            Booking last = itemBookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .orElse(null);

            Booking next = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            dto.setLastBooking(last != null ? mapToBookingShort(last) : null);
            dto.setNextBooking(next != null ? mapToBookingShort(next) : null);
            dto.setComments(commentsMap.getOrDefault(dto.getId(), Collections.emptyList()));
        }

        return dtos;
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, String text) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new UserNotFoundException("Вещь с ID=" + itemId + " не найдена"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + userId + " не найден"));

        // Проверяем, брал ли пользователь эту вещь в аренду
        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooking) {
            throw new IllegalArgumentException("Комментировать может только пользователь, который брал вещь в аренду");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        return new CommentDto(
                saved.getId(),
                saved.getText(),
                saved.getAuthor().getName(),
                saved.getCreated()
        );
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<Item> found = itemRepository.searchAvailableItems(text);
        return found.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    private ItemDto.BookingShort mapToBookingShort(Booking b) {
        return new ItemDto.BookingShort(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd());
    }
}
