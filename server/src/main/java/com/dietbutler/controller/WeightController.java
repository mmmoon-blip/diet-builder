package com.dietbutler.controller;

import com.dietbutler.dto.AddWeightRequest;
import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.WeightRecord;
import com.dietbutler.service.WeightRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weight")
@RequiredArgsConstructor
public class WeightController {

    private final WeightRecordService weightRecordService;

    @PostMapping("/add")
    public ApiResponse<WeightRecord> addWeight(@RequestParam Long userId, @RequestBody AddWeightRequest request) {
        if (request.getWeight() == null) {
            return ApiResponse.error("体重不能为空");
        }
        WeightRecord record = weightRecordService.addWeight(userId, request.getWeight(), request.getNote(),
                request.getSleepStart(), request.getSleepEnd(), request.getRecordDate());
        return ApiResponse.success("体重记录成功", record);
    }

    @GetMapping("/history")
    public ApiResponse<List<WeightRecord>> getHistory(@RequestParam Long userId, @RequestParam(defaultValue = "0") int days) {
        List<WeightRecord> records;
        if (days > 0) {
            records = weightRecordService.getRecent(userId, days);
        } else {
            records = weightRecordService.getHistory(userId);
        }
        return ApiResponse.success(records);
    }

    @GetMapping("/recent")
    public ApiResponse<List<WeightRecord>> getRecent(@RequestParam Long userId, @RequestParam(defaultValue = "7") int days) {
        List<WeightRecord> records = weightRecordService.getRecent(userId, days);
        return ApiResponse.success(records);
    }

    @GetMapping("/statistics")
    public ApiResponse<WeightRecordService.WeightStatistics> getStatistics(@RequestParam Long userId) {
        WeightRecordService.WeightStatistics stats = weightRecordService.getStatistics(userId);
        return ApiResponse.success(stats);
    }

    @PutMapping("/{id}")
    public ApiResponse<WeightRecord> updateWeight(@PathVariable Long id, @RequestBody AddWeightRequest request) {
        WeightRecord record = weightRecordService.updateWeight(id, request.getWeight(), request.getNote(),
                request.getSleepStart(), request.getSleepEnd(), request.getRecordDate());
        if (record == null) {
            return ApiResponse.error("记录不存在");
        }
        return ApiResponse.success("更新成功", record);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWeight(@PathVariable Long id) {
        weightRecordService.deleteWeight(id);
        return ApiResponse.success("删除成功", null);
    }
}