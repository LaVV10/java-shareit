package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private ItemDto testItemDto;

    @BeforeEach
    void setUp() {
        // Создаём и сохраняем пользователя
        testUser = User.builder()
                .name("Test User")
                .email("test.user@email.com")
                .build();
        testUser = userRepository.save(testUser);
        entityManager.flush();

        // Подготавливаем DTO для вещи
        testItemDto = ItemDto.builder()
                .name("Power Drill")
                .description("Professional power drill for home use")
                .available(true)
                .build();
    }

    @Test
    void shouldCreateAndPersistItem() {
        ItemDto createdItem = itemService.createItem(testItemDto, testUser.getId());

        assertNotNull(createdItem);
        assertEquals("Power Drill", createdItem.getName());
        assertEquals("Professional power drill for home use", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());

        Item persistedItem = itemRepository.findById(createdItem.getId()).orElse(null);
        assertNotNull(persistedItem);
        assertEquals(createdItem.getName(), persistedItem.getName());
        assertEquals(createdItem.getDescription(), persistedItem.getDescription());
        assertEquals(createdItem.getAvailable(), persistedItem.getAvailable()); // getAvailable(), не isAvailable()
    }

    @Test
    void shouldUpdateExistingItem() {
        ItemDto createdItem = itemService.createItem(testItemDto, testUser.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(createdItem.getId(), updateDto, testUser.getId());

        assertNotNull(updatedItem);
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals("Updated Drill", updatedItem.getName());
        assertEquals("Updated description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void shouldReturnAllUserItems() {
        itemService.createItem(testItemDto, testUser.getId());

        ItemDto anotherItem = ItemDto.builder()
                .name("Hammer")
                .description("Heavy duty hammer")
                .available(true)
                .build();
        itemService.createItem(anotherItem, testUser.getId());

        List<ItemDto> userItems = itemService.getItemsByOwner(testUser.getId());

        assertNotNull(userItems);
        assertEquals(2, userItems.size());
        assertTrue(userItems.stream().anyMatch(item -> item.getName().equals("Power Drill")));
        assertTrue(userItems.stream().anyMatch(item -> item.getName().equals("Hammer")));
    }

    @Test
    void shouldSearchItemsByText() {
        ItemDto createdItem = itemService.createItem(testItemDto, testUser.getId());

        List<ItemDto> searchResults = itemService.searchItems("drill");

        assertNotNull(searchResults);
        assertEquals(1, searchResults.size());
        assertEquals(createdItem.getId(), searchResults.get(0).getId());

        List<ItemDto> searchResultsByDescription = itemService.searchItems("professional");

        assertEquals(1, searchResultsByDescription.size());
        assertEquals(createdItem.getId(), searchResultsByDescription.get(0).getId());
    }

    @Test
    void shouldNotReturnUnavailableItemsInSearch() {
        ItemDto availableItem = ItemDto.builder()
                .name("Power Drill")
                .description("Good drill")
                .available(true)
                .build();
        itemService.createItem(availableItem, testUser.getId());

        ItemDto unavailableItem = ItemDto.builder()
                .name("Broken Drill")
                .description("Not working")
                .available(false)
                .build();
        itemService.createItem(unavailableItem, testUser.getId());

        List<ItemDto> searchResults = itemService.searchItems("drill");

        assertEquals(1, searchResults.size());
        assertEquals("Power Drill", searchResults.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsBlank() {
        itemService.createItem(testItemDto, testUser.getId());

        List<ItemDto> searchResults = itemService.searchItems("");

        assertNotNull(searchResults);
        assertTrue(searchResults.isEmpty());
    }

    @Test
    void shouldSearchCaseInsensitive() {
        ItemDto createdItem = itemService.createItem(testItemDto, testUser.getId());

        List<ItemDto> searchResults1 = itemService.searchItems("DRILL");
        List<ItemDto> searchResults2 = itemService.searchItems("DrIlL");
        List<ItemDto> searchResults3 = itemService.searchItems("power");

        assertEquals(1, searchResults1.size());
        assertEquals(1, searchResults2.size());
        assertEquals(1, searchResults3.size());
        assertEquals(createdItem.getId(), searchResults1.get(0).getId());
        assertEquals(createdItem.getId(), searchResults2.get(0).getId());
        assertEquals(createdItem.getId(), searchResults3.get(0).getId());
    }
}
