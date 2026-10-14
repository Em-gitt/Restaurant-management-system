package com.unipoli.report.repository;

import com.unipoli.report.model.SalesProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesProjectionRepository extends JpaRepository<SalesProjection, Long> {

    Optional<SalesProjection> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<SalesProjection> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    List<SalesProjection> findAllByOrderByOrderDateDesc();
}
