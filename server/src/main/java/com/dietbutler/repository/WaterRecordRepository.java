package com.dietbutler.repository;

import com.dietbutler.entity.WaterRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaterRecordRepository extends JpaRepository<WaterRecord, Long> {
    List<WaterRecord> findByUserIdAndRecordDateOrderByCreatedAtDesc(Long userId, LocalDate recordDate);
    List<WaterRecord> findByUserIdOrderByRecordDateDesc(Long userId);
}