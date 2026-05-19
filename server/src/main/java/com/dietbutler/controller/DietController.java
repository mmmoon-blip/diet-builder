package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.DietRecord;
import com.dietbutler.service.DietRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietRecordService dietRecordService;

    @GetMapping("/{userId}")
    public ApiResponse<List<DietRecord>> getHistory(@PathVariable Long userId,
                                                    @RequestParam(defaultValue = "0") int days) {
        List<DietRecord> records;
        if (days > 0) {
            records = dietRecordService.getRecent(userId, days);
        } else {
            records = dietRecordService.getHistory(userId);
        }
        return ApiResponse.success(records);
    }

    @GetMapping("/{userId}/date/{date}")
    public ApiResponse<DietRecordService.DietSummary> getByDate(@PathVariable Long userId,
                                                                  @PathVariable LocalDate date) {
        return ApiResponse.success(dietRecordService.getDailySummary(userId, date));
    }

    @GetMapping("/{userId}/dates")
    public ApiResponse<List<LocalDate>> getRecordedDates(@PathVariable Long userId) {
        return ApiResponse.success(dietRecordService.getRecordedDates(userId));
    }

    @PostMapping
    public ApiResponse<DietRecord> add(@RequestBody DietRequest request) {
        DietRecord r = dietRecordService.addRecord(
                request.getUserId(),
                request.getMealType(),
                request.getFoods(),
                request.getCalories(),
                request.getImageUrl(),
                request.getRecordDate() != null ? request.getRecordDate() : LocalDate.now(),
                request.getStartTime(),
                request.getEndTime(),
                request.getNote()
        );
        return ApiResponse.success("饮食记录成功", r);
    }

    @PutMapping("/{id}")
    public ApiResponse<DietRecord> update(@PathVariable Long id, @RequestBody DietRequest request) {
        DietRecord r = dietRecordService.updateRecord(id,
                request.getMealType(), request.getFoods(), request.getCalories(),
                request.getImageUrl(), request.getRecordDate(),
                request.getStartTime(), request.getEndTime(), request.getNote());
        if (r == null) return ApiResponse.error("记录不存在");
        return ApiResponse.success("更新成功", r);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dietRecordService.deleteRecord(id);
        return ApiResponse.success("删除成功", null);
    }

    @lombok.Data
    public static class DietRequest {
        private Long userId;
        private String mealType;
        private String foods;
        private Integer calories;
        private String imageUrl;
        private LocalDate recordDate;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private String note;
    }
}