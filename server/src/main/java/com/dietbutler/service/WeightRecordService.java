package com.dietbutler.service;

import com.dietbutler.entity.WeightRecord;
import com.dietbutler.repository.WeightRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;

    public WeightRecord addWeight(Long userId, Double weight, String note, LocalTime sleepStart, LocalTime sleepEnd, LocalDate recordDate) {
        // recordDate为null时使用今天
        LocalDate targetDate = recordDate != null ? recordDate : LocalDate.now();
        // 检查当天是否已有记录，有则更新，无则新增
        List<WeightRecord> existing = weightRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtDesc(userId, targetDate);
        WeightRecord record;
        if (!existing.isEmpty()) {
            record = existing.get(0);
            record.setWeight(weight);
            record.setNote(note);
        } else {
            record = new WeightRecord();
            record.setUserId(userId);
            record.setWeight(weight);
            record.setNote(note);
            record.setRecordDate(targetDate);
        }
        record.setSleepStart(sleepStart);
        record.setSleepEnd(sleepEnd);
        return weightRecordRepository.save(record);
    }

    public List<WeightRecord> getHistory(Long userId) {
        return weightRecordRepository.findByUserIdOrderByRecordDateDesc(userId);
    }

    public List<WeightRecord> getRecent(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, end);
    }

    public Optional<WeightRecord> getToday(Long userId) {
        List<WeightRecord> records = weightRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtDesc(userId, LocalDate.now());
        return records.isEmpty() ? Optional.empty() : Optional.of(records.get(0));
    }

    public WeightRecord updateWeight(Long id, Double weight, String note, LocalTime sleepStart, LocalTime sleepEnd, LocalDate recordDate) {
        Optional<WeightRecord> opt = weightRecordRepository.findById(id);
        if (opt.isEmpty()) return null;
        WeightRecord record = opt.get();
        if (weight != null) record.setWeight(weight);
        if (note != null) record.setNote(note);
        if (sleepStart != null) record.setSleepStart(sleepStart);
        if (sleepEnd != null) record.setSleepEnd(sleepEnd);
        if (recordDate != null) record.setRecordDate(recordDate);
        return weightRecordRepository.save(record);
    }

    public void deleteWeight(Long id) {
        weightRecordRepository.deleteById(id);
    }

    public WeightStatistics getStatistics(Long userId) {
        List<WeightRecord> records = getHistory(userId);
        if (records.isEmpty()) {
            return new WeightStatistics();
        }

        WeightRecord latest = records.get(0);
        WeightRecord oldest = records.get(records.size() - 1);

        double change = latest.getWeight() - oldest.getWeight();

        WeightStatistics stats = new WeightStatistics();
        stats.setLatestWeight(latest.getWeight());
        stats.setChange(change);
        stats.setRecordCount(records.size());
        stats.setStartWeight(oldest.getWeight());

        return stats;
    }

    @lombok.Data
    public static class WeightStatistics {
        private Double latestWeight;
        private Double change;
        private Double startWeight;
        private Integer recordCount;
    }
}
