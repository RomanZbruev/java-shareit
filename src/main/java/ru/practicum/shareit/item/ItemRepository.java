package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import javax.validation.constraints.NotBlank;
import java.util.List;


public interface ItemRepository extends JpaRepository<Item,Long> {
    Item getItemById(Long id);

    List<Item> getItemsByOwnerId(Long ownerId);

    List<Item> findItemsByNameOrDescriptionContainingIgnoreCaseAndAvailableEquals(
            String name, String description, Boolean available);

}
