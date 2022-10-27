package ru.practicum.shareit;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.RequestServiceImpl;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.GetRequestInfo;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceImplTest {

    private final EntityManager em;

    private final RequestServiceImpl requestService;

    private User user1;

    private User user2;

    @BeforeEach
    void beforeEach() {
        user1 = new User();
        user1.setName("name");
        user1.setEmail("mail@email.ru");
        em.persist(user1);

        user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table users");
        em.createNativeQuery("truncate table items");
        em.createNativeQuery("truncate table bookings");
        em.createNativeQuery("truncate table requests");
    }

    @Test
    void addRequestCorrectTest() {
        RequestDto request = RequestDto.builder()
                .description("skovorodka")
                .build();

        RequestDto requestDto = requestService.addRequest(user1.getId(), request);
        TypedQuery<Request> query = em.createQuery("SELECT rt from Request rt where rt.id = :id", Request.class);
        Request request1 = query.setParameter("id", requestDto.getId()).getSingleResult();

        assertEquals(requestDto.getDescription(), request1.getDescription());
        assertEquals(requestDto.getId(), request1.getId());
        assertEquals(requestDto.getCreated(), request1.getCreated());

    }

    @Test
    void addRequestIncorrectUserIdTest() {
        RequestDto request = RequestDto.builder()
                .description("skovorodka")
                .build();
        Long badId = 22L;


        var a = assertThrows(NotFoundException.class, () -> requestService.addRequest(badId, request));
        assertEquals(a.getMessage(), "Пользователь не найден");
    }

    @Test
    void addRequestEmptyDescriptionTest() {
        RequestDto request = RequestDto.builder()
                .description("")
                .build();

        var a =
                assertThrows(BadRequestException.class, () -> requestService.addRequest(user1.getId(), request));
        assertEquals(a.getMessage(), "Ошибка. Запрос на вещь должен иметь непустое описание");
    }

    @Test
    void getOwnRequestsCorrectTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        Request request1 = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(1))
                .description("Num2")
                .build();
        em.persist(request1);

        List<RequestDto> requests = requestService.getOwnRequests(user1.getId());
        TypedQuery<Request> query =
                em.createQuery("SELECT rt from Request rt where rt.requester.id = :id", Request.class);
        List<Request> requestsBase = query.setParameter("id", user1.getId()).getResultList();

        assertEquals(requests.size(), 2);
        assertEquals(requests.get(0).getDescription(), requestsBase.get(0).getDescription());
        assertEquals(requests.get(0).getId(), requestsBase.get(0).getId());
        assertEquals(requests.get(0).getCreated(), requestsBase.get(0).getCreated());
        assertEquals(requests.get(1).getDescription(), requestsBase.get(1).getDescription());
        assertEquals(requests.get(1).getId(), requestsBase.get(1).getId());
        assertEquals(requests.get(1).getCreated(), requestsBase.get(1).getCreated());
    }

    @Test
    void getOwnRequestsIncorrectIdUserTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);
        Long badId = 22L;

        var a = assertThrows(NotFoundException.class, () -> requestService.getOwnRequests(badId));
        assertEquals(a.getMessage(), "Пользователь не найден");
    }

    @Test
    void getRequestsCorrectWithoutPagingTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        Request request1 = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(1))
                .description("Num2")
                .build();
        em.persist(request1);

        GetRequestInfo info = GetRequestInfo.of(user2.getId(), null, null);
        List<RequestDto> requests = requestService.getRequests(info);
        TypedQuery<Request> query =
                em.createQuery("SELECT rt from Request rt ", Request.class);
        List<Request> requestsBase = query.getResultList();

        assertEquals(requests.size(), 2);
        assertEquals(requests.get(0).getDescription(), requestsBase.get(0).getDescription());
        assertEquals(requests.get(0).getId(), requestsBase.get(0).getId());
        assertEquals(requests.get(0).getCreated(), requestsBase.get(0).getCreated());
        assertEquals(requests.get(1).getDescription(), requestsBase.get(1).getDescription());
        assertEquals(requests.get(1).getId(), requestsBase.get(1).getId());
        assertEquals(requests.get(1).getCreated(), requestsBase.get(1).getCreated());

    }

    @Test
    void getRequestsIncorrectWithPagingBadFromSizeTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        Request request1 = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(1))
                .description("Num2")
                .build();
        em.persist(request1);

        GetRequestInfo info = GetRequestInfo.of(user2.getId(), 0, 0);
        var a = assertThrows(BadRequestException.class, () -> requestService.getRequests(info));
        assertEquals(a.getMessage(), "Ошибка указания формата вывода запросов. " +
                "Индекс первого элемента, начиная с 0, и количество элементов для отображения - " +
                "положительные числа");

    }

    @Test
    void getRequestsCorrectWithPaging() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        Request request1 = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(1))
                .description("Num2")
                .build();
        em.persist(request1);

        GetRequestInfo info = GetRequestInfo.of(user2.getId(), 0, 1);
        List<RequestDto> requests = requestService.getRequests(info);
        TypedQuery<Request> query =
                em.createQuery("SELECT rt from Request rt where rt.id = :id", Request.class);
        List<Request> requestsBase = query.setParameter("id", request.getId()).getResultList();

        assertEquals(requests.size(), 1);
        assertEquals(requests.get(0).getDescription(), requestsBase.get(0).getDescription());
        assertEquals(requests.get(0).getId(), requestsBase.get(0).getId());
        assertEquals(requests.get(0).getCreated(), requestsBase.get(0).getCreated());

    }

    @Test
    void getRequestByIdCorrectTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        RequestDto requestMethod = requestService.getRequestById(user1.getId(), request.getId());
        TypedQuery<Request> query = em.createQuery("SELECT rt from Request rt where rt.id = :id", Request.class);
        Request requestBase = query.setParameter("id", request.getId()).getSingleResult();

        assertEquals(requestMethod.getDescription(), requestBase.getDescription());
        assertEquals(requestMethod.getId(), requestBase.getId());
        assertEquals(requestMethod.getCreated(), requestBase.getCreated());
    }

    @Test
    void getRequestByIdCorrectWithItemsTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);

        Item item = Item.builder()
                .name("Num")
                .description("descv")
                .requestId(request.getId())
                .available(true)
                .ownerId(user2.getId())
                .build();
        em.persist(item);

        RequestDto requestMethod = requestService.getRequestById(user1.getId(), request.getId());
        TypedQuery<Request> query = em.createQuery("SELECT rt from Request rt where rt.id = :id", Request.class);
        Request requestBase = query.setParameter("id", request.getId()).getSingleResult();

        assertEquals(requestMethod.getDescription(), requestBase.getDescription());
        assertEquals(requestMethod.getId(), requestBase.getId());
        assertEquals(requestMethod.getCreated(), requestBase.getCreated());
        assertEquals(requestMethod.getItems().size(), 1);
        assertEquals(requestMethod.getItems().get(0).getName(), item.getName());
        assertEquals(requestMethod.getItems().get(0).getDescription(), item.getDescription());
        assertEquals(requestMethod.getItems().get(0).getAvailable(), item.getAvailable());

    }

    @Test
    void getRequestIncorrectUserIdTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);
        Long badId = 22L;

        var a =
                assertThrows(NotFoundException.class, () -> requestService.getRequestById(badId, request.getId()));
        assertEquals(a.getMessage(), "Пользователь не найден");
    }

    @Test
    void getRequestIncorrectRequestIdTest() {
        Request request = Request.builder()
                .requester(user1)
                .created(LocalDateTime.now().minusHours(2))
                .description("Num1")
                .build();
        em.persist(request);
        Long badId = 22L;

        var a =
                assertThrows(NotFoundException.class, () -> requestService.getRequestById(user1.getId(), badId));
        assertEquals(a.getMessage(), "Запрос на вещь не найден");
    }

}
