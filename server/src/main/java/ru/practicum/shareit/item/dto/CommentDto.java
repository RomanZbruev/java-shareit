package ru.practicum.shareit.item.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class CommentDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime created;

    public CommentDto(Long id, String text, String authorName, LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.created = created;
    }
}
