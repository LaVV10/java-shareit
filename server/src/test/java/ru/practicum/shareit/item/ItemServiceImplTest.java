package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.comment.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User user;
    private Item item;
    private ItemDto itemDto;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@email.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .ownerId(1L)
                .build();

        itemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        itemRequest = new ItemRequest();
        itemRequest.setId(10L);
    }

    @Test
    void getItemEntityByIdShouldReturnItemWhenExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Item result = itemService.getItemEntityById(1L);

        assertThat(result).isEqualTo(item);
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemEntityByIdShouldThrowNotFoundExceptionWhenNotExists() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemEntityById(999L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void getItemByIdShouldReturnItemDtoWhenExists() {
        ItemDto expectedDto = ItemDto.builder().id(1L).name("Item").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.getItemById(1L, 1L);

        assertThat(result).isEqualTo(expectedDto);
        verify(itemRepository).findById(1L);
        verify(itemMapper).toItemDto(item);
    }

    @Test
    void getItemByIdShouldThrowNotFoundExceptionWhenNotExists() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(999L, 1L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Предмет с таким id(999) не найден");
    }

    @Test
    void createItemShouldReturnItemDtoWhenValid() {
        UserDto userDto = new UserDto(1L, "User", "user@yandex.ru");
        ItemDto savedDto = ItemDto.builder().id(1L).name("Item").build();

        when(userService.getUserById(1L)).thenReturn(userDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDto, 1L)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(savedDto);

        ItemDto result = itemService.createItem(itemDto, 1L);

        assertThat(result).isEqualTo(savedDto);
        verify(userService).getUserById(1L);
        verify(userRepository).findById(1L);
        verify(itemRepository).save(item);
        assertThat(item.getOwnerId()).isEqualTo(1L);
    }

    @Test
    void createItemWithRequestIdShouldSetRequestWhenRequestExists() {
        itemDto.setRequestId(10L);
        UserDto userDto = new UserDto(1L, "User", "user@yandex.ru");

        when(userService.getUserById(1L)).thenReturn(userDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemMapper.toItem(itemDto, 1L)).thenReturn(item);
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        itemService.createItem(itemDto, 1L);

        verify(itemRequestRepository).findById(10L);
        verify(itemRepository).save(item);
        assertThat(item.getRequest()).isEqualTo(itemRequest);
    }

    @Test
    void createItemWithRequestIdShouldThrowNotFoundExceptionWhenRequestNotExists() {
        itemDto.setRequestId(999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(itemMapper.toItem(itemDto, 1L)).thenReturn(item);
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(itemDto, 1L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Запрос с id 999 не найден");

        verify(itemRequestRepository).findById(999L);
    }

    @Test
    void updateItemShouldUpdateWhenUserIsOwner() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .description("Updated description")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(updateDto);

        ItemDto result = itemService.updateItem(1L, updateDto, 1L);

        assertThat(result).isEqualTo(updateDto);
        verify(itemRepository).save(item);
        assertThat(item.getName()).isEqualTo("Updated");
        assertThat(item.getDescription()).isEqualTo("Updated description");
        assertThat(item.getAvailable()).isFalse();
    }

    @Test
    void updateItemShouldThrowAccessDeniedWhenUserIsNotOwner() {
        item.setOwnerId(2L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ItemDto updateDto = ItemDto.builder().build();

        assertThatThrownBy(() -> itemService.updateItem(1L, updateDto, 1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Только владелец может редактировать вещь");
    }

    @Test
    void updateItemShouldThrowNotFoundExceptionWhenItemNotExists() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        ItemDto updateDto = ItemDto.builder().build();

        assertThatThrownBy(() -> itemService.updateItem(999L, updateDto, 1L))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Предмет с таким id(999) не найден");
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenTextIsBlank() {
        List<ItemDto> result = itemService.searchItems("");

        assertThat(result).isEmpty();
        verify(itemRepository, never()).searchAvailableItems(any());
    }

    @Test
    void searchItemsShouldReturnEmptyListWhenTextIsNull() {
        List<ItemDto> result = itemService.searchItems(null);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).searchAvailableItems(any());
    }

    @Test
    void searchItemsShouldReturnItemsWhenTextIsValid() {
        List<Item> items = List.of(item);
        ItemDto itemDto = ItemDto.builder().id(1L).name("Item").build();
        when(itemRepository.searchAvailableItems("drill")).thenReturn(items);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(itemDto);
        verify(itemRepository).searchAvailableItems("drill");
    }

    @Test
    void getItemsByOwnerShouldReturnEmptyListWhenUserHasNoItems() {
        UserDto userDto = new UserDto(1L, "User", "user@yandex.ru");
        when(userService.getUserById(1L)).thenReturn(userDto);
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of());

        List<ItemDto> result = itemService.getItemsByOwner(1L);

        assertThat(result).isEmpty();
        verify(userService).getUserById(1L);
        verify(itemRepository).findByOwnerIdOrderById(1L);
    }

    @Test
    void addCommentShouldThrowItemNotFoundExceptionWhenItemNotExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(1L, 1L, "Comment text"))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Предмет с таким id(1) не найден");
    }

    @Test
    void addCommentShouldThrowUserNotFoundExceptionWhenUserNotExists() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(1L, 1L, "Comment text"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void addCommentShouldThrowWrongRequestExceptionWhenUserDidNotBookItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(1L, 1L, "Comment text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Комментировать может только пользователь, который брал вещь в аренду");

        verify(bookingRepository).existsByBookerIdAndItemIdAndEndBefore(eq(1L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    void addCommentShouldSuccessWhenUserBookedItem() {
        UserDto userDto = UserDto.builder().id(1L).name("User").build();

        // Подготовка данных
        Item targetItem = item;
        User author = user;

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(targetItem);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        ru.practicum.shareit.item.comment.CommentDto commentResponse =
                ru.practicum.shareit.item.comment.CommentDto.builder()
                        .id(1L)
                        .text("Great item!")
                        .authorName("User")
                        .created(LocalDateTime.now())
                        .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(targetItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(eq(1L), eq(1L), any(LocalDateTime.class)))
                .thenReturn(true);

        CommentCreateDto dto = new CommentCreateDto();
        dto.setText("Great item!");

        when(commentMapper.mapNewCommentToComment(
                argThat(c -> c.getText() != null && c.getText().equals("Great item!")),
                same(author),
                same(targetItem),
                any(LocalDateTime.class)
        )).thenReturn(comment);

        when(commentRepository.save(argThat(c ->
                c.getItem() != null &&
                        c.getAuthor() != null &&
                        c.getText() != null &&
                        c.getText().equals("Great item!")
        ))).thenReturn(comment);

        when(commentMapper.mapCommentToResponse(same(comment))).thenReturn(commentResponse);

        ru.practicum.shareit.item.comment.CommentDto result = itemService.addComment(1L, 1L, "Great item!");

        assertThat(result).isEqualTo(commentResponse);

        verify(bookingRepository).existsByBookerIdAndItemIdAndEndBefore(eq(1L), eq(1L), any(LocalDateTime.class));
        verify(commentRepository).save(argThat(saved ->
                saved.getItem().equals(targetItem) &&
                        saved.getAuthor().equals(author) &&
                        saved.getText().equals("Great item!")
        ));
    }
}
