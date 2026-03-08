package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.DuplicateEmailException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateAndPersistUser() {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        UserDto createdUser = userService.createUser(userDto);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("John Doe", createdUser.getName());
        assertEquals("john.doe@example.com", createdUser.getEmail());

        User persistedUser = userRepository.findById(createdUser.getId()).orElse(null);
        assertNotNull(persistedUser);
        assertEquals(createdUser.getName(), persistedUser.getName());
        assertEquals(createdUser.getEmail(), persistedUser.getEmail());
    }

    @Test
    void shouldUpdateExistingUser() {
        User user = User.builder()
                .name("Initial Name")
                .email("initial@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto updates = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(user.getId(), updates);

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldUpdateOnlyNameWhenEmailNotProvided() {
        User user = User.builder()
                .name("Initial Name")
                .email("initial@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto updates = UserDto.builder()
                .name("Updated Name Only")
                .build();

        UserDto updatedUser = userService.updateUser(user.getId(), updates);

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Updated Name Only", updatedUser.getName());
        assertEquals("initial@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldUpdateOnlyEmailWhenNameNotProvided() {
        User user = User.builder()
                .name("Initial Name")
                .email("initial@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto updates = UserDto.builder()
                .email("newemail@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(user.getId(), updates);

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Initial Name", updatedUser.getName());
        assertEquals("newemail@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldGetUserById() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto foundUser = userService.getUserById(user.getId());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();
        entityManager.persist(user1);

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        entityManager.persist(user2);

        entityManager.flush();

        List<UserDto> allUsers = userService.getAllUsers();

        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("user1@example.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("user2@example.com")));
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        assertTrue(userRepository.findById(user.getId()).isPresent());

        userService.deleteUser(user.getId());

        assertFalse(userRepository.findById(user.getId()).isPresent());
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        User existingUser = User.builder()
                .name("User 1")
                .email("same@example.com")
                .build();
        entityManager.persist(existingUser);
        entityManager.flush();

        UserDto user2 = UserDto.builder()
                .name("User 2")
                .email("same@example.com")
                .build();

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(user2));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingEmailToExistingEmail() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();
        entityManager.persist(user1);

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();
        entityManager.persist(user2);

        entityManager.flush();

        UserDto updates = UserDto.builder()
                .email("user2@example.com")
                .build();

        assertThrows(DuplicateEmailException.class,
                () -> userService.updateUser(user1.getId(), updates));
    }

    @Test
    void shouldUpdateEmailToSameEmail() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        UserDto updates = UserDto.builder()
                .email("test@example.com")
                .name("Updated Name")
                .build();

        UserDto updatedUser = userService.updateUser(user.getId(), updates);

        assertEquals("test@example.com", updatedUser.getEmail());
        assertEquals("Updated Name", updatedUser.getName());
    }
}
