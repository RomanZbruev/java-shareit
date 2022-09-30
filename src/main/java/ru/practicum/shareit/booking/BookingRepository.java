package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.ForItemBookingDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Booking getBookingById(Long id);

    List<Booking> getBookingsByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> getBookingsByBooker_IdAndStatusEqualsOrderByStartDesc(Long bookerId, Status status);

    List<Booking> getBookingsByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                  LocalDateTime start,
                                                                                  LocalDateTime end);

    List<Booking> getBookingsByBooker_IdAndStartBeforeAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                                   LocalDateTime start,
                                                                                   LocalDateTime end);

    List<Booking> getBookingsByBooker_IdAndStartAfterAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                 LocalDateTime start,
                                                                                 LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdOrderByStartDesc(Long itemOwnerId);

    List<Booking> getBookingsByItemOwnerIdAndStatusEqualsOrderByStartDesc(Long bookerId, Status status);

    List<Booking> getBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                    LocalDateTime start,
                                                                                    LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdAndStartBeforeAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                                     LocalDateTime start,
                                                                                     LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdAndStartAfterAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                   LocalDateTime start,
                                                                                   LocalDateTime end);


    @Query("select new ru.practicum.shareit.item.dto.ForItemBookingDto(bk.id, bk.start " +
            ",bk.end , bk.booker.id )" +
            "from Booking as bk " +
            "where (bk.end < current_timestamp and bk.item.id = :id)")
    List<ForItemBookingDto> getLastBooking(@Param("id") Long id, Pageable pageable);

    @Query("select new ru.practicum.shareit.item.dto.ForItemBookingDto(bk.id, bk.start ," +
            "bk.end , bk.booker.id )" +
            "from Booking as bk " +
            "where (bk.start > current_timestamp and bk.item.id = :id)")
    List<ForItemBookingDto> getNextBooking(@Param("id") Long id, Pageable pageable);


    List<Booking> getBookingsByBooker_IdAndItemIdAndEndBeforeAndStatus(Long bookerId, Long itemId,
                                                                       LocalDateTime end, Status status);


}
