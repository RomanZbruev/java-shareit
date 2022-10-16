package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.Request;


import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> getRequestsByRequesterId(Long requesterId);


    Request getRequestsById(Long id);

    @Query("SELECT new ru.practicum.shareit.request.model.Request(rt.id,rt.description,rt.requester,rt.created) " +
            "from Request as rt " +
            "where rt.requester.id <> :id ")
    List<Request> getRequest(@Param("id") Long id);


    @Query("SELECT new ru.practicum.shareit.request.model.Request(rt.id,rt.description,rt.requester,rt.created) " +
            "from Request as rt " +
            "where rt.requester.id <> :id ")
    List<Request> getRequest(Long id, Pageable pageable);


    List<Request> findByRequester_IdNot(Long requesterId);

    List<Request> findByRequester_IdNot(Long requesterId, Pageable pageable);

}
