package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }


    public UserDto addUser(UserDto userDto){
        User user = userMapper.mapFromUserDto(userDto);
        User inMemoryUser = userRepository.save(user);
        log.info("Пользователь с айди: {} добавлен в хранилище", inMemoryUser.getId());
        return userMapper.mapFromUser(inMemoryUser);
    }

    public UserDto findById(Long id){
        User user = userRepository.getUserById(id);
        return userMapper.mapFromUser(user);
    }

    public List<UserDto> findAll(){
        List<User> inMemoryUserList = userRepository.getAll();
        return inMemoryUserList.stream().map(userMapper::mapFromUser).collect(Collectors.toList());
    }

    public void removeById(Long id){
        userRepository.removeById(id);
    }

    public UserDto updateUser(Long id, UserDto userDto){
        User inMemoryUser = userRepository.getUserById(id);
        User user = User.builder()
                .id(inMemoryUser.getId())
                .name(inMemoryUser.getName())
                .email(inMemoryUser.getEmail())
                .build();

        if (userDto.getEmail()!=null){
            user.setEmail(userDto.getEmail());
            log.info("Почта пользователя с айди {} успешно обновлена", id);
        }
        if(userDto.getName()!=null){
            user.setName(userDto.getName());
            log.info("Имя пользователя с айди {} успешно обновлено", id);
        }
        userRepository.update(user);
        log.info("Успешное добавление в хранилище обновлений пользователя");
        return userMapper.mapFromUser(user);
    }

}
