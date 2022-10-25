package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ErrorResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {

    @Test
    void errorResponseCreationTest() {
        String text = "text";
        ErrorResponse errorResponse = new ErrorResponse(text);

        assertEquals(text, errorResponse.getError());

    }
}
