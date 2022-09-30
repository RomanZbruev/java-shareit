package ru.practicum.shareit.item.dto;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class ItemDtoWithBooking {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean available;

    @NotNull
    private ForItemBookingDto lastBooking;

    @NotNull
    private ForItemBookingDto nextBooking;

    @NotNull
    private List<CommentDto> comments;
}
