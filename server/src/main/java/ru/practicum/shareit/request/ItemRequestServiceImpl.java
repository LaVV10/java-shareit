package ru.practicum.shareit.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestIncomingDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestServiceImpl(ItemRequestRepository requestRepository,
                                  UserRepository userRepository,
                                  ItemRepository itemRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestIncomingDto requestDto) {
        User user = getUserOrThrow(userId);
        ItemRequest request = ItemRequestMapper.toEntity(requestDto);
        request.setRequester(user);
        request.setCreated(LocalDateTime.now());
        ItemRequest saved = requestRepository.save(request);
        return ItemRequestMapper.toDto(saved);
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = requestRepository.findByRequester_IdOrderByIdDesc(userId);
        return mapToDtoWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());
        List<ItemRequest> requests = requestRepository.findByRequester_IdNot(userId, pageable);
        return mapToDtoWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new UserNotFoundException("Запрос не найден"));
        ItemRequestDto dto = ItemRequestMapper.toDto(request);
        dto.setItems(getItemsForRequest(requestId));
        return dto;
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID=" + userId + " не найден"));
    }

    private List<ItemRequestDto> mapToDtoWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) return List.of();
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<ItemRequestDto.ItemRequestItemDto>> itemsMap = getItemsByRequestId(requestIds);
        return requests.stream().map(req -> {
            ItemRequestDto dto = ItemRequestMapper.toDto(req);
            dto.setItems(itemsMap.getOrDefault(req.getId(), List.of()));
            return dto;
        }).collect(Collectors.toList());
    }

    private Map<Long, List<ItemRequestDto.ItemRequestItemDto>> getItemsByRequestId(List<Long> requestIds) {
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        return items.stream()
                .map(item -> ItemRequestDto.ItemRequestItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwnerId())
                        .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                        .build())
                .collect(Collectors.groupingBy(ItemRequestDto.ItemRequestItemDto::getRequestId));
    }

    private List<ItemRequestDto.ItemRequestItemDto> getItemsForRequest(Long requestId) {
        return itemRepository.findByRequestId(requestId).stream()
                .map(item -> ItemRequestDto.ItemRequestItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwnerId())
                        .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }
}


