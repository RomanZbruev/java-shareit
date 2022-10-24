package ru.practicum.shareit;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addUserTest() throws Exception {
        UserDto userDto = UserDto
                .builder()
                .id(1)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.addUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

    }

    @Test
    void findUserByIdTest() throws Exception {
        UserDto userDto = UserDto
                .builder()
                .id(1)
                .name("name")
                .email("email@mail.ru")
                .build();

        when(userService.findById(any())).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));
    }

    @Test
    void findUsersTest() throws Exception {
        UserDto userDto = UserDto
                .builder()
                .id(1)
                .name("name")
                .email("email@mail.ru")
                .build();

        UserDto userDto2 = UserDto
                .builder()
                .id(2)
                .name("name2")
                .email("email2@mail.ru")
                .build();

        when(userService.findAll()).thenReturn(List.of(userDto, userDto2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(userDto, userDto2))));

    }

    @Test
    void removeByIdTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserTest() throws Exception {
        UserDto userDto = UserDto
                .builder()
                .id(1)
                .name("name")
                .email("email@mail.ru")
                .build();

        UserDto userDto2 = UserDto
                .builder()
                .id(1)
                .name("name2")
                .email("email2@mail.ru")
                .build();

        when(userService.updateUser(1L, userDto2)).thenReturn(userDto2);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto2.getId()))
                .andExpect(jsonPath("$.name").value(userDto2.getName()))
                .andExpect(jsonPath("$.email").value(userDto2.getEmail()));

    }

}
