package com.carrental.repository;

import com.carrental.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.cancelled = false
              AND r.startDate <= :endDate
              AND r.endDate >= :startDate
            ORDER BY r.startDate ASC
            """)
    List<Reservation> findActiveOverlapping(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
