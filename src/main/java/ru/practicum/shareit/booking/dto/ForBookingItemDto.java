package ru.practicum.shareit.booking.dto;


import lombok.*;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ForBookingItemDto {

    @NotNull
    Long id;

    @NotNull
    String name;


}
