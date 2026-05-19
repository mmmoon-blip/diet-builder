package com.dietbutler.service;

import com.dietbutler.entity.DietRecord;
import com.dietbutler.repository.DietRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietRecordService {

    private final DietRecordRepository dietRecordRepository;

    public DietRecord addRecord(Long userId, String mealType, String foods, Integer calories,
                                String imageUrl, LocalDate recordDate,
                                java.time.LocalTime startTime, java.time.LocalTime endTime, String note) {
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setMealType(mealType);
        record.setFoods(foods);
        record.setCalories(calories);
        record.setImageUrl(imageUrl);
        record.setRecordDate(recordDate);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setNote(note);
        return dietRecordRepository.save(record);
    }

    public List<DietRecord> getHistory(Long userId) {
        return dietRecordRepository.findByUserIdOrderByRecordDateDescCreatedAtDesc(userId);
    }

    public List<DietRecord> getByDate(Long userId, LocalDate date) {
        return dietRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtDesc(userId, date);
    }

    public List<DietRecord> getRecent(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return dietRecordRepository.findRecentByUserId(userId, startDate);
    }

    public DietRecord updateRecord(Long id, String mealType, String foods, Integer calories,
                                   String imageUrl, LocalDate recordDate,
                                   java.time.LocalTime startTime, java.time.LocalTime endTime, String note) {
        return dietRecordRepository.findById(id)
                .map(record -> {
                    if (mealType != null) record.setMealType(mealType);
                    if (foods != null) record.setFoods(foods);
                    if (calories != null) record.setCalories(calories);
                    if (imageUrl != null) record.setImageUrl(imageUrl);
                    if (recordDate != null) record.setRecordDate(recordDate);
                    if (startTime != null) record.setStartTime(startTime);
                    if (endTime != null) record.setEndTime(endTime);
                    if (note != null) record.setNote(note);
                    return dietRecordRepository.save(record);
                })
                .orElse(null);
    }

    public void deleteRecord(Long id) {
        dietRecordRepository.deleteById(id);
    }

    public Integer getTotalCaloriesByDate(Long userId, LocalDate date) {
        return dietRecordRepository.sumCaloriesByUserIdAndDate(userId, date);
    }

    public List<LocalDate> getRecordedDates(Long userId) {
        return dietRecordRepository.findDistinctDatesByUserId(userId);
    }

    @lombok.Data
    @lombok.Builder
    public static class DietSummary {
        private LocalDate date;
        private List<DietRecord> records;
        private Integer totalCalories;
    }

    @lombok.Data
    public static class DietData {
        public LocalDate recordDate;
        public String mealType;
        public String foods;
        public Integer calories;
        public String note;
    }

    public DietSummary addDiet(Long userId, String mealType, String foods, Integer calories,
                               LocalDate recordDate, String note) {
        DietRecord record = new DietRecord();
        record.setUserId(userId);
        record.setMealType(mealType);
        record.setFoods(foods);
        record.setCalories(calories);
        record.setRecordDate(recordDate);
        record.setNote(note);
        DietRecord saved = dietRecordRepository.save(record);

        List<DietRecord> records = getByDate(userId, recordDate);
        Integer totalCalories = getTotalCaloriesByDate(userId, recordDate);

        return DietSummary.builder()
                .date(recordDate)
                .records(records)
                .totalCalories(totalCalories)
                .build();
    }

    public DietSummary getDailySummary(Long userId, LocalDate date) {
        List<DietRecord> records = getByDate(userId, date);
        Integer totalCalories = getTotalCaloriesByDate(userId, date);
        return DietSummary.builder()
                .date(date)
                .records(records)
                .totalCalories(totalCalories)
                .build();
    }
}