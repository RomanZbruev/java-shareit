package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemRepository {

    private final HashMap<Long, Item> items = new HashMap<>();
    private Long id = 1L;

    Item save(Item item) {
        item.setId(id);
        id++;
        items.put(item.getId(), item);

        return item;
    }

    Item getById(Long id){
        if(!items.containsKey(id)){
            throw new NotFoundException("Предмета с таким айди не существует");
        }
        else {
            return items.get(id);
        }
    }

    List<Item> getAllUserItems(Long ownerId){
        List<Item> itemList = new ArrayList<>();
        for (Map.Entry<Long,Item> entry: items.entrySet()){
            if (entry.getValue().getOwnerId().equals(ownerId)){
                itemList.add(entry.getValue());
            }
        }
        return itemList;
    }

    void updateItem(Item item){
        if(!items.get(item.getId()).getOwnerId().equals(item.getOwnerId())){
            throw new NotFoundException("Ошибка редактирования предмета. Данный пользователь не является владельцем");
        }
        items.put(item.getId(),item);

    }

    List<Item> getItemsByText(String text){
        List<Item> itemList = new ArrayList<>();
        for (Map.Entry<Long,Item> entry: items.entrySet()){
            if((entry.getValue().getName().toLowerCase().contains(text)
                    || entry.getValue().getDescription().toLowerCase().contains(text))
                    && entry.getValue().getAvailable()){
                itemList.add(entry.getValue());
            }
        }
        return itemList;
    }
}
