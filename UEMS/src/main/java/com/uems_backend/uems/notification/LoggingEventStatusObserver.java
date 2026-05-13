package com.uems_backend.uems.notification;

import com.uems_backend.uems.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventStatusObserver implements EventStatusObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventStatusObserver.class);

    @Override
    public void onStatusChanged(Event event) {
        LOGGER.info("Event '{}' for organizer '{}' changed to {}",
                event.getEventName(),
                event.getOrganizer().getUsername(),
                event.getStatus());
    }
}
