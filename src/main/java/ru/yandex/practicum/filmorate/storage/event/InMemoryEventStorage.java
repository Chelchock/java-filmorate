package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryEventStorage implements EventStorage {
    private final Map<Long, List<Event>> eventsByUser = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Event create(Event event) {
        event.setEventId(nextId.getAndIncrement());
        eventsByUser.computeIfAbsent(event.getUserId(), id -> new ArrayList<>()).add(event);
        return event;
    }

    @Override
    public List<Event> findByUserId(Long userId) {
        return eventsByUser.getOrDefault(userId, List.of()).stream()
                .sorted(Comparator.comparing(Event::getEventId))
                .toList();
    }
}
