package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDtoWithBooking findItemById(Long userId, Long itemId);

    List<ItemDtoWithBooking> findAllUserItems(Long ownerId);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDto> findItemsByText(String text);

    CommentDto addComment(Long userId, CommentDto commentDto, Long itemId);

}
