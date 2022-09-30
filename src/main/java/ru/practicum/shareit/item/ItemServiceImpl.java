package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ForItemBookingDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;

    private final CommentMapper commentMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    public ItemServiceImpl(ItemMapper itemMapper,
                           CommentMapper commentMapper, ItemRepository itemRepository,
                           UserRepository userRepository,
                           CommentRepository commentRepository, BookingRepository bookingRepository) {
        this.itemMapper = itemMapper;
        this.commentMapper = commentMapper;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
    }

    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (!userRepository.getAllIds().contains(ownerId)) {
            throw new NotFoundException("Пользователя с таким айди не существует");
        }
        Item item = itemMapper.mapFromItemDto(itemDto);
        item.setOwnerId(ownerId);
        Item inMemoryItem = itemRepository.save(item);
        log.info("Предмет добавлен в хранилище. Присвоено айди: {}", inMemoryItem.getId());
        return itemMapper.mapFromItem(inMemoryItem);
    }

    public ItemDtoWithBooking findItemById(Long userId, Long itemId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        ItemDtoWithBooking itemDtoWithBooking = itemMapper.mapFromItemForItemWithBooking(item);
        settingComments(itemDtoWithBooking,itemId);
        if (userId.equals(item.getOwnerId())) {
            settingLastBookingForItem(itemDtoWithBooking, itemId);
            settingNextBookingForItem(itemDtoWithBooking, itemId);
        }
        return itemDtoWithBooking;
    }

    public List<ItemDtoWithBooking> findAllUserItems(Long ownerId) {
        List<Item> items = itemRepository.getItemsByOwnerId(ownerId);

        List<ItemDtoWithBooking> itemsDto = items  //маппируем объекты
                .stream()
                .map(itemMapper::mapFromItemForItemWithBooking)
                .collect(Collectors.toList());

        itemsDto.forEach(item -> {
            settingLastBookingForItem(item, item.getId()); //заполняем поле последнего бронирования
            settingNextBookingForItem(item, item.getId()); //заполняем поле следующего бронирования
            settingComments(item, item.getId()); // заполняем комментарии
        });


        itemsDto.sort((o1, o2) -> { // сортировка (в тестах нужно, чтобы предметы без бронирования были в конце)
            if (o1.getNextBooking() == (null) && o1.getLastBooking() == null &&
                    o2.getNextBooking() != null && o2.getLastBooking() != null) {
                return -1;
            }
            if (o2.getNextBooking() == (null) && o2.getLastBooking() == null &&
                    o1.getNextBooking() != null && o1.getLastBooking() != null) {
                return -1;
            }
            if (o2.getNextBooking() == (null) && o2.getLastBooking() == null &&
                    o1.getNextBooking() == null && o1.getLastBooking() == null) {
                return 0;
            }
            return o1.getId() > o2.getId() ? 1 : -1;
        });

        return itemsDto;
    }

    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        if (!userRepository.getAllIds().contains(ownerId)) {
            throw new NotFoundException("Пользователя с таким айди не существует");
        }
        Item inMemoryItem = itemRepository.getItemById(itemId);
        if (!inMemoryItem.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Ошибка редактирования предмета. Данный пользователь не является владельцем");
        }
        Item item = Item.builder()
                .id(inMemoryItem.getId())
                .name(inMemoryItem.getName())
                .description(inMemoryItem.getDescription())
                .available(inMemoryItem.getAvailable())
                .ownerId(ownerId)
                .build();
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
            log.info("Название предмета с айди {} успешно обновлено", itemId);
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
            log.info("Описание предмета с айди {} успешно обновлено", itemId);
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
            log.info("Доступность предмета с айди {} успешно обновлено", itemId);
        }

        itemRepository.save(item);
        log.info("Обновление предмета");
        return itemMapper.mapFromItem(item);
    }


    public List<ItemDto> findItemsByText(String text) {
        if (text.isEmpty()) {
            log.info("Возвращение пустого списка вещей");
            return List.of();
        } else {
            log.info("Возвращение списка вещей");
            return itemRepository
                    .findItemsByNameOrDescriptionContainingIgnoreCaseAndAvailableEquals(text, text, true)
                    .stream()
                    .map(itemMapper::mapFromItem)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public CommentDto addComment(Long userId, CommentDto commentDto, Long itemId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        Item item = itemRepository.getItemById(itemId);
        if (item == null) {
            log.error("Предмет не найден");
            throw new NotFoundException("Предмет не найден");
        }
        if (commentDto.getText().isEmpty()) {
            log.error("Текст комментария не должен быть пустым");
            throw new BadRequestException("Текст комментария не должен быть пустым");
        }
        List<Booking> bookingCheck =
                bookingRepository.getBookingsByBooker_IdAndItemIdAndEndBeforeAndStatus(userId, itemId,
                        LocalDateTime.now(), Status.APPROVED);
        if (bookingCheck.size() > 0) {
            Comment comment = commentMapper.mapFromCommentDto(commentDto);
            comment.setCreated(LocalDateTime.now());
            comment.setAuthor(user);
            comment.setItem(item);
            commentRepository.save(comment);
            return commentMapper.mapFromComment(comment);
        } else {
            log.error("Ошибка запроса. Данный пользователь еще не производил полное бронирование хотя бы раз");
            throw new
                    BadRequestException("Ошибка запроса. " +
                    "Данный пользователь еще не производил полное бронирование хотя бы раз");
        }
    }

    private void settingLastBookingForItem(ItemDtoWithBooking itemDtoWithBooking, Long itemId) {
        Pageable pageableLastBooking = PageRequest.of(0, 1, Sort.by(("end"))); //пишем ограничение для того, чтобы получить последнее броинрование
        List<ForItemBookingDto> bookings = bookingRepository.getLastBooking(itemId, pageableLastBooking);
        if (bookings.size() == 0) { //проверка на отсутсвие последнего броинрования
            itemDtoWithBooking.setLastBooking(null);
        } else {
            itemDtoWithBooking.setLastBooking(bookings.get(0));
        }
    }

    private void settingNextBookingForItem(ItemDtoWithBooking itemDtoWithBooking, Long itemId) {
        Pageable pageableNextBooking = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("start"))); //пишем ограничение для того, чтобы получить следующее броинрование
        List<ForItemBookingDto> booking = bookingRepository.getNextBooking(itemId, pageableNextBooking);
        if (booking.size() == 0) { //проверка на отсутсвие следующего броинрования
            itemDtoWithBooking.setNextBooking(null);
        } else {
            itemDtoWithBooking.setNextBooking(booking.get(0));
        }
    }

    private void settingComments(ItemDtoWithBooking itemDtoWithBooking, Long itemId) {
        List<CommentDto> comments = commentRepository.getComments(itemId);
        itemDtoWithBooking.setComments(comments);
    }
}
