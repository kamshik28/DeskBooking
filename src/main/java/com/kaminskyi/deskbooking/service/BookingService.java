package com.kaminskyi.deskbooking.service;

import com.kaminskyi.deskbooking.config.AppProperties;
import com.kaminskyi.deskbooking.dto.BookingRequest;
import com.kaminskyi.deskbooking.dto.BookingResponse;
import com.kaminskyi.deskbooking.entity.Booking;
import com.kaminskyi.deskbooking.entity.BookingStatus;
import com.kaminskyi.deskbooking.entity.Desk;
import com.kaminskyi.deskbooking.entity.User;
import com.kaminskyi.deskbooking.exception.BadRequestException;
import com.kaminskyi.deskbooking.exception.ConflictException;
import com.kaminskyi.deskbooking.exception.NotFoundException;
import com.kaminskyi.deskbooking.repository.BookingRepository;
import com.kaminskyi.deskbooking.repository.DeskRepository;
import com.kaminskyi.deskbooking.repository.UserRepository;
import com.kaminskyi.deskbooking.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final DeskRepository deskRepository;
    private final UserRepository userRepository;
    private final AppProperties props;
    private final Clock clock;

    @Transactional
    public BookingResponse create(AuthUser currentUser, BookingRequest request) {
        LocalDate date = request.date();
        validateDate(date);

        Desk desk = deskRepository.findById(request.deskId())
                .orElseThrow(() -> NotFoundException.of("Desk", request.deskId()));
        if (!desk.isActive()) {
            throw new BadRequestException("Desk is not available for booking");
        }
        if (bookingRepository.existsByDeskIdAndDateAndStatus(desk.getId(), date, BookingStatus.ACTIVE)) {
            throw new ConflictException("Desk is already booked for " + date);
        }
        if (bookingRepository.existsByUserIdAndDateAndStatus(currentUser.id(), date, BookingStatus.ACTIVE)) {
            throw new ConflictException("You already have a booking for " + date);
        }

        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> NotFoundException.of("User", currentUser.id()));

        Booking booking = new Booking();
        booking.setDesk(desk);
        booking.setUser(user);
        booking.setDate(date);
        booking.setStatus(BookingStatus.ACTIVE);

        // saveAndFlush so the unique index error is thrown here and not on commit
        return BookingResponse.from(bookingRepository.saveAndFlush(booking));
    }

    @Transactional
    public BookingResponse cancel(AuthUser currentUser, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> NotFoundException.of("Booking", bookingId));

        if (!currentUser.isAdmin() && !booking.getUser().getId().equals(currentUser.id())) {
            throw new AccessDeniedException("You can cancel only your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }
        if (booking.getDate().isBefore(today())) {
            throw new BadRequestException("Past bookings can't be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyUpcoming(AuthUser currentUser) {
        return bookingRepository.findByUserIdAndDateGreaterThanEqualOrderByDateAsc(currentUser.id(), today())
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getForDay(LocalDate date, Long officeId) {
        List<Booking> bookings = officeId == null
                ? bookingRepository.findByDateOrderByDeskNameAsc(date)
                : bookingRepository.findByDateAndDeskOfficeIdOrderByDeskNameAsc(date, officeId);
        return bookings.stream()
                .map(BookingResponse::from)
                .toList();
    }

    private void validateDate(LocalDate date) {
        LocalDate today = today();
        if (date.isBefore(today)) {
            throw new BadRequestException("Can't book a desk in the past");
        }
        int maxDays = props.booking().maxDaysAhead();
        if (date.isAfter(today.plusDays(maxDays))) {
            throw new BadRequestException("Booking is possible max " + maxDays + " days ahead");
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
