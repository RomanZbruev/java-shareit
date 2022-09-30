package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.forItemBookingDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    @Query("select new ru.practicum.shareit.item.dto.CommentDto(cs.id, cs.text, cs.author.name, cs.created)" +
            "from Comment as cs " +
            "where (cs.item.id = :id)")
    List<CommentDto> getComments(@Param("id") Long id);

}
