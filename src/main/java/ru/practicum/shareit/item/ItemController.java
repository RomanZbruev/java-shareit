package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,@Valid @RequestBody ItemDto item){
        log.info("Принят запрос на добавление предмета от пользователя с айди: {}", ownerId);
        return itemService.addItem(ownerId,item);
    }

    @GetMapping("/{itemId}")
    public ItemDto findItem(@PathVariable Long itemId){
        log.info("Принят запрос на просмотр вещи от пользователя с айди: {}", itemId);
        return itemService.findItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAllUserItems(@RequestHeader("X-Sharer-User-Id") Long ownerId){
        log.info("Принят запрос на просмотр всех вещей пользователя с айди: {}", ownerId);
        return itemService.findAllUserItems(ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @RequestBody ItemDto itemDto, @PathVariable Long itemId){
        log.info("Принят запрос на обновление предмета от пользователя с айди: {}", ownerId);
        return itemService.updateItem(ownerId,itemId,itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> findItemsByText(@RequestParam String text){
        log.info("Принят запрос на поиск предмета");
        return itemService.findItemsByText(text);
    }


}
