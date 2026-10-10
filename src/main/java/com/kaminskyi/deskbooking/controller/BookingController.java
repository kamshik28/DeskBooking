package com.kaminskyi.deskbooking.controller;

import com.kaminskyi.deskbooking.dto.BookingRequest;
import com.kaminskyi.deskbooking.dto.BookingResponse;
import com.kaminskyi.deskbooking.security.AuthUser;
import com.kaminskyi.deskbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@AuthenticationPrincipal AuthUser user,
                                  @Valid @RequestBody BookingRequest request) {
        return bookingService.create(user, request);
    }

    @GetMapping("/my")
    public List<BookingResponse> myBookings(@AuthenticationPrincipal AuthUser user) {
        return bookingService.getMyUpcoming(user);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return bookingService.cancel(user, id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> forDay(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                        @RequestParam(required = false) Long officeId) {
        return bookingService.getForDay(date, officeId);
    }
}
