package com.dietbutler.repository;

import com.dietbutler.entity.DietRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {

    List<DietRecord> findByUserIdOrderByRecordDateDescCreatedAtDesc(Long userId);

    List<DietRecord> findByUserIdAndRecordDateOrderByCreatedAtDesc(Long userId, LocalDate recordDate);

    List<DietRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT d FROM DietRecord d WHERE d.userId = :userId AND d.recordDate >= :startDate ORDER BY d.recordDate DESC, d.createdAt DESC")
    List<DietRecord> findRecentByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(d.calories), 0) FROM DietRecord d WHERE d.userId = :userId AND d.recordDate = :date")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT d.recordDate FROM DietRecord d WHERE d.userId = :userId ORDER BY d.recordDate DESC")
    List<LocalDate> findDistinctDatesByUserId(@Param("userId") Long userId);
}