package com.dietbutler.repository;

import com.dietbutler.entity.EmotionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionLogRepository extends JpaRepository<EmotionLog, Long> {

    List<EmotionLog> findByUserIdAndRecordDateBetweenOrderByCreatedAtDesc(Long userId, LocalDate start, LocalDate end);

    Optional<EmotionLog> findFirstByUserIdAndRecordDateOrderByCreatedAtDesc(Long userId, LocalDate recordDate);

    List<EmotionLog> findByUserIdAndAiIntervenedFalseAndCreatedAtAfter(Long userId, java.time.LocalDateTime after);
}