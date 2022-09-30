package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId, @Valid @RequestBody ItemDto item) {
        log.info("Принят запрос на добавление предмета от пользователя с айди: {}", ownerId);
        return itemService.addItem(ownerId, item);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking findItem(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.info("Принят запрос на просмотр вещи от пользователя с айди: {}", userId);
        return itemService.findItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoWithBooking> findAllUserItems(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        log.info("Принят запрос на просмотр всех вещей пользователя с айди: {}", ownerId);
        return itemService.findAllUserItems(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @RequestBody ItemDto itemDto, @PathVariable Long itemId) {
        log.info("Принят запрос на обновление предмета от пользователя с айди: {}", ownerId);
        return itemService.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestParam String text) {
        log.info("Принят запрос на поиск предмета");
        return itemService.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                 @RequestBody CommentDto commentDto, @PathVariable Long itemId) {
        log.info("Принят запрос на добавление комментария");
        return itemService.addComment(ownerId, commentDto, itemId);
    }


}
