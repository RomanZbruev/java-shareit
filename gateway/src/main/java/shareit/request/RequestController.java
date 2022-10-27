package shareit.request;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shareit.request.RequestDto;


@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody RequestDto request) {
        return requestClient.addRequest(userId, request);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return requestClient.getRequests(userId, from, size);
    }


    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long requestId) {
        return requestClient.getRequestById(userId, requestId);
    }
}
