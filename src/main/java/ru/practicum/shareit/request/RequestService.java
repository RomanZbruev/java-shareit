package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.GetRequestInfo;

import java.util.List;

public interface RequestService {

    public RequestDto addRequest(Long userId, RequestDto requestDto);

    public List<RequestDto> getOwnRequests(Long userId);

    public List<RequestDto> getRequests(GetRequestInfo requestInfo);

    public RequestDto getRequestById(Long userId, Long requestId);

}
