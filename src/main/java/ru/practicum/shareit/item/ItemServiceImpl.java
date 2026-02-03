package ru.practicum.shareit.item;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        // Можно добавить тестовые данные
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        userService.getUserById(userId); // проверяем, что пользователь существует
        Item item = ItemMapper.toItem(itemDto, userId);
        item.setId(idGenerator.getAndIncrement());
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long userId) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new UserNotFoundException("Вещь с ID=" + itemId + " не найдена");
        }
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

        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new UserNotFoundException("Вещь с ID=" + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        return items.values().stream().filter(item -> item.getOwnerId().equals(userId))
                .sorted(Comparator.comparing(Item::getId)).map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return items.values().stream().filter(item -> item.getAvailable()).filter(item -> item.getName()
                .toLowerCase().contains(text.toLowerCase()) || item.getDescription().toLowerCase().contains(text
                .toLowerCase())).map(ItemMapper::toItemDto).collect(Collectors.toList());
    }
}
