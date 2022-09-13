package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto){
        log.info("Получен запрос на добавление пользователя");
        return userService.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto findUserById(@PathVariable Long userId){
        log.info("Получен запрос на просмотр пользователя");
        return userService.findById(userId);
    }

    @GetMapping
    public List<UserDto> findAll(){
        log.info("Получен запрос на просмотр списка всех пользователей");
        return userService.findAll();
    }
    @DeleteMapping("/{userId}")
    public void  removeById(@PathVariable Long userId){
        log.info("Получен запрос на удаление пользователя с айди {}", userId);
        userService.removeById(userId);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@PathVariable Long userId, @RequestBody UserDto userDto){
        log.info("Получен запрос на обновление пользователя с айди {}", userId);
        return userService.updateUser(userId,userDto);
    }


}
