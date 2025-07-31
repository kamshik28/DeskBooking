package org.example.repository;

import org.example.entity.Desk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DeskRepository extends JpaRepository<Desk, Long>,
        JpaSpecificationExecutor<Desk> {
}
