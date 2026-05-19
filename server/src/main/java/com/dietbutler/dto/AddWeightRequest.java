package com.dietbutler.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AddWeightRequest {
    private Double weight;
    private String note;
    private LocalTime sleepStart;
    private LocalTime sleepEnd;
    private LocalDate recordDate;  // 可选，用于导入历史记录
}