package com.kaminskyi.deskbooking.dto;

import com.kaminskyi.deskbooking.entity.Booking;
import com.kaminskyi.deskbooking.entity.BookingStatus;

import java.time.LocalDate;

public record BookingResponse(
        Long id,
        LocalDate date,
        BookingStatus status,
        Long deskId,
        String deskName,
        String officeName,
        Long userId,
        String userName
) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getDate(),
                booking.getStatus(),
                booking.getDesk().getId(),
                booking.getDesk().getName(),
                booking.getDesk().getOffice().getName(),
                booking.getUser().getId(),
                booking.getUser().getFullName()
        );
    }
}
