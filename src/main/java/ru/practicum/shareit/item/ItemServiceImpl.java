package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemMapper itemMapper, ItemRepository itemRepository, UserRepository userRepository) {
        this.itemMapper = itemMapper;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        if (!userRepository.getUsersId().contains(ownerId)) {
            throw new NotFoundException("Пользователя с таким айди не существует");
        }
        Item item = itemMapper.mapFromItemDto(itemDto);
        item.setOwnerId(ownerId);
        Item inMemoryItem = itemRepository.save(item);
        log.info("Предмет добавлен в хранилище. Присвоено айди: {}", inMemoryItem.getId());
        return itemMapper.mapFromItem(inMemoryItem);
    }

    public ItemDto findItemById(Long itemId) {
        Item item = itemRepository.getById(itemId);
        return itemMapper.mapFromItem(item);
    }

    public List<ItemDto> findAllUserItems(Long ownerId) {
        List<Item> items = itemRepository.getAllUserItems(ownerId);
        return items
                .stream()
                .map(itemMapper::mapFromItem)
                .collect(Collectors.toList());
    }

    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) {
        if (!userRepository.getUsersId().contains(ownerId)) {
            throw new NotFoundException("Пользователя с таким айди не существует");
        }
        Item inMemoryItem = itemRepository.getById(itemId);
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

        itemRepository.updateItem(item);
        log.info("Обновление предмета");
        return itemMapper.mapFromItem(item);
    }

    public List<ItemDto> findItemsByText(String text) {
        if (text.isEmpty()) {
            log.info("Возвращение пустого списка вещей");
            return List.of();
        } else {
            log.info("Возвращение списка вещей");
            return itemRepository.getItemsByText(text.toLowerCase())
                    .stream()
                    .map(itemMapper::mapFromItem)
                    .collect(Collectors.toList());
        }
    }

}
