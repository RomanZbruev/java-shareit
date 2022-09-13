package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Status;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
public class BookingDto {

    private Long id;

    private Long itemId;

    @NotNull
    private LocalDate bookingFrom;

    @NotNull
    private LocalDate bookingTo;

    private Status status;
}
