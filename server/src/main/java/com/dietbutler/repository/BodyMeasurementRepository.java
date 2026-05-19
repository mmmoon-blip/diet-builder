package com.dietbutler.repository;

import com.dietbutler.entity.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findByUserIdOrderByRecordDateDesc(Long userId);
    List<BodyMeasurement> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(Long userId, LocalDate start, LocalDate end);
    List<BodyMeasurement> findByUserIdAndRecordDateOrderByCreatedAtDesc(Long userId, LocalDate recordDate);
}
