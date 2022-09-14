package ru.practicum.shareit.user;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class User {

    private long id;

    @NotBlank(message = "Имя не должно быть пустым")
    private String name;

    @Email(message = "Почта должна содержать @")
    @NotBlank(message = "Имя не должно быть пустым")
    private String email;

}
