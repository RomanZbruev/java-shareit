package ru.practicum.shareit.request.dto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * TODO Sprint add-item-requests.
 */
public class ItemRequestDto {

    private Long id;

    @NotBlank
    private String itemName;

    private Long ownerId;

    private LocalDate creationDate;
}
