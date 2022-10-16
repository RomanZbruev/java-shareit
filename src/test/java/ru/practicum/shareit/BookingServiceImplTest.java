package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {

    private final EntityManager em;

    private final BookingServiceImpl service;

    private User user1;

    private User user2;

    private Item item;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("name");
        user1.setEmail("mail@email.ru");
        em.persist(user1);

        user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

        item = new Item();
        item.setName("item");
        item.setDescription("item descr");
        item.setOwnerId(user1.getId());
        item.setAvailable(true);
        em.persist(item);
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table users");
        em.createNativeQuery("truncate table items");
        em.createNativeQuery("truncate table bookings");


    }

    @Test
    void addBookingCorrectTest() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        bookingDto = service.addBooking(user2.getId(), bookingDto);

        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk where bk.booker.id = :id", Booking.class);
        Booking bookingDto1 = query.setParameter("id", bookingDto.getBooker().getId()).getSingleResult();

        assertEquals(bookingDto.getId(), bookingDto1.getId());
        assertEquals(bookingDto.getStart(), bookingDto1.getStart());
        assertEquals(bookingDto.getEnd(), bookingDto1.getEnd());
        assertEquals(bookingDto.getStatus(), bookingDto1.getStatus());
        assertEquals(bookingDto.getBooker(), bookingDto1.getBooker());
        assertEquals(bookingDto.getItem(), bookingDto1.getItem());

    }

    @Test
    void addBookingBadItemTest() {
        Long badId = 22L;
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(badId)
                .build();

        BookingDto finalBookingDto = bookingDto;
        var exception =
                assertThrows(NotFoundException.class, () -> service.addBooking(user2.getId(), finalBookingDto));
        assertEquals(exception.getMessage(), "Ошибка бронирования. Предмета не существует");

    }

    @Test
    void addBookingYourItemTest() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(NotFoundException.class, () -> service.addBooking(user1.getId(), finalBookingDto));

    }

    @Test
    void addBookingBadUserTest() {
        Long badId = 22L;
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(NotFoundException.class, () -> service.addBooking(badId, finalBookingDto));
    }

    @Test
    void addBookingNotAvailableTest() {
        item.setAvailable(false);

        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(BadRequestException.class, () -> service.addBooking(user2.getId(), finalBookingDto));
    }

    @Test
    void addBookingBadStartTimeTest() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(BadRequestException.class, () -> service.addBooking(user2.getId(), finalBookingDto));
    }

    @Test
    void addBookingBadEndTimeTest() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(BadRequestException.class, () -> service.addBooking(user2.getId(), finalBookingDto));
    }

    @Test
    void addBookingTimesTest() {
        BookingDto bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item.getId())
                .build();

        BookingDto finalBookingDto = bookingDto;
        assertThrows(BadRequestException.class, () -> service.addBooking(user2.getId(), finalBookingDto));
    }

    @Test
    void approveBookingBadBookingIdTest() {
        Long badId = 22L;

        assertThrows(NotFoundException.class, () -> service.approve(user1.getId(), badId, true));
    }

    @Test
    void approveBookingBadIdTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        assertThrows(NotFoundException.class, () -> service.approve(user2.getId(), booking.getId(), true));
    }

    @Test
    void approveBookingWhenApprovedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        assertThrows(BadRequestException.class, () -> service.approve(user1.getId(), booking.getId(), true));
    }

    @Test
    void approveBookingCorrectTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        BookingDtoResponse bookingDtoResponse = service.approve(user1.getId(), booking.getId(), true);

        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk where bk.booker.id = :id", Booking.class);
        Booking bookingBase = query.setParameter("id", booking.getBooker().getId()).getSingleResult();

        assertEquals(bookingDtoResponse.getId(), bookingBase.getId());
        assertEquals(bookingDtoResponse.getStart(), bookingBase.getStart());
        assertEquals(bookingDtoResponse.getEnd(), bookingBase.getEnd());
        assertEquals(bookingDtoResponse.getStatus(), bookingBase.getStatus());
        assertEquals(bookingDtoResponse.getBooker().getId(), bookingBase.getBooker().getId());
        assertEquals(bookingDtoResponse.getItem().getId(), bookingBase.getItem().getId());
    }

    @Test
    void approveBookingCorrectSetRejectedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        BookingDtoResponse bookingDtoResponse = service.approve(user1.getId(), booking.getId(), false);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk where bk.booker.id = :id", Booking.class);
        Booking bookingBase = query.setParameter("id", booking.getBooker().getId()).getSingleResult();

        assertEquals(bookingDtoResponse.getId(), bookingBase.getId());
        assertEquals(bookingDtoResponse.getStart(), bookingBase.getStart());
        assertEquals(bookingDtoResponse.getEnd(), bookingBase.getEnd());
        assertEquals(bookingDtoResponse.getStatus(), Status.REJECTED);
        assertEquals(bookingDtoResponse.getBooker().getId(), bookingBase.getBooker().getId());
        assertEquals(bookingDtoResponse.getItem().getId(), bookingBase.getItem().getId());
    }

    @Test
    void getBookingCorrectTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        BookingDtoResponse bookingDtoResponse = service.getBooking(user1.getId(), booking.getId());
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk where bk.id = :id", Booking.class);
        Booking bookingBase = query.setParameter("id", booking.getId()).getSingleResult();

        assertEquals(bookingDtoResponse.getId(), bookingBase.getId());
        assertEquals(bookingDtoResponse.getStart(), bookingBase.getStart());
        assertEquals(bookingDtoResponse.getEnd(), bookingBase.getEnd());
        assertEquals(bookingDtoResponse.getStatus(), bookingBase.getStatus());
        assertEquals(bookingDtoResponse.getBooker().getId(), bookingBase.getBooker().getId());
        assertEquals(bookingDtoResponse.getItem().getId(), bookingBase.getItem().getId());

    }

    @Test
    void getBookingBadUserTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        User user = User.builder()
                .name("name3")
                .email("male@male.ru")
                .build();
        em.persist(user);

        assertThrows(NotFoundException.class, () -> service.getBooking(user.getId(), booking.getId()));
    }

    @Test
    void getBookingBadBookingTest() {
        Long badId = 22L;
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        assertThrows(NotFoundException.class, () -> service.getBooking(user1.getId(), badId));
    }

    @Test
    void getBookingThirdUserTest() {
        User user = new User();
        user.setName("name0");
        user.setEmail("mail0@email.ru");
        em.persist(user);

        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        assertThrows(NotFoundException.class, () -> service.getBooking(user.getId(), booking.getId()));

    }

    @Test
    void getUserBookingsCorrectWithoutPagingStateWaitingTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "WAITING", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.WAITING)
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId()); //т.к. по условию в bookings передаются от самого нового бронирования
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());


    }


    @Test
    void getUserBookingsCorrectWithoutPagingStateALLTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "ALL", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithoutPagingStateRejectedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "REJECTED", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.REJECTED)
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithoutPagingStateCurrentTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "CURRENT", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithoutPagingStatePastTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "PAST", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithoutPagingStateFutureTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "FUTURE", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsIncorrectUserIdTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);
        Long badId = 22L;

        var exception = assertThrows(NotFoundException.class, () ->
                service.getUserBookings(badId, "FUTURE", null, null));
        assertEquals(exception.getMessage(), "Пользователь не найден");

    }

    @Test
    void getUserBookingsIncorrectStateTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);
        String badState = "STATENOSTATE";

        var exception = assertThrows(ValidationException.class, () ->
                service.getUserBookings(user2.getId(), badState, null, null));
        assertEquals(exception.getMessage(), "Неизвестное состояние запроса аренды: " + badState);

    }

    @Test
    void getUserBookingsIncorrectFromSizeTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        var exception = assertThrows(BadRequestException.class, () ->
                service.getUserBookings(user2.getId(), "WAITING", 0, 0));
        assertEquals(exception.getMessage(), "Ошибка указания формата вывода запросов. " +
                "Индекс первого элемента, начиная с 0, и количество элементов для отображения - " +
                "положительные числа");

    }

    @Test
    void getUserBookingsCorrectWithPagingStateWaitingTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "WAITING", 0, 1); //отбираем первое бронирование
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId()); //т.к. по условию в bookings передаются от самого нового бронирования
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());

    }


    @Test
    void getUserBookingsCorrectWithPagingStateALLTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "ALL", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithPagingStateRejectedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "REJECTED", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.REJECTED)
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithPagingStateCurrentTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "CURRENT", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithPagingStatePastTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "PAST", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getUserBookingsCorrectWithPagingStateFutureTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getUserBookings(user2.getId(), "FUTURE", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStateWaitingTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "WAITING", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.WAITING)
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId()); //т.к. по условию в bookings передаются от самого нового бронирования
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());


    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStateWaitingWhenOneItemTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Item item1 = Item.builder()
                .ownerId(user2.getId())
                .name("testItem")
                .available(true)
                .description("coolItem")
                .build();
        em.persist(item1);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.WAITING)
                .booker(user1)
                .item(item1)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "WAITING", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .setParameter("status", Status.WAITING)
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());


    }


    @Test
    void getOwnerBookingsCorrectWithoutPagingStateALLTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "ALL", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStateRejectedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "REJECTED", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.REJECTED)
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStateCurrentTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "CURRENT", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStatePastTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "PAST", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithoutPagingStateFutureTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "FUTURE", null, null);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithPagingStateWaitingTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "WAITING", 0, 1); //отбираем первое бронирование
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId()); //т.к. по условию в bookings передаются от самого нового бронирования
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());

    }


    @Test
    void getOwnerBookingsCorrectWithPagingStateALLTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "ALL", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .getResultList();

        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(1).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(1).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(1).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(1).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(1).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(1).getItem().getId());
        assertEquals(bookings.get(1).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(1).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(1).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(1).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(1).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(1).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithPagingStateRejectedTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "REJECTED", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.booker.id = :id and bk.status = :status", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", user2.getId())
                .setParameter("status", Status.REJECTED)
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithPagingStateCurrentTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "CURRENT", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithPagingStatePastTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "PAST", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsCorrectWithPagingStateFutureTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        List<BookingDtoResponse> bookings = service.getOwnerBookings(user1.getId(), "FUTURE", 0, 20);
        TypedQuery<Booking> query = em.createQuery("SELECT bk from Booking bk " +
                "where bk.id = :id", Booking.class);
        List<Booking> bookingBase = query
                .setParameter("id", booking2.getId())
                .getResultList();

        assertEquals(1, bookings.size());
        assertEquals(bookings.get(0).getId(), bookingBase.get(0).getId());
        assertEquals(bookings.get(0).getStart(), bookingBase.get(0).getStart());
        assertEquals(bookings.get(0).getEnd(), bookingBase.get(0).getEnd());
        assertEquals(bookings.get(0).getStatus(), bookingBase.get(0).getStatus());
        assertEquals(bookings.get(0).getBooker().getId(), bookingBase.get(0).getBooker().getId());
        assertEquals(bookings.get(0).getItem().getId(), bookingBase.get(0).getItem().getId());

    }

    @Test
    void getOwnerBookingsIncorrectFromSizeTest() {
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusHours(10))
                .end(LocalDateTime.now().minusHours(2))
                .status(Status.WAITING)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking);

        Booking booking2 = Booking.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .status(Status.REJECTED)
                .booker(user2)
                .item(item)
                .build();
        em.persist(booking2);

        var exception = assertThrows(BadRequestException.class, () ->
                service.getOwnerBookings(user1.getId(), "WAITING", 0, 0));
        assertEquals(exception.getMessage(), "Ошибка указания формата вывода запросов. " +
                "Индекс первого элемента, начиная с 0, и количество элементов для отображения - " +
                "положительные числа");
    }
}
