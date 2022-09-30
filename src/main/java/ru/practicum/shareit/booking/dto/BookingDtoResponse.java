package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingDtoResponse {
    private Long id;

    @NotNull
    private forBookingItemDto item;

    @NotNull
    private forBookingUserDto booker;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    private Status status;
}
