package com.kaminskyi.deskbooking.repository;

import com.kaminskyi.deskbooking.entity.Booking;
import com.kaminskyi.deskbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByDeskIdAndDateAndStatus(Long deskId, LocalDate date, BookingStatus status);

    boolean existsByUserIdAndDateAndStatus(Long userId, LocalDate date, BookingStatus status);

    boolean existsByDeskId(Long deskId);

    @EntityGraph(attributePaths = {"desk", "desk.office", "user"})
    List<Booking> findByUserIdAndDateGreaterThanEqualOrderByDateAsc(Long userId, LocalDate from);

    @EntityGraph(attributePaths = {"desk", "desk.office", "user"})
    List<Booking> findByDateOrderByDeskNameAsc(LocalDate date);

    @EntityGraph(attributePaths = {"desk", "desk.office", "user"})
    List<Booking> findByDateAndDeskOfficeIdOrderByDeskNameAsc(LocalDate date, Long officeId);
}
