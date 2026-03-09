package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ItemMapper.class})
class ItemMapperTest {

    @Autowired
    private ItemMapper mapper;

    @Test
    void shouldMapItemToItemDto() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .request(request)
                .build();

        ItemDto dto = mapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Item", dto.getName());
        assertEquals("Description", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void shouldMapItemToItemDtoWhenRequestIsNull() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .available(true)
                .request(null)
                .build();

        ItemDto dto = mapper.toItemDto(item);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertNull(dto.getRequestId());
    }

    @Test
    void shouldReturnNullWhenMapToDtoWithNullItem() {
        ItemDto result = mapper.toItemDto(null);
        assertNull(result);
    }

    @Test
    void itemRequestIdShouldReturnNullWhenItemIsNull() {
        ItemDto dto = mapper.toItemDto(null);
        assertNull(dto);
    }

    @Test
    void itemRequestIdShouldReturnNullWhenRequestIsNull() {
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .request(null)
                .build();

        ItemDto dto = mapper.toItemDto(item);
        assertNotNull(dto);
        assertNull(dto.getRequestId());
    }

    @Test
    void itemRequestIdShouldReturnNullWhenRequestIdIsNull() {
        ItemRequest request = new ItemRequest();
        request.setId(null);

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .request(request)
                .build();

        ItemDto dto = mapper.toItemDto(item);
        assertNotNull(dto);
        assertNull(dto.getRequestId());
    }

    @Test
    void shouldUpdateItemWhenAllFieldsProvided() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .name("New")
                .description("New desc")
                .available(false)
                .build();

        mapper.updateItem(item, dto);

        assertEquals("New", item.getName());
        assertEquals("New desc", item.getDescription());
        assertFalse(item.getAvailable());
    }

    @Test
    void shouldUpdateItemWhenOnlyNameProvided() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .name("New")
                .build();

        mapper.updateItem(item, dto);

        assertEquals("New", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertTrue(item.getAvailable());
    }

    @Test
    void shouldUpdateItemWhenOnlyDescriptionProvided() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .description("New desc")
                .build();

        mapper.updateItem(item, dto);

        assertEquals("Old", item.getName());
        assertEquals("New desc", item.getDescription());
        assertTrue(item.getAvailable());
    }

    @Test
    void shouldUpdateItemWhenOnlyAvailableProvided() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .available(false)
                .build();

        mapper.updateItem(item, dto);

        assertEquals("Old", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertFalse(item.getAvailable());
    }

    @Test
    void shouldNotUpdateItemWhenEmptyName() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .name(" ")
                .build();

        mapper.updateItem(item, dto);

        assertEquals("Old", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertTrue(item.getAvailable());
    }

    @Test
    void shouldNotUpdateItemWhenEmptyDescription() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder()
                .description(" ")
                .build();

        mapper.updateItem(item, dto);

        assertEquals("Old", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertTrue(item.getAvailable());
    }

    @Test
    void shouldUpdateItemWhenDtoHasNullFields() {
        Item item = Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .build();

        ItemDto dto = ItemDto.builder().build();

        mapper.updateItem(item, dto);

        assertEquals("Old", item.getName());
        assertEquals("Old desc", item.getDescription());
        assertTrue(item.getAvailable());
    }
}
