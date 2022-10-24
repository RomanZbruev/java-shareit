package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(Long bookerId, BookingDto bookingDto);

    BookingDtoResponse approve(Long ownerId, Long bookingId, Boolean approve);

    BookingDtoResponse getBooking(Long userId, Long bookingId);

    List<BookingDtoResponse> getUserBookings(Long userId, String state, Integer from, Integer size);

    List<BookingDtoResponse> getOwnerBookings(Long userId, String state, Integer from, Integer size);

}
