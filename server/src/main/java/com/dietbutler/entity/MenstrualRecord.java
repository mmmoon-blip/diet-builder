package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "menstrual_records")
public class MenstrualRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate cycleStartDate;  // 经期开始日期（处于经期=本次开始，非经期=最近一次开始）

    private LocalDate cycleEndDate;    // 经期结束日期（处于经期时为空）

    private Integer cycleLength;        // 本次周期天数（end - start）

    // 经量: light(少)/medium(中)/heavy(多)
    private String flowLevel;

    // 是否处于经期
    private Boolean isInPeriod;

    // 是否疼痛
    private Boolean hasPain;

    // 其他信息/备注
    private String otherInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
