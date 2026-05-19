package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.WaterRecord;
import com.dietbutler.service.WaterRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterRecordService waterRecordService;

    @PostMapping
    public ApiResponse<WaterRecord> addWater(@RequestParam Long userId,
                                            @RequestParam Integer amount,
                                            @RequestParam(required = false) LocalDate recordDate) {
        if (amount == null || amount <= 0) {
            return ApiResponse.error("喝水量必须大于0");
        }
        WaterRecord record = waterRecordService.addWater(userId, amount, recordDate);
        return ApiResponse.success("记录成功", record);
    }

    @GetMapping("/today")
    public ApiResponse<Map<String, Object>> getTodayWater(@RequestParam Long userId) {
        Integer total = waterRecordService.getTodayTotal(userId);
        List<WaterRecord> records = waterRecordService.getTodayRecords(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("records", records);
        return ApiResponse.success(data);
    }

    @GetMapping("/history")
    public ApiResponse<List<WaterRecord>> getHistory(@RequestParam Long userId) {
        List<WaterRecord> records = waterRecordService.getHistory(userId);
        return ApiResponse.success(records);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        waterRecordService.deleteRecord(id);
        return ApiResponse.success("删除成功", null);
    }
}