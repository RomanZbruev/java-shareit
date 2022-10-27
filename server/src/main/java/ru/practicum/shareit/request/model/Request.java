package ru.practicum.shareit.request.model;


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
@Table(name = "requests", schema = "public")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;


    @ManyToOne(cascade = CascadeType.ALL)
    private User requester;

    @Column(name = "created")
    private LocalDateTime created;

}
