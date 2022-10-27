package shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;


    @PostMapping
    ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                      @Valid @RequestBody BookingDto booking) {
        log.info("Принят запрос на бронь предмета от пользователя с айди: {}", bookerId);
        return bookingClient.addBooking(bookerId, booking);
    }

    @PatchMapping("/{bookingId}")
    ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long ownerId, @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        log.info("Принят запрос на подтверждение брони предмета от пользователя с айди: {}", ownerId);
        return bookingClient.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Принят запрос на просмотр брони предмета от пользователя с айди: {}", userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    ResponseEntity<Object> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(defaultValue = "ALL") String state,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                           @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Принят запрос на просмотр бронирований пользователя");
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    ResponseEntity<Object> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = "ALL") String state,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Принят запрос на просмотр владельцем его бронирований");
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

}
