package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserRepository {

    private final HashMap<Long, User> users = new HashMap<>();
    private Long id = 1L;

    public User save(User user) {
        for (Map.Entry<Long, User> entry : users.entrySet()) {
            if (entry.getValue().getEmail().equals(user.getEmail())) {
                throw new ValidationException("Пользователь с такой почтой уже зарегистрирован");
            }
        }
        user.setId(id);
        id++;
        users.put(user.getId(), user);
        log.info("Пользователь сохранен");
        return user;
    }

    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("Пользователь с айди %s не найден", id));
        } else {
            return users.get(id);
        }
    }

    public List<User> getAll() {
        List<User> userList = new ArrayList<>();
        for (Long id : users.keySet()) {
            userList.add(users.get(id));
        }
        return userList;
    }

    public void removeById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("Пользователь с айди %s не найден", id));
        } else {
            users.remove(id);
            log.info("Пользователь с айди {} успешно удален", id);
        }
    }

    public void update(User user) {
        for (Map.Entry<Long, User> entry : users.entrySet()) {
            if (entry.getValue().getEmail().equals(user.getEmail()) && entry.getValue().getId() != user.getId()) {
                throw new ValidationException("Пользователь с такой почтой уже зарегистрирован");
            }
        }
        users.put(user.getId(), user);
    }

    public List<Long> getUsersId() { //вспомогательный метод для валидации в сервисе предметов
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<Long, User> entry : users.entrySet()) {
            ids.add(entry.getValue().getId());
        }
        return ids;
    }

}
