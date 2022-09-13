package ru.practicum.shareit.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequest {

    private Long id;

    @NotBlank
    private String itemName;

    private Long ownerId;

    private LocalDate creationDate;

}
