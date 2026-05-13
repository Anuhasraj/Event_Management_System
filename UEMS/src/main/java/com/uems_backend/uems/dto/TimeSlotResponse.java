package com.uems_backend.uems.dto;

import java.time.LocalTime;

public record TimeSlotResponse(LocalTime startTime, LocalTime endTime) {
}
