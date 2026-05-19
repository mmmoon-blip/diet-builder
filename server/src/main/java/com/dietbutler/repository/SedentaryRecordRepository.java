package com.dietbutler.repository;

import com.dietbutler.entity.SedentaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SedentaryRecordRepository extends JpaRepository<SedentaryRecord, Long> {

    Optional<SedentaryRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
}