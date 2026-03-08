package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User toUser(UserDto userDto) {
        if (userDto == null) return null;
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public User updateEntityFromDto(UserDto dto, User entity) {
        if (dto == null || entity == null) return entity;

        if (dto.getName() != null && !dto.getName().isBlank()) {
            entity.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            entity.setEmail(dto.getEmail());
        }

        return entity;
    }
}
