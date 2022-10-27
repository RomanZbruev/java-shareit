package ru.practicum.shareit;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.request.model.GetRequestInfo;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void addItemTest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("descr")
                .available(true)
                .build();
        when(itemService.addItem(1L, itemDto)).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));

    }

    @Test
    void findItemTest() throws Exception {
        ItemDtoWithBooking itemDto = ItemDtoWithBooking.builder()
                .id(1L)
                .name("name")
                .description("descr")
                .available(true)
                .build();
        when(itemService.findItemById(1L, itemDto.getId())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    void findAllUsersItems() throws Exception {
        ItemDtoWithBooking itemDto = ItemDtoWithBooking.builder()
                .id(1L)
                .name("name")
                .description("descr")
                .available(true)
                .build();
        when(itemService.findAllUserItems(GetRequestInfo.of(1L, 0, 20))).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void updateItem() throws Exception {
        ItemDtoWithBooking itemDto = ItemDtoWithBooking.builder()
                .id(1L)
                .name("name")
                .description("descr")
                .available(true)
                .build();

        ItemDto request = ItemDto
                .builder()
                .id(1L)
                .name("name2")
                .description("descr2")
                .available(true)
                .build();

        when(itemService.updateItem(1L, 1L, request)).thenReturn(request);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.description").value(request.getDescription()))
                .andExpect(jsonPath("$.available").value(request.getAvailable()));

    }

    @Test
    void findItemsByText() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("descr")
                .available(true)
                .build();

        when(itemService.findItemsByText("name", 0, 20)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "name")
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto))));
    }

    @Test
    void addComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("haha")
                .authorName("name")
                .build();

        when(itemService.addComment(1L, commentDto, 1L)).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));
    }
}
