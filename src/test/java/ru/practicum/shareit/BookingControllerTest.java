package ru.practicum.shareit;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
public class BookingControllerTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @MockBean
    private BookingService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addBookingTest() throws Exception {
        BookingDto bookingDto = BookingDto
                .builder()
                .id(1L)
                .status(Status.WAITING)
                .itemId(1L)
                .start(LocalDateTime.of(2022, 1, 1, 1, 1).withNano(0))
                .end(LocalDateTime.of(2022, 2, 1, 1, 1).withNano(0))
                .build();
        when(service.addBooking(1L, bookingDto)).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().name()))
                .andExpect(jsonPath("$.itemId").value(bookingDto.getItemId()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString() + ":00"))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString() + ":00"));
    }

    @Test
    void approveBookingTest() throws Exception {
        BookingDtoResponse bookingDto = BookingDtoResponse
                .builder()
                .id(1L)
                .status(Status.WAITING)
                .start(LocalDateTime.of(2022, 1, 1, 1, 1).withNano(0))
                .end(LocalDateTime.of(2022, 2, 1, 1, 1).withNano(0))
                .build();
        when(service.approve(1L, 1L, true)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().name()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString() + ":00"))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString() + ":00"));
    }

    @Test
    void getBookingTest() throws Exception {
        BookingDtoResponse bookingDto = BookingDtoResponse
                .builder()
                .id(1L)
                .status(Status.WAITING)
                .start(LocalDateTime.of(2022, 1, 1, 1, 1).withNano(0))
                .end(LocalDateTime.of(2022, 2, 1, 1, 1).withNano(0))
                .build();
        when(service.getBooking(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingDto.getId()))
                .andExpect(jsonPath("$.status").value(bookingDto.getStatus().name()))
                .andExpect(jsonPath("$.start").value(bookingDto.getStart().toString() + ":00"))
                .andExpect(jsonPath("$.end").value(bookingDto.getEnd().toString() + ":00"));
    }

    @Test
    void getOwnerBookingTest() throws Exception {
        BookingDtoResponse bookingDto = BookingDtoResponse
                .builder()
                .id(1L)
                .status(Status.WAITING)
                .start(LocalDateTime.of(2022, 1, 1, 1, 1).withNano(0))
                .end(LocalDateTime.of(2022, 2, 1, 1, 1).withNano(0))
                .build();
        when(service.getOwnerBookings(1L, "ALL", 0, 20)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20")
                        .param("state", "ALL")
                )
                .andExpect(status().isOk());

    }

    @Test
    void getUserBookingTest() throws Exception {
        BookingDtoResponse bookingDto = BookingDtoResponse
                .builder()
                .id(1L)
                .status(Status.WAITING)
                .start(LocalDateTime.of(2022, 1, 1, 1, 1).withNano(0))
                .end(LocalDateTime.of(2022, 2, 1, 1, 1).withNano(0))
                .build();
        when(service.getUserBookings(1L, "ALL", 0, 20)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20")
                        .param("state", "ALL")
                )
                .andExpect(status().isOk());

    }

}
