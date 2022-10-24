package ru.practicum.shareit;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.GetRequestInfo;
import ru.practicum.shareit.user.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {


    private final EntityManager em;

    private final ItemServiceImpl service;

    private User user;

    @BeforeEach
    void beforeEach() {
        user = new User();
        user.setName("name");
        user.setEmail("mail@email.ru");
        em.persist(user);
    }

    @AfterEach
    void afterEach() {
        em.createNativeQuery("truncate table users");
        em.createNativeQuery("truncate table items");
        em.createNativeQuery("truncate table bookings");
    }

    @Test
    void saveItemCorrectTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();

        itemDto = service.addItem(user.getId(), itemDto);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());

    }

    @Test
    void saveItemInCorrectIdUserTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        Long incorrectId = 222L;

        assertThrows(NotFoundException.class, () -> service.addItem(incorrectId, itemDto));
    }

    @Test
    void findItemByIdCorrectForOwnerWithoutBookingTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();

        itemDto = service.addItem(user.getId(), itemDto);

        ItemDtoWithBooking result = service.findItemById(user.getId(), itemDto.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(item.getDescription(), result.getDescription());
        assertNull(result.getNextBooking());
        assertNull(result.getLastBooking());
        assertEquals(List.of(), result.getComments());
    }

    @Test
    void findItemByIdCorrectForNewUserWithoutBookingTest() {
        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDtoWithBooking result = service.findItemById(user2.getId(), itemDto.getId());

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertNull(result.getNextBooking());
        assertNull(result.getLastBooking());
        assertEquals(List.of(), result.getComments());
    }


    @Test
    void findItemByIdIncorrectUserIdTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        Long incorrectId = 22L;
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto finalItemDto = itemDto;
        assertThrows(NotFoundException.class, () -> service.findItemById(incorrectId, finalItemDto.getId()));
    }

    @Test
    void findItemByIdIncorrectItemIdTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        Long incorrectId = 22L;
        itemDto = service.addItem(user.getId(), itemDto);

        assertThrows(NotFoundException.class, () -> service.findItemById(user.getId(), 22L));
    }


    @Test
    void updateItemCorrectTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);


        ItemDto updated = ItemDto.builder()
                .name("newName")
                .description("dds")
                .available(true)
                .build();

        updated = service.updateItem(user.getId(), itemDto.getId(), updated);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), updated.getId());
        assertEquals(item.getName(), updated.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), updated.getDescription());
        assertEquals(item.getAvailable(), updated.getAvailable());

    }

    @Test
    void updateItemNotInBaseTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);


        ItemDto updated = ItemDto.builder()
                .name("newName")
                .description("dds")
                .available(true)
                .build();
        Long userId = 21L;
        Long itemId = itemDto.getId();
        assertThrows(NotFoundException.class, () -> service.updateItem(userId, itemId, updated));
    }

    @Test
    void updateItemNotOwnerTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);
        Long itemId = itemDto.getId();


        ItemDto updated = ItemDto.builder()
                .name("newName")
                .description("dds")
                .available(true)
                .build();
        assertThrows(NotFoundException.class, () -> service.updateItem(user2.getId(), itemId, updated));
    }

    @Test
    void updateItemCorrectWithNullNameTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto updated = ItemDto.builder()
                .description("dds")
                .available(true)
                .build();
        updated = service.updateItem(user.getId(), itemDto.getId(), updated);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), updated.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), updated.getDescription());
        assertEquals(item.getAvailable(), updated.getAvailable());

    }

    @Test
    void updateItemCorrectWithNullDescriptionTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto updated = ItemDto.builder()
                .name("lalala")
                .available(true)
                .build();
        updated = service.updateItem(user.getId(), itemDto.getId(), updated);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), updated.getId());
        assertEquals(item.getName(), updated.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertEquals(item.getAvailable(), updated.getAvailable());
    }

    @Test
    void updateItemCorrectWithNullAvailableTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto updated = ItemDto.builder()
                .name("lalala")
                .description("descripsdasdastion")
                .build();
        updated = service.updateItem(user.getId(), itemDto.getId(), updated);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertEquals(item.getId(), updated.getId());
        assertEquals(item.getName(), updated.getName());
        assertEquals(item.getOwnerId(), user.getId());
        assertEquals(item.getDescription(), updated.getDescription());
        assertEquals(item.getAvailable(), itemDto.getAvailable());
    }

    @Test
    void findAllUserItemsWithoutPage() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("lalala")
                .description("descripsdasdastion")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);

        GetRequestInfo getRequestInfo = GetRequestInfo.of(user.getId(), null, null);


        List<ItemDtoWithBooking> items = service.findAllUserItems(getRequestInfo);
        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.ownerId = :id", Item.class);
        List<Item> itemsBase = query.setParameter("id", user.getId()).getResultList();

        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getId(), itemsBase.get(0).getId());
        assertEquals(items.get(0).getName(), itemsBase.get(0).getName());
        assertEquals(items.get(0).getDescription(), itemsBase.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), itemsBase.get(0).getAvailable());
        assertEquals(items.get(1).getId(), itemsBase.get(1).getId());
        assertEquals(items.get(1).getName(), itemsBase.get(1).getName());
        assertEquals(items.get(1).getDescription(), itemsBase.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), itemsBase.get(1).getAvailable());
    }

    @Test
    void findAllUserItemsNullsSizeFromTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("lalala")
                .description("descripsdasdastion")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);
        GetRequestInfo getRequestInfo = GetRequestInfo.of(user.getId(), 0, 0);

        assertThrows(BadRequestException.class, () -> service.findAllUserItems(getRequestInfo));
    }

    @Test
    void findAllUserItemNegativeSizeOrFromTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("lalala")
                .description("descripsdasdastion")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);
        GetRequestInfo getRequestInfo = GetRequestInfo.of(user.getId(), 0, -1);

        assertThrows(BadRequestException.class, () -> service.findAllUserItems(getRequestInfo));
    }


    @Test
    void findAllUserItemWithPageTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("lalala")
                .description("descripsdasdastion")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);
        GetRequestInfo getRequestInfo = GetRequestInfo.of(user.getId(), 0, 20);

        List<ItemDtoWithBooking> items = service.findAllUserItems(getRequestInfo);
        TypedQuery<Item> query = em.createQuery("SELECT i from Item i where i.ownerId = :id", Item.class);
        List<Item> itemsBase = query.setParameter("id", user.getId()).getResultList();

        assertEquals(items.size(), 2);
        assertEquals(items.get(0).getId(), itemsBase.get(0).getId());
        assertEquals(items.get(0).getName(), itemsBase.get(0).getName());
        assertEquals(items.get(0).getDescription(), itemsBase.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), itemsBase.get(0).getAvailable());
        assertEquals(items.get(1).getId(), itemsBase.get(1).getId());
        assertEquals(items.get(1).getName(), itemsBase.get(1).getName());
        assertEquals(items.get(1).getDescription(), itemsBase.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), itemsBase.get(1).getAvailable());
    }


    @Test
    void addCommentCorrect() {
        Item item = Item.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        em.persist(item);


        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(10))
                .end(LocalDateTime.now().minusDays(5))
                .booker(user2)
                .status(Status.APPROVED)
                .item(item)
                .build();
        em.persist(booking);


        CommentDto commentDto = CommentDto.builder()
                .authorName("Name")
                .created(LocalDateTime.now())
                .text("text")
                .build();

        commentDto = service.addComment(user2.getId(), commentDto, item.getId());
        TypedQuery<Comment> query = em.createQuery("SELECT i from Comment i where i.id = :id", Comment.class);
        Comment commentBase = query.setParameter("id", commentDto.getId()).getSingleResult();
        assertEquals(commentDto.getText(), commentBase.getText());
        assertEquals(commentDto.getAuthorName(), commentBase.getAuthor().getName());
        assertEquals(commentDto.getCreated(), commentBase.getCreated());

    }


    @Test
    void addCommentNoUserTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);
        Long id = 22L;

        CommentDto commentDto = CommentDto.builder()
                .authorName("Name")
                .created(LocalDateTime.now())
                .text("text")
                .build();

        ItemDto finalItemDto = itemDto;
        assertThrows(NotFoundException.class, () -> service.addComment(id, commentDto, finalItemDto.getId()));
    }

    @Test
    void addCommentNoItemTest() {
        Long id = 22L;
        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

        CommentDto commentDto = CommentDto.builder()
                .authorName("Name")
                .created(LocalDateTime.now())
                .text("text")
                .build();

        assertThrows(NotFoundException.class, () -> service.addComment(user.getId(), commentDto, id));

    }


    @Test
    void addCommentNoBookingTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);


        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

        CommentDto commentDto = CommentDto.builder()
                .authorName("Name")
                .created(LocalDateTime.now())
                .text("text")
                .build();

        ItemDto finalItemDto = itemDto;
        assertThrows(BadRequestException.class, () -> service.addComment(user.getId(), commentDto, finalItemDto.getId()));
    }

    @Test
    void addCommentEmptyTextTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("It.name")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);


        User user2 = new User();
        user2.setName("name2");
        user2.setEmail("mail2@email.ru");
        em.persist(user2);

        CommentDto commentDto = CommentDto.builder()
                .authorName("Name")
                .text("")
                .created(LocalDateTime.now())
                .build();

        ItemDto finalItemDto = itemDto;
        assertThrows(BadRequestException.class, () -> service.addComment(user.getId(), commentDto, finalItemDto.getId()));
    }

    @Test
    void findItemsByTextCorrectForNameNoPageTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Itname")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("nameIt")
                .description("description")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);


        List<ItemDto> items = service.findItemsByText("na", null, null);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i", Item.class); // так как в базе только два предмета
        List<Item> itemsBase = query.getResultList();

        assertEquals(items.size(), itemsBase.size());
        assertEquals(items.get(0).getId(), itemsBase.get(0).getId());
        assertEquals(items.get(0).getName(), itemsBase.get(0).getName());
        assertEquals(items.get(0).getDescription(), itemsBase.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), itemsBase.get(0).getAvailable());
        assertEquals(items.get(1).getId(), itemsBase.get(1).getId());
        assertEquals(items.get(1).getName(), itemsBase.get(1).getName());
        assertEquals(items.get(1).getDescription(), itemsBase.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), itemsBase.get(1).getAvailable());

    }

    @Test
    void findItemsByTextCorrectForDescriptionNoPageTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Itname")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("nameIt")
                .description("description")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);


        List<ItemDto> items = service.findItemsByText("des", null, null);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i", Item.class); // так как в базе только два предмета
        List<Item> itemsBase = query.getResultList();

        assertEquals(items.size(), itemsBase.size());
        assertEquals(items.get(0).getId(), itemsBase.get(0).getId());
        assertEquals(items.get(0).getName(), itemsBase.get(0).getName());
        assertEquals(items.get(0).getDescription(), itemsBase.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), itemsBase.get(0).getAvailable());
        assertEquals(items.get(1).getId(), itemsBase.get(1).getId());
        assertEquals(items.get(1).getName(), itemsBase.get(1).getName());
        assertEquals(items.get(1).getDescription(), itemsBase.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), itemsBase.get(1).getAvailable());


    }

    @Test
    void findItemsByTextCorrectNoTextTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Itname")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("nameIt")
                .description("description")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);


        List<ItemDto> items = service.findItemsByText("", null, null);


        assertEquals(items.size(), 0);

    }

    @Test
    void findItemsByTextBadFromOrSizeTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Itname")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("nameIt")
                .description("description")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);

        assertThrows(BadRequestException.class, () -> service.findItemsByText("dsds", -1, 0));

    }

    @Test
    void findItemsByTextCorrectWithPageableTest() {
        ItemDto itemDto = ItemDto.builder()
                .name("Itname")
                .description("description")
                .available(true)
                .build();
        itemDto = service.addItem(user.getId(), itemDto);

        ItemDto itemDto2 = ItemDto.builder()
                .name("nameIt")
                .description("description")
                .available(true)
                .build();
        itemDto2 = service.addItem(user.getId(), itemDto2);

        List<ItemDto> items = service.findItemsByText("des", 0, 20);

        TypedQuery<Item> query = em.createQuery("SELECT i from Item i", Item.class); // так как в базе только два предмета
        List<Item> itemsBase = query.getResultList();

        assertEquals(items.size(), itemsBase.size());
        assertEquals(items.get(0).getId(), itemsBase.get(0).getId());
        assertEquals(items.get(0).getName(), itemsBase.get(0).getName());
        assertEquals(items.get(0).getDescription(), itemsBase.get(0).getDescription());
        assertEquals(items.get(0).getAvailable(), itemsBase.get(0).getAvailable());
        assertEquals(items.get(1).getId(), itemsBase.get(1).getId());
        assertEquals(items.get(1).getName(), itemsBase.get(1).getName());
        assertEquals(items.get(1).getDescription(), itemsBase.get(1).getDescription());
        assertEquals(items.get(1).getAvailable(), itemsBase.get(1).getAvailable());

    }


}
