package com.dietbutler.service;

import com.dietbutler.entity.WaterRecord;
import com.dietbutler.repository.WaterRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterRecordService {

    private final WaterRecordRepository waterRecordRepository;

    public WaterRecord addWater(Long userId, Integer amount, LocalDate recordDate) {
        WaterRecord record = new WaterRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setRecordDate(recordDate != null ? recordDate : LocalDate.now());
        record.setCreatedAt(System.currentTimeMillis());
        return waterRecordRepository.save(record);
    }

    public List<WaterRecord> getTodayRecords(Long userId) {
        return waterRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtDesc(userId, LocalDate.now());
    }

    public List<WaterRecord> getHistory(Long userId) {
        return waterRecordRepository.findByUserIdOrderByRecordDateDesc(userId);
    }

    public Integer getTodayTotal(Long userId) {
        List<WaterRecord> records = getTodayRecords(userId);
        return records.stream().mapToInt(WaterRecord::getAmount).sum();
    }

    public void deleteRecord(Long id) {
        waterRecordRepository.deleteById(id);
    }
}