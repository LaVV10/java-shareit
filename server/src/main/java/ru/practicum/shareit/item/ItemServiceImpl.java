package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
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
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    public Item getItemEntityById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        UserDto userDto = userService.getUserById(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + userId + " не найден"));

        Item item = itemMapper.toItem(itemDto, user.getId());

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new ItemNotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Предмет с таким id(" + itemId + ") не найден"));

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
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Предмет с таким id(" + itemId + ") не найден"));

        ItemDto dto = itemMapper.toItemDto(item);

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
                .map(commentMapper::mapCommentToResponse)
                .collect(Collectors.toList());
        dto.setComments(comments);

        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        userService.getUserById(userId);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        if (items.isEmpty()) return List.of();

        List<ItemDto> dtos = items.stream()
                .map(itemMapper::toItemDto)
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
                        Collectors.mapping(commentMapper::mapCommentToResponse, Collectors.toList())
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
                .orElseThrow(() -> new ItemNotFoundException("Предмет с таким id(" + itemId + ") не найден"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + userId + " не найден"));

        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooking) {
            throw new IllegalArgumentException("Комментировать может только пользователь, который брал вещь в аренду");
        }

        // Создаём DTO для передачи в маппер
        CommentCreateDto dto = new CommentCreateDto();
        dto.setText(text);

        // Используем маппер
        Comment comment = commentMapper.mapNewCommentToComment(dto, author, item, LocalDateTime.now());
        Comment saved = commentRepository.save(comment);

        return commentMapper.mapCommentToResponse(saved);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<Item> found = itemRepository.searchAvailableItems(text.toLowerCase());
        return found.stream().map(itemMapper::toItemDto).collect(Collectors.toList());
    }

    private ItemDto.BookingShort mapToBookingShort(Booking b) {
        return new ItemDto.BookingShort(b.getId(), b.getBooker().getId(), b.getStart(), b.getEnd());
    }
}
