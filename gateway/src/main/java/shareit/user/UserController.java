package shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient client;


    @PostMapping
    public ResponseEntity<Object> addUser(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос на добавление пользователя");
        return client.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> findUserById(@PathVariable Long userId) {
        log.info("Получен запрос на просмотр пользователя");
        return client.findUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Получен запрос на просмотр списка всех пользователей");
        return client.findAll();
    }

    @DeleteMapping("/{userId}")
    public void removeById(@PathVariable Long userId) {
        log.info("Получен запрос на удаление пользователя с айди {}", userId);
        client.removeById(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Получен запрос на обновление пользователя с айди {}", userId);
        return client.updateUser(userId, userDto);
    }


}
