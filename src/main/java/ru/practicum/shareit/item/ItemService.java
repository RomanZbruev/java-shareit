package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.request.model.GetRequestInfo;

import java.util.List;

public interface ItemService {

    ItemDto addItem(Long ownerId, ItemDto itemDto);

    ItemDtoWithBooking findItemById(Long userId, Long itemId);

    List<ItemDtoWithBooking> findAllUserItems(GetRequestInfo requestInfo);

    ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto);

    List<ItemDto> findItemsByText(String text, Integer from, Integer size);

    CommentDto addComment(Long userId, CommentDto commentDto, Long itemId);

}
