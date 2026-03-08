package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestIncomingDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestIncomingDto requestDto);

    List<ItemRequestDto> getOwnRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
