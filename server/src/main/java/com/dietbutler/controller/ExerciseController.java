package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.ExerciseRecord;
import com.dietbutler.service.ExerciseRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseRecordService exerciseRecordService;

    @GetMapping("/{userId}")
    public ApiResponse<List<ExerciseRecord>> getHistory(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "0") int days) {
        List<ExerciseRecord> records;
        if (days > 0) {
            records = exerciseRecordService.getRecent(userId, days);
        } else {
            records = exerciseRecordService.getHistory(userId);
        }
        return ApiResponse.success(records);
    }

    @GetMapping("/{userId}/date/{date}")
    public ApiResponse<ExerciseRecordService.ExerciseSummary> getByDate(@PathVariable Long userId,
                                                                        @PathVariable LocalDate date) {
        return ApiResponse.success(exerciseRecordService.getDailySummary(userId, date));
    }

    @GetMapping("/{userId}/dates")
    public ApiResponse<List<LocalDate>> getRecordedDates(@PathVariable Long userId) {
        return ApiResponse.success(exerciseRecordService.getRecordedDates(userId));
    }

    @PostMapping
    public ApiResponse<ExerciseRecord> add(@RequestBody ExerciseRequest request) {
        ExerciseRecord r = exerciseRecordService.addRecord(
                request.getUserId(),
                request.getType(),
                request.getDuration(),
                request.getCalories(),
                request.getRecordDate() != null ? request.getRecordDate() : LocalDate.now(),
                request.getStartTime(),
                request.getEndTime(),
                request.getNote()
        );
        return ApiResponse.success("运动记录成功", r);
    }

    @PutMapping("/{id}")
    public ApiResponse<ExerciseRecord> update(@PathVariable Long id, @RequestBody ExerciseRequest request) {
        ExerciseRecord r = exerciseRecordService.updateRecord(id,
                request.getType(), request.getDuration(), request.getCalories(),
                request.getRecordDate(), request.getStartTime(), request.getEndTime(), request.getNote());
        if (r == null) return ApiResponse.error("记录不存在");
        return ApiResponse.success("更新成功", r);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        exerciseRecordService.deleteRecord(id);
        return ApiResponse.success("删除成功", null);
    }

    @lombok.Data
    public static class ExerciseRequest {
        private Long userId;
        private String type;
        private Integer duration;
        private Integer calories;
        private LocalDate recordDate;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private String note;
    }
}