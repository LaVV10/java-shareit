package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestTest {

    @Test
    void testNoArgsConstructor() {
        ItemRequest request = new ItemRequest();

        assertThat(request).isNotNull();
        assertThat(request.getId()).isNull();
        assertThat(request.getDescription()).isNull();
        assertThat(request.getRequester()).isNull();
        assertThat(request.getCreated()).isNull();
        assertThat(request.getItems()).isNull();
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);

        // Исправлено: передаём все 5 полей, включая items
        ItemRequest request = new ItemRequest(1L, "Нужна дрель", user, now, null);

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getDescription()).isEqualTo("Нужна дрель");
        assertThat(request.getRequester()).isEqualTo(user);
        assertThat(request.getCreated()).isEqualTo(now);
        assertThat(request.getItems()).isNull();
    }

    @Test
    void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);

        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Нужен молоток")
                .requester(user)
                .created(now)
                .items(List.of())
                .build();

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getDescription()).isEqualTo("Нужен молоток");
        assertThat(request.getRequester()).isEqualTo(user);
        assertThat(request.getCreated()).isEqualTo(now);
        assertThat(request.getItems()).isNotNull().isEmpty();
    }

    @Test
    void testSettersAndGetters() {
        ItemRequest request = new ItemRequest();
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);

        request.setId(1L);
        request.setDescription("Нужна пила");
        request.setRequester(user);
        request.setCreated(now);
        request.setItems(List.of());

        assertThat(request.getId()).isEqualTo(1L);
        assertThat(request.getDescription()).isEqualTo("Нужна пила");
        assertThat(request.getRequester()).isEqualTo(user);
        assertThat(request.getCreated()).isEqualTo(now);
        assertThat(request.getItems()).isNotNull().isEmpty();
    }
}
