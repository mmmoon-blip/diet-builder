package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.BodyMeasurement;
import com.dietbutler.service.BodyMeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final BodyMeasurementService bodyMeasurementService;

    @GetMapping("/{userId}")
    public ApiResponse<List<BodyMeasurement>> getHistory(@PathVariable Long userId,
                                                        @RequestParam(defaultValue = "0") int days,
                                                        @RequestParam(required = false) Integer limit) {
        List<BodyMeasurement> records;
        if (limit != null && limit > 0) {
            records = bodyMeasurementService.getHistory(userId, limit);
        } else if (days > 0) {
            records = bodyMeasurementService.getRecent(userId, days);
        } else {
            records = bodyMeasurementService.getHistory(userId);
        }
        return ApiResponse.success(records);
    }

    @PostMapping
    public ApiResponse<BodyMeasurement> add(@RequestBody AddMeasurementRequest request) {
        BodyMeasurement m = bodyMeasurementService.addMeasurement(
                request.getUserId(),
                request.getWaist(),
                request.getHip(),
                request.getChest(),
                request.getUpperArm(),
                request.getForearm(),
                request.getThigh(),
                request.getCalf(),
                request.getNote(),
                request.getRecordDate()
        );
        return ApiResponse.success("维度记录成功", m);
    }

    @PutMapping("/{id}")
    public ApiResponse<BodyMeasurement> update(@PathVariable Long id, @RequestBody AddMeasurementRequest request) {
        BodyMeasurement m = bodyMeasurementService.updateMeasurement(id,
                request.getWaist(), request.getHip(), request.getChest(),
                request.getUpperArm(), request.getForearm(),
                request.getThigh(), request.getCalf(), request.getRecordDate());
        if (m == null) return ApiResponse.error("记录不存在");
        return ApiResponse.success("更新成功", m);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bodyMeasurementService.deleteMeasurement(id);
        return ApiResponse.success("删除成功", null);
    }

    @lombok.Data
    public static class AddMeasurementRequest {
        private Long userId;
        private Double waist;
        private Double hip;
        private Double chest;
        private Double upperArm;
        private Double forearm;
        private Double thigh;
        private Double calf;
        private String note;
        private LocalDate recordDate;  // 可选，用于导入历史记录
    }
}
