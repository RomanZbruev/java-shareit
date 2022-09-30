package ru.practicum.shareit.booking.dto;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class ForBookingUserDto {

    @NotNull
    Long id;

    @NotNull
    String name;
}