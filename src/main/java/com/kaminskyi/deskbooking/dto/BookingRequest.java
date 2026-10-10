package com.kaminskyi.deskbooking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull Long deskId,
        @NotNull LocalDate date
) {
}
