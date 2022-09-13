package ru.practicum.shareit.booking;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {

    private Long id;

    private Long itemId;

    @NotNull
    private LocalDate bookingFrom;

    @NotNull
    private LocalDate bookingTo;

    private Long ownerId;

    private Status status;
}
