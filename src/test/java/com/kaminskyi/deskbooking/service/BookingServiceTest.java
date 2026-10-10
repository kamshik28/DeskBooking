package com.kaminskyi.deskbooking.service;

import com.kaminskyi.deskbooking.config.AppProperties;
import com.kaminskyi.deskbooking.dto.BookingRequest;
import com.kaminskyi.deskbooking.dto.BookingResponse;
import com.kaminskyi.deskbooking.entity.*;
import com.kaminskyi.deskbooking.exception.BadRequestException;
import com.kaminskyi.deskbooking.exception.ConflictException;
import com.kaminskyi.deskbooking.exception.NotFoundException;
import com.kaminskyi.deskbooking.repository.BookingRepository;
import com.kaminskyi.deskbooking.repository.DeskRepository;
import com.kaminskyi.deskbooking.repository.UserRepository;
import com.kaminskyi.deskbooking.security.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2025, 9, 15);

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private DeskRepository deskRepository;
    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;

    private final AuthUser john = new AuthUser(1L, "john@test.com", Role.USER);
    private final AuthUser admin = new AuthUser(99L, "admin@test.com", Role.ADMIN);
    private Desk desk;
    private User johnEntity;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        AppProperties props = new AppProperties(null, new AppProperties.Booking(30), null);
        bookingService = new BookingService(bookingRepository, deskRepository, userRepository, props, clock);

        Office office = new Office();
        office.setId(1L);
        office.setName("Main");

        desk = new Desk();
        desk.setId(10L);
        desk.setName("A-101");
        desk.setFloor(1);
        desk.setOffice(office);

        johnEntity = new User();
        johnEntity.setId(1L);
        johnEntity.setFullName("John Doe");
    }

    @Test
    void createsBookingWhenDeskIsFree() {
        LocalDate date = TODAY.plusDays(1);
        when(deskRepository.findById(10L)).thenReturn(Optional.of(desk));
        when(userRepository.findById(1L)).thenReturn(Optional.of(johnEntity));
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(5L);
            return b;
        });

        BookingResponse result = bookingService.create(john, new BookingRequest(10L, date));

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.date()).isEqualTo(date);
        assertThat(result.status()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(result.deskName()).isEqualTo("A-101");
    }

    @Test
    void cantBookInThePast() {
        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY.minusDays(1))))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(deskRepository);
    }

    @Test
    void cantBookTooFarAhead() {
        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY.plusDays(31))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("30");
    }

    @Test
    void deskNotFound() {
        when(deskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void cantBookInactiveDesk() {
        desk.setActive(false);
        when(deskRepository.findById(10L)).thenReturn(Optional.of(desk));

        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cantBookAlreadyBookedDesk() {
        when(deskRepository.findById(10L)).thenReturn(Optional.of(desk));
        when(bookingRepository.existsByDeskIdAndDateAndStatus(10L, TODAY, BookingStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void userCantHaveTwoBookingsSameDay() {
        when(deskRepository.findById(10L)).thenReturn(Optional.of(desk));
        when(bookingRepository.existsByUserIdAndDateAndStatus(1L, TODAY, BookingStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.create(john, new BookingRequest(10L, TODAY)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void ownerCanCancel() {
        Booking booking = booking(TODAY.plusDays(2), BookingStatus.ACTIVE);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        BookingResponse result = bookingService.cancel(john, 7L);

        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void otherUserCantCancel() {
        Booking booking = booking(TODAY.plusDays(2), BookingStatus.ACTIVE);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        AuthUser stranger = new AuthUser(2L, "other@test.com", Role.USER);

        assertThatThrownBy(() -> bookingService.cancel(stranger, 7L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adminCanCancelAnyBooking() {
        Booking booking = booking(TODAY, BookingStatus.ACTIVE);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.cancel(admin, 7L).status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cantCancelTwice() {
        Booking booking = booking(TODAY.plusDays(1), BookingStatus.CANCELLED);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(john, 7L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cantCancelPastBooking() {
        Booking booking = booking(TODAY.minusDays(1), BookingStatus.ACTIVE);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(john, 7L))
                .isInstanceOf(BadRequestException.class);
    }

    private Booking booking(LocalDate date, BookingStatus status) {
        Booking b = new Booking();
        b.setId(7L);
        b.setDesk(desk);
        b.setUser(johnEntity);
        b.setDate(date);
        b.setStatus(status);
        return b;
    }
}
