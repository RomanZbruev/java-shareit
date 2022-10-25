package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.GetRequestInfo;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequestServiceImpl implements RequestService {

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;

    public RequestServiceImpl(UserRepository userRepository,
                              ItemRepository itemRepository,
                              RequestRepository requestRepository,
                              RequestMapper requestMapper,
                              ItemMapper itemMapper) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    public RequestDto addRequest(Long userId, RequestDto requestDto) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        if (requestDto.getDescription() == null || requestDto.getDescription().isEmpty()) {
            log.error("Ошибка. Запрос на вещь должен иметь непустое описание");
            throw new BadRequestException("Ошибка. Запрос на вещь должен иметь непустое описание");
        }
        Request request = requestMapper.mapFromRequestDto(requestDto);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        Request inBaseRequest = requestRepository.save(request);
        return requestMapper.mapFromRequest(inBaseRequest);
    }

    @Override
    public List<RequestDto> getOwnRequests(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        List<Request> requests = requestRepository.getRequestsByRequesterId(userId);
        return settingRequestDtoList(requests);
    }

    @Override
    public List<RequestDto> getRequests(GetRequestInfo requestInfo) {
        Long userId = requestInfo.getUserId();
        Integer size = requestInfo.getSize();
        Integer from = requestInfo.getFrom();
        List<RequestDto> requestsDto;
        if (from == null || size == null) {
            //List<Request> requests = requestRepository.getRequest(userId); - реализация метода со следующей строки
            // через нативный запрос(пока оставил для себя)
            List<Request> requests = requestRepository.findByRequester_IdNot(userId);
            requestsDto = settingRequestDtoList(requests);
            return requestsDto;
        }
        if ((size == 0 && from == 0) || (size < 0 || from < 0)) {
            log.error("Ошибка указания формата вывода запросов. " +
                    "Индекс первого элемента, начиная с 0, и количество элементов для отображения - " +
                    "положительные числа");
            throw new BadRequestException("Ошибка указания формата вывода запросов. " +
                    "Индекс первого элемента, начиная с 0, и количество элементов для отображения - " +
                    "положительные числа");
        }
        int fromPage = from / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        //List<Request> requests = requestRepository.getRequest(userId, pageable); - реализация метода со следующей строки
        // через нативный запрос(пока оставил для себя)
        List<Request> requests = requestRepository.findByRequester_IdNot(userId,pageable);
        requestsDto = settingRequestDtoList(requests);
        return requestsDto;
    }

    @Override
    public RequestDto getRequestById(Long userId, Long requestId) {
        User user = userRepository.getUserById(userId);
        Request request = requestRepository.getRequestsById(requestId);
        if (user == null) {
            log.error("Пользователь не найден");
            throw new NotFoundException("Пользователь не найден");
        }
        if (request == null) {
            log.error("Запрос на вещь не найден");
            throw new NotFoundException("Запрос на вещь не найден");
        }
        RequestDto requestDto = requestMapper.mapFromRequest(request);
        addItems(requestDto);
        return requestDto;
    }


    private void addItems(RequestDto requestDto) {
        List<ItemDto> items = itemRepository.getItemsByRequestId(requestDto.getId()).stream()
                .map(itemMapper::mapFromItem)
                .collect(Collectors.toList());
        requestDto.setItems(items);
    }

    private List<RequestDto> settingRequestDtoList(List<Request> requests) {
        List<RequestDto> requestsDto = requests.stream()
                .map(requestMapper::mapFromRequest)
                .collect(Collectors.toList());
        requestsDto.forEach(this::addItems);
        return requestsDto;
    }
}
