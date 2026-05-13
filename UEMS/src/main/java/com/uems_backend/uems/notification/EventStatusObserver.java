package com.uems_backend.uems.notification;

import com.uems_backend.uems.model.Event;

public interface EventStatusObserver {
    void onStatusChanged(Event event);
}
