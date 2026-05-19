package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.MenstrualRecord;
import com.dietbutler.service.MenstrualService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/menstrual")
@RequiredArgsConstructor
public class MenstrualController {

    private final MenstrualService menstrualService;

    @GetMapping("/{userId}")
    public ApiResponse<List<MenstrualRecord>> getHistory(@PathVariable Long userId) {
        List<MenstrualRecord> records = menstrualService.getHistory(userId);
        return ApiResponse.success(records);
    }

    @GetMapping("/{userId}/phase")
    public ApiResponse<MenstrualService.CyclePhase> getCurrentPhase(@PathVariable Long userId) {
        MenstrualService.CyclePhase phase = menstrualService.getCurrentPhase(userId);
        return ApiResponse.success(phase);
    }

    @PostMapping
    public ApiResponse<MenstrualRecord> add(@RequestBody AddMenstrualRequest request) {
        MenstrualRecord record = menstrualService.addRecord(
                request.getUserId(),
                request.getCycleStartDate(),
                request.getCycleEndDate(),
                request.getFlowLevel(),
                request.getIsInPeriod(),
                request.getHasPain(),
                request.getOtherInfo()
        );
        return ApiResponse.success("经期记录成功", record);
    }

    @PutMapping("/{id}")
    public ApiResponse<MenstrualRecord> update(@PathVariable Long id, @RequestBody AddMenstrualRequest request) {
        MenstrualRecord record = menstrualService.updateRecord(id,
                request.getCycleStartDate(), request.getCycleEndDate(),
                request.getFlowLevel(), request.getIsInPeriod(),
                request.getHasPain(), request.getOtherInfo());
        if (record == null) return ApiResponse.error("记录不存在");
        return ApiResponse.success("更新成功", record);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menstrualService.deleteRecord(id);
        return ApiResponse.success("删除成功", null);
    }

    @lombok.Data
    public static class AddMenstrualRequest {
        private Long userId;
        private LocalDate cycleStartDate;
        private LocalDate cycleEndDate;
        private String flowLevel;  // light/medium/heavy
        private Boolean isInPeriod;
        private Boolean hasPain;
        private String otherInfo;
    }
}
