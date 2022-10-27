package ru.practicum.shareit.item.model;


import lombok.*;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comments", schema = "public")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text")
    private String text;

    @ManyToOne(cascade = CascadeType.ALL)
    private User author;

    @ManyToOne(cascade = CascadeType.ALL)
    private Item item;

    @Column(name = "created")
    private LocalDateTime created;

}
