package ru.practicum.shareit.user;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @PostConstruct
    public void init() {
        User admin = new User(idGenerator.getAndIncrement(), "Admin", "admin@shareit.ru");
        users.put(admin.getId(), admin);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }

        if (users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()))) {
            System.err.println("Email уже существует: " + userDto.getEmail());
            throw new DuplicateEmailException("Email уже используется");
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idGenerator.getAndIncrement());
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("Пользователь с ID=" + userId + " не найден");
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (users.values().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(userDto.getEmail()) && !u.getId()
                            .equals(userId))) {
                throw new DuplicateEmailException("Email уже используется");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return new ArrayList<>(users.values()).stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("Пользователь с ID=" + userId + " не найден");
        }
        users.remove(userId);
    }
}
