package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;


public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto findById(Long id);

    List<UserDto> findAll();

    void removeById(Long id);

    UserDto updateUser(Long id, UserDto userDto);


}
