package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;

    public BookingServiceImpl(BookingMapper bookingMapper,
                              UserRepository userRepository,
                              ItemRepository itemRepository,
                              BookingRepository bookingRepository) {
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingDto addBooking(Long bookerId, BookingDto bookingDto) {
        checkData(bookerId, bookingDto);
        Booking booking = bookingMapper.mapFromBookingDto(bookingDto);
        booking.setBooker(userRepository.getUserById(bookerId));
        booking.setItem(itemRepository.getItemById(bookingDto.getItemId()));
        booking.setStatus(Status.WAITING);
        return bookingMapper.mapFromBooking(bookingRepository.save(booking));
    }

    @Override
    public BookingDtoResponse approve(Long ownerId, Long bookingId, Boolean approve) {
        Booking booking = bookingRepository.getBookingById(bookingId);
        if (booking == null) {
            log.error("Бронирования с айди " + bookingId + " не существует.");
            throw new NotFoundException("Бронирования с айди " + bookingId + " не существует.");
        }

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            log.error("Попытка изменения статуса бронирования не владельцем");
            throw new NotFoundException("Попытка изменения статуса бронирования не владельцем");
        }

        if (booking.getStatus().equals(Status.APPROVED)) {
            log.error("Ошибка изменения статуса. Владелец уже подтвердил бронирование");
            throw new BadRequestException("Ошибка изменения статуса. Владелец уже подтвердил бронирование");
        }
        if (approve) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        bookingRepository.save(booking);
        return bookingMapper.mapFromBookingResponse(booking);
    }

    @Override
    public BookingDtoResponse getBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.getBookingById(bookingId);
        if (booking == null) {
            log.error("Бронирования с айди " + bookingId + " не существует.");
            throw new NotFoundException("Бронирования с айди " + bookingId + " не существует.");
        }
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwnerId())) {
            log.error("Попытка получения бронирования от стороннего пользователя");
            throw new NotFoundException("Попытка получения бронирования от стороннего пользователя");
        }
        return bookingMapper.mapFromBookingResponse(booking);

    }

    @Override
    public List<BookingDtoResponse> getUserBookings(Long userId, String state) {
        List<Booking> bookings = new ArrayList<>();
        checkUserAndState(userId, state);
        if (State.valueOf(state).equals(State.ALL)) {
            bookings = bookingRepository.getBookingsByBooker_IdOrderByStartDesc(userId);
        } else if (State.valueOf(state).equals(State.REJECTED)) {
            bookings = bookingRepository.getBookingsByBooker_IdAndStatusEqualsOrderByStartDesc(userId, Status.REJECTED);
        } else if (State.valueOf(state).equals(State.WAITING)) {
            bookings = bookingRepository.getBookingsByBooker_IdAndStatusEqualsOrderByStartDesc(userId, Status.WAITING);
        } else if (State.valueOf(state).equals(State.CURRENT)) {
            bookings = bookingRepository
                    .getBookingsByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        } else if (State.valueOf(state).equals(State.PAST)) {
            bookings = bookingRepository
                    .getBookingsByBooker_IdAndStartBeforeAndEndBeforeOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        } else if (State.valueOf(state).equals(State.FUTURE)) {
            bookings = bookingRepository
                    .getBookingsByBooker_IdAndStartAfterAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        }

        return bookings
                .stream()
                .map(bookingMapper::mapFromBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoResponse> getOwnerBookings(Long userId, String state) {
        List<Booking> bookings = new ArrayList<>();
        checkUserAndState(userId, state);
        if (State.valueOf(state).equals(State.ALL)) {
            bookings = bookingRepository.getBookingsByItemOwnerIdOrderByStartDesc(userId);
        } else if (State.valueOf(state).equals(State.REJECTED)) {
            bookings = bookingRepository.getBookingsByItemOwnerIdAndStatusEqualsOrderByStartDesc(userId, Status.REJECTED);
        } else if (State.valueOf(state).equals(State.WAITING)) {
            bookings = bookingRepository.getBookingsByItemOwnerIdAndStatusEqualsOrderByStartDesc(userId, Status.WAITING);
        } else if (State.valueOf(state).equals(State.CURRENT)) {
            bookings = bookingRepository
                    .getBookingsByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        } else if (State.valueOf(state).equals(State.PAST)) {
            bookings = bookingRepository
                    .getBookingsByItemOwnerIdAndStartBeforeAndEndBeforeOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        } else if (State.valueOf(state).equals(State.FUTURE)) {
            bookings = bookingRepository
                    .getBookingsByItemOwnerIdAndStartAfterAndEndAfterOrderByStartDesc(userId,
                            LocalDateTime.now(), LocalDateTime.now());
        }

        return bookings
                .stream()
                .map(bookingMapper::mapFromBookingResponse)
                .collect(Collectors.toList());
    }

    private void checkData(Long bookerId, BookingDto bookingDto) {
        Item item = itemRepository.getItemById(bookingDto.getItemId());
        if (item == null) {
            log.error("Ошибка бронирования. Предмета не существует");
            throw new NotFoundException("Ошибка бронирования. Предмета не существует");
        } else if (item.getOwnerId().equals(bookerId)) {
            log.error("Ошибка бронирования. Попытка забронировать собственную вещь");
            throw new NotFoundException("Ошибка бронирования. Попытка забронировать собственную вещь");
        } else if (!userRepository.getAllIds().contains(bookerId)) {
            log.error("Ошибка бронирования. Пользователя с айди " + bookerId + " не существует");
            throw new NotFoundException("Ошибка бронирования. Пользователя с айди " + bookerId + " не существует");
        } else if (!item.getAvailable()) {
            log.error("Ошибка бронирования. Данный предмет недоступен");
            throw new BadRequestException("Ошибка бронирования. Данный предмет недоступен");
        } else if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.error("Неправильно указана дата начала бронирования");
            throw new BadRequestException("Неправильно указана дата начала бронирования");
        } else if (bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            log.error("Неправильно указана дата окончания бронирования");
            throw new BadRequestException("Неправильно указана дата окончания бронирования");
        } else if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            log.error("Ошибка указания дат. " +
                    "Дата окончания бронирования раньше, чем дата начала бронирования");
            throw new BadRequestException("Ошибка указания дат. " +
                    "Дата окончания бронирования раньше, чем дата начала бронирования");
        }
    }

    private void checkUserAndState(Long userId, String state) {
        if (userRepository.getUserById(userId) == null) {
            log.info("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        try {
            State.valueOf(state);
        } catch (IllegalArgumentException e) {
            log.error("Unknown state: " + state);
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
