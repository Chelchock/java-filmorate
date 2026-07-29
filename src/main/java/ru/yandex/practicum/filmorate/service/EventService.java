package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;

    public void add(Long userId, EventType eventType, Operation operation) {
        Event event = new Event();
        event.setUserId(userId);
        event.setTimestamp(System.currentTimeMillis());
        event.setEventType(eventType);
        event.setOperation(operation);
        eventStorage.create(event);
    }

    public List<Event> findByUserId(Long userId) {
        return eventStorage.findByUserId(userId);
    }
}
