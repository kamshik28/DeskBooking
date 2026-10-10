package com.kaminskyi.deskbooking.repository;

import com.kaminskyi.deskbooking.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficeRepository extends JpaRepository<Office, Long> {
}
