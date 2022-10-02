package ru.practicum.shareit.item.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ForItemBookingDto {

    @NotNull
    private Long id;

    @NotNull
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    @NotNull
    private Long bookerId;

    public ForItemBookingDto(Long id, LocalDateTime start, LocalDateTime end, Long bookerId) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.bookerId = bookerId;
    }


}
