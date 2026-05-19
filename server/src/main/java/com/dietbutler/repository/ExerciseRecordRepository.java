package com.dietbutler.repository;

import com.dietbutler.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    List<ExerciseRecord> findByUserIdOrderByRecordDateDescCreatedAtDesc(Long userId);

    List<ExerciseRecord> findByUserIdAndRecordDateOrderByCreatedAtDesc(Long userId, LocalDate recordDate);

    List<ExerciseRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM ExerciseRecord e WHERE e.userId = :userId AND e.recordDate >= :startDate ORDER BY e.recordDate DESC, e.createdAt DESC")
    List<ExerciseRecord> findRecentByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Query("SELECT COALESCE(SUM(e.calories), 0) FROM ExerciseRecord e WHERE e.userId = :userId AND e.recordDate = :date")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.duration), 0) FROM ExerciseRecord e WHERE e.userId = :userId AND e.recordDate = :date")
    Integer sumDurationByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT e.recordDate FROM ExerciseRecord e WHERE e.userId = :userId ORDER BY e.recordDate DESC")
    List<LocalDate> findDistinctDatesByUserId(@Param("userId") Long userId);
}