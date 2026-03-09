package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestIncomingDto;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = User.builder()
                .name("Requestor")
                .email("requestor@email.com")
                .build();
        entityManager.persist(requestor);

        otherUser = User.builder()
                .name("Other User")
                .email("other@email.com")
                .build();
        entityManager.persist(otherUser);

        entityManager.flush();
    }

    @Test
    void shouldCreateRequestAndPersistInDatabase() {
        ItemRequestIncomingDto dto = new ItemRequestIncomingDto();
        dto.setDescription("Need a power drill");

        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(), dto);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("Need a power drill", created.getDescription());
        assertNotNull(created.getCreated());
        assertNull(created.getItems());

        ItemRequest persisted = itemRequestRepository.findById(created.getId()).orElse(null);
        assertNotNull(persisted);
        assertEquals(created.getDescription(), persisted.getDescription());
        assertEquals(requestor.getId(), persisted.getRequester().getId());
    }

    @Test
    void shouldReturnOwnRequests() {
        ItemRequestIncomingDto dto1 = new ItemRequestIncomingDto();
        dto1.setDescription("Need a drill");
        itemRequestService.createRequest(requestor.getId(), dto1);

        ItemRequestIncomingDto dto2 = new ItemRequestIncomingDto();
        dto2.setDescription("Need a hammer");
        itemRequestService.createRequest(requestor.getId(), dto2);

        List<ItemRequestDto> ownRequests = itemRequestService.getOwnRequests(requestor.getId());

        assertNotNull(ownRequests);
        assertEquals(2, ownRequests.size());
        assertEquals("Need a hammer", ownRequests.get(0).getDescription());
        assertEquals("Need a drill", ownRequests.get(1).getDescription());
    }

    @Test
    void shouldReturnEmptyListWhenNoOwnRequests() {
        List<ItemRequestDto> ownRequests = itemRequestService.getOwnRequests(requestor.getId());

        assertNotNull(ownRequests);
        assertTrue(ownRequests.isEmpty());
    }

    @Test
    void shouldReturnAllRequestsFromOtherUsers() {
        ItemRequestIncomingDto requestorRequest = new ItemRequestIncomingDto();
        requestorRequest.setDescription("Requestor's request");
        itemRequestService.createRequest(requestor.getId(), requestorRequest);

        ItemRequestIncomingDto otherUserRequest = new ItemRequestIncomingDto();
        otherUserRequest.setDescription("Other user's request");
        itemRequestService.createRequest(otherUser.getId(), otherUserRequest);

        List<ItemRequestDto> allRequestsForRequestor = itemRequestService.getAllRequests(requestor.getId(), 0, 20);

        assertNotNull(allRequestsForRequestor);
        assertEquals(1, allRequestsForRequestor.size());
        assertEquals("Other user's request", allRequestsForRequestor.get(0).getDescription());
    }

    @Test
    void shouldReturnEmptyListWhenNoOtherUsersRequests() {
        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(requestor.getId(), 0, 20);

        assertNotNull(allRequests);
        assertTrue(allRequests.isEmpty());
    }

    @Test
    void shouldReturnRequestById() {
        ItemRequestIncomingDto dto = new ItemRequestIncomingDto();
        dto.setDescription("Specific request");
        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(), dto);

        ItemRequestDto found = itemRequestService.getRequestById(requestor.getId(), created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Specific request", found.getDescription());
        assertNotNull(found.getCreated());
    }

    @Test
    void shouldReturnRequestWithItemsWhenItemsExist() {
        ItemRequestIncomingDto dto = new ItemRequestIncomingDto();
        dto.setDescription("Request with items");
        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(), dto);

        ItemRequestDto found = itemRequestService.getRequestById(requestor.getId(), created.getId());

        assertNotNull(found);
        assertNotNull(found.getItems());
        assertTrue(found.getItems().isEmpty());
    }
}
