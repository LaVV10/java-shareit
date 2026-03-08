package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;

@Service
public class UserClient extends BaseClient {

    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder.build(),
                serverUrl + "/users"
        );
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post("", 0L, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto userDto) {
        return patch("/" + userId, 0L, userDto);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return get("/" + userId, 0L);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("", 0L);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/" + userId, 0L);
    }
}
