package com.dietbutler.service;

import com.dietbutler.entity.ExerciseRecord;
import com.dietbutler.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseRecordService {

    private final ExerciseRecordRepository exerciseRecordRepository;

    public ExerciseRecord addRecord(Long userId, String type, Integer duration, Integer calories,
                                    LocalDate recordDate, java.time.LocalTime startTime, java.time.LocalTime endTime, String note) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setDuration(duration);
        record.setCalories(calories);
        record.setRecordDate(recordDate);
        record.setStartTime(startTime);
        record.setEndTime(endTime);
        record.setNote(note);
        return exerciseRecordRepository.save(record);
    }

    public List<ExerciseRecord> getHistory(Long userId) {
        return exerciseRecordRepository.findByUserIdOrderByRecordDateDescCreatedAtDesc(userId);
    }

    public List<ExerciseRecord> getByDate(Long userId, LocalDate date) {
        return exerciseRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtDesc(userId, date);
    }

    public List<ExerciseRecord> getRecent(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        return exerciseRecordRepository.findRecentByUserId(userId, startDate);
    }

    public ExerciseRecord updateRecord(Long id, String type, Integer duration, Integer calories,
                                       LocalDate recordDate, java.time.LocalTime startTime, java.time.LocalTime endTime, String note) {
        return exerciseRecordRepository.findById(id)
                .map(record -> {
                    if (type != null) record.setType(type);
                    if (duration != null) record.setDuration(duration);
                    if (calories != null) record.setCalories(calories);
                    if (recordDate != null) record.setRecordDate(recordDate);
                    if (startTime != null) record.setStartTime(startTime);
                    if (endTime != null) record.setEndTime(endTime);
                    if (note != null) record.setNote(note);
                    return exerciseRecordRepository.save(record);
                })
                .orElse(null);
    }

    public void deleteRecord(Long id) {
        exerciseRecordRepository.deleteById(id);
    }

    public Integer getTotalCaloriesByDate(Long userId, LocalDate date) {
        return exerciseRecordRepository.sumCaloriesByUserIdAndDate(userId, date);
    }

    public Integer getTotalDurationByDate(Long userId, LocalDate date) {
        return exerciseRecordRepository.sumDurationByUserIdAndDate(userId, date);
    }

    public List<LocalDate> getRecordedDates(Long userId) {
        return exerciseRecordRepository.findDistinctDatesByUserId(userId);
    }

    @lombok.Data
    @lombok.Builder
    public static class ExerciseSummary {
        private LocalDate date;
        private List<ExerciseRecord> records;
        private Integer totalDuration;
        private Integer totalCalories;
    }

    @lombok.Data
    public static class ExerciseData {
        public LocalDate recordDate;
        public String type;
        public Integer duration;
        public Integer calories;
        public String note;
    }

    public ExerciseSummary addExercise(Long userId, String type, Integer duration, Integer calories,
                                      LocalDate recordDate, String note) {
        ExerciseRecord record = new ExerciseRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setDuration(duration);
        record.setCalories(calories);
        record.setRecordDate(recordDate);
        record.setNote(note);
        ExerciseRecord saved = exerciseRecordRepository.save(record);

        List<ExerciseRecord> records = getByDate(userId, recordDate);
        Integer totalDuration = getTotalDurationByDate(userId, recordDate);
        Integer totalCalories = getTotalCaloriesByDate(userId, recordDate);

        return ExerciseSummary.builder()
                .date(recordDate)
                .records(records)
                .totalDuration(totalDuration)
                .totalCalories(totalCalories)
                .build();
    }

    public ExerciseSummary getDailySummary(Long userId, LocalDate date) {
        List<ExerciseRecord> records = getByDate(userId, date);
        Integer totalDuration = getTotalDurationByDate(userId, date);
        Integer totalCalories = getTotalCaloriesByDate(userId, date);
        return ExerciseSummary.builder()
                .date(date)
                .records(records)
                .totalDuration(totalDuration)
                .totalCalories(totalCalories)
                .build();
    }
}