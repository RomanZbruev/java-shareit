package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;


public interface ItemRepository extends JpaRepository<Item, Long> {
    Item getItemById(Long id);

    List<Item> getItemsByOwnerId(Long ownerId);

    List<Item> findItemsByNameOrDescriptionContainingIgnoreCaseAndAvailableEquals(
            String name, String description, Boolean available);

}
