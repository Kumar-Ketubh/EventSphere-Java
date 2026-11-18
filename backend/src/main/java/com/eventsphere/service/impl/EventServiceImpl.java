package com.eventsphere.service.impl;

import com.eventsphere.dto.DashboardResponse;
import com.eventsphere.dto.EventResponse;
import com.eventsphere.entity.Event;
import com.eventsphere.repository.EventRepository;
import com.eventsphere.service.EventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link EventService} providing dynamic event categorization.
 */
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public DashboardResponse getDashboardData() {
        LocalDateTime now = LocalDateTime.now();

        List<EventResponse> live = eventRepository.findLiveEvents(now)
                .stream().map(this::mapToEventResponse).collect(Collectors.toList());

        List<EventResponse> upcoming = eventRepository.findUpcomingEvents(now)
                .stream().map(this::mapToEventResponse).collect(Collectors.toList());

        List<EventResponse> previous = eventRepository.findPreviousEvents(now)
                .stream().map(this::mapToEventResponse).collect(Collectors.toList());

        return DashboardResponse.builder()
                .live(live)
                .upcoming(upcoming)
                .previous(previous)
                .build();
    }

    private EventResponse mapToEventResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .venue(event.getVenue())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .totalSeats(event.getTotalSeats())
                .bookedSeats(event.getBookedSeats())
                .build();
    }
}
