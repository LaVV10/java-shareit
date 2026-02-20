package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;

        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        // Поля lastBooking, nextBooking, comments будут установлены в сервисе
        return dto;
    }

    public static Item toItem(ItemDto itemDto, Long ownerId) {
        if (itemDto == null) return null;

        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwnerId(ownerId);
        return item;
    }
}