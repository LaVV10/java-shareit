package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .build();

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("Test User", foundUser.get().getName());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        User user = User.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        entityManager.persist(user);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
        assertEquals("john.doe@example.com", foundUser.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        assertEquals(2, userRepository.findAll().size());
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .name("To Delete")
                .email("delete@example.com")
                .build();

        User savedUser = entityManager.persist(user);
        entityManager.flush();

        assertTrue(userRepository.findById(savedUser.getId()).isPresent());

        userRepository.deleteById(savedUser.getId());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    void shouldThrowExceptionWhenSavingUserWithDuplicateEmail() {
        User user1 = User.builder()
                .name("User 1")
                .email("same@example.com")
                .build();

        User user2 = User.builder()
                .name("User 2")
                .email("same@example.com")
                .build();

        entityManager.persist(user1);
        entityManager.flush();

        assertThrows(Exception.class, () -> {
            entityManager.persist(user2);
            entityManager.flush();
        });
    }
}
