package ru.practicum.shareit.user;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserMapper {

    public UserDto mapFromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public User mapFromUserDto(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
