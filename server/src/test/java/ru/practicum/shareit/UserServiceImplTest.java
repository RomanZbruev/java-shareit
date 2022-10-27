package ru.practicum.shareit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

    private UserServiceImpl userService;

    private UserRepository userRepository;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        when(userRepository.save(any())).then(invocation -> invocation.getArgument(0));
        userMapper = new UserMapper();
        userService = new UserServiceImpl(userRepository, userMapper);
    }

    @Test
    void addUser() {
        UserDto userDto = UserDto
                .builder()
                .name("NewName")
                .email("mail@mail.ru")
                .build();

        UserDto result = userService.addUser(userDto);

        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

    @Test
    void findUserByIdTestCorrect() {
        User user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setEmail("mail@mail.ru");
        when(userRepository.getUserById(user.getId())).thenReturn(user);

        UserDto result = userService.findById(user.getId());

        assertEquals(result.getId(), user.getId());
        assertEquals(result.getName(), user.getName());
        assertEquals(result.getEmail(), user.getEmail());
    }

    @Test
    void findUserByIdTestWhenIdNotInBase() {
        User user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setEmail("mail@mail.ru");
        when(userRepository.getUserById(user.getId())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.findById(user.getId()));
    }

    @Test
    void findAllTest() {
        User user = new User();
        user.setId(1L);
        user.setName("Name");
        user.setEmail("mail@mail.ru");
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Name2");
        user2.setEmail("mail2@mail.ru");
        when(userRepository.findAll()).thenReturn(List.of(user,user2));

        List<UserDto> result = userService.findAll();

        assertEquals(result.size(),2);

    }

    @Test
    void updateUserTest() {
        UserDto userDto = UserDto
                .builder()
                .name("NewName")
                .email("mail2@mail.ru")
                .build();
        User inBase = User.builder()
                .id(1L)
                .name("Name")
                .email("mail@mail.ru")
                .build();
        when(userRepository.getUserById(1L)).thenReturn(inBase);

        UserDto result = userService.updateUser(1L,userDto);
        userDto.setId(1);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

    @Test
    void updateUserOnlyNameTest() {
        UserDto userDto = UserDto
                .builder()
                .name("NewName")
                .build();
        User inBase = User.builder()
                .id(1L)
                .name("Name")
                .email("mail@mail.ru")
                .build();
        when(userRepository.getUserById(1L)).thenReturn(inBase);

        UserDto result = userService.updateUser(1L,userDto);
        userDto.setId(1);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), userDto.getName());
        assertEquals(result.getEmail(), inBase.getEmail());
    }

    @Test
    void updateUserOnlyMailTest() {
        UserDto userDto = UserDto
                .builder()
                .email("newmail@mail.ru")
                .build();
        User inBase = User.builder()
                .id(1L)
                .name("Name")
                .email("mail@mail.ru")
                .build();
        when(userRepository.getUserById(1L)).thenReturn(inBase);

        UserDto result = userService.updateUser(1L,userDto);
        userDto.setId(1);
        assertEquals(result.getId(), userDto.getId());
        assertEquals(result.getName(), inBase.getName());
        assertEquals(result.getEmail(), userDto.getEmail());
    }

}
