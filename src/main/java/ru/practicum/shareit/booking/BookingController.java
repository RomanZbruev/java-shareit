package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    BookingDto addBooking(@RequestHeader("X-Sharer-User-Id") Long bookerId, @Valid @RequestBody BookingDto booking) {
        log.info("Принят запрос на бронь предмета от пользователя с айди: {}", bookerId);
        return bookingService.addBooking(bookerId, booking);
    }

    @PatchMapping("/{bookingId}")
    BookingDtoResponse approveBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId,
                                      @RequestParam Boolean approved) {
        log.info("Принят запрос на подтверждение брони предмета от пользователя с айди: {}", ownerId);
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    BookingDtoResponse getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Принят запрос на просмотр брони предмета от пользователя с айди: {}", userId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    List<BookingDtoResponse> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(defaultValue = "ALL", required = false) String state) {
        log.info("Принят запрос на просмотр бронирований пользователя");
        return bookingService.getUserBookings(userId, state);
    }

    @GetMapping("/owner")
    List<BookingDtoResponse> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @RequestParam(defaultValue = "ALL", required = false) String state) {
        log.info("Принят запрос на просмотр владельцем его бронирований");
        return bookingService.getOwnerBookings(userId, state);
    }

}
