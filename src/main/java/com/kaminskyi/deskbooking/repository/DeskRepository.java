package com.kaminskyi.deskbooking.repository;

import com.kaminskyi.deskbooking.entity.Desk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DeskRepository extends JpaRepository<Desk, Long>, JpaSpecificationExecutor<Desk> {

    @Override
    @EntityGraph(attributePaths = "office")
    Page<Desk> findAll(Specification<Desk> spec, Pageable pageable);

    boolean existsByOfficeId(Long officeId);

    boolean existsByOfficeIdAndNameIgnoreCase(Long officeId, String name);

    @EntityGraph(attributePaths = "office")
    @Query("""
            select d from Desk d
            where d.office.id = :officeId
              and d.active = true
              and not exists (
                  select b.id from Booking b
                  where b.desk = d and b.date = :date and b.status = com.kaminskyi.deskbooking.entity.BookingStatus.ACTIVE
              )
            order by d.floor, d.name
            """)
    List<Desk> findAvailable(@Param("officeId") Long officeId, @Param("date") LocalDate date);
}
