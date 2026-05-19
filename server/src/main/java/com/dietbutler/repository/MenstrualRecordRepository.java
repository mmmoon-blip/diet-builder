package com.dietbutler.repository;

import com.dietbutler.entity.MenstrualRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenstrualRecordRepository extends JpaRepository<MenstrualRecord, Long> {
    List<MenstrualRecord> findByUserIdOrderByCycleStartDateDesc(Long userId);
    Optional<MenstrualRecord> findFirstByUserIdOrderByCycleStartDateDesc(Long userId);
    Optional<MenstrualRecord> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
