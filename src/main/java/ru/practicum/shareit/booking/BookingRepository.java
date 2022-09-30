package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.forItemBookingDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Booking getBookingById(Long Id);

    List<Booking> getBookingsByBooker_IdOrderByStartDesc(Long booker_id);

    List<Booking> getBookingsByBooker_IdAndStatusEqualsOrderByStartDesc(Long booker_id, Status status);

    List<Booking> getBookingsByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(Long booker_id,
                                                                                  LocalDateTime start,
                                                                                  LocalDateTime end);

    List<Booking> getBookingsByBooker_IdAndStartBeforeAndEndBeforeOrderByStartDesc(Long booker_id,
                                                                                   LocalDateTime start,
                                                                                   LocalDateTime end);

    List<Booking> getBookingsByBooker_IdAndStartAfterAndEndAfterOrderByStartDesc(Long booker_id,
                                                                                 LocalDateTime start,
                                                                                 LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdOrderByStartDesc(Long item_ownerId);

    List<Booking> getBookingsByItemOwnerIdAndStatusEqualsOrderByStartDesc(Long booker_id, Status status);

    List<Booking> getBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long booker_id,
                                                                                    LocalDateTime start,
                                                                                    LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdAndStartBeforeAndEndBeforeOrderByStartDesc(Long booker_id,
                                                                                     LocalDateTime start,
                                                                                     LocalDateTime end);

    List<Booking> getBookingsByItemOwnerIdAndStartAfterAndEndAfterOrderByStartDesc(Long booker_id,
                                                                                   LocalDateTime start,
                                                                                   LocalDateTime end);


    @Query("select new ru.practicum.shareit.item.dto.forItemBookingDto(bk.id, bk.start " +
            ",bk.end , bk.booker.id )" +
            "from Booking as bk " +
            "where (bk.end < current_timestamp and bk.item.id = :id)")
    List<forItemBookingDto> getLastBooking(@Param("id") Long id, Pageable pageable);

     @Query("select new ru.practicum.shareit.item.dto.forItemBookingDto(bk.id, bk.start ," +
            "bk.end , bk.booker.id )" +
            "from Booking as bk " +
            "where (bk.start > current_timestamp and bk.item.id = :id)")
    List<forItemBookingDto> getNextBooking(@Param("id") Long id, Pageable pageable);


     List<Booking> getBookingsByBooker_IdAndItemIdAndEndBeforeAndStatus(Long booker_id, Long item_id,
                                                                        LocalDateTime end, Status status);



}
