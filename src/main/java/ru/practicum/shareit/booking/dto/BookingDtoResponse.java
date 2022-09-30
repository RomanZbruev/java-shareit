package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
public class BookingDtoResponse {
    private Long id;

    @NotNull
    private ForBookingItemDto item;

    @NotNull
    private ForBookingUserDto booker;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    private Status status;
}
