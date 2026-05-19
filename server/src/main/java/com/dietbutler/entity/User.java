package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String openid;  // 微信 OpenID

    @Column(nullable = false)
    private String nickname = "减减用户";

    private String avatar;

    private Integer gender;  // 0:未知, 1:男, 2:女

    private Integer age;

    private Double height;  // 身高(cm)

    private Double targetWeight;  // 目标体重(kg)

    private Double initialWeight;  // 初始体重(kg)

    private Integer weightLossPeriod;  // 减重周期(天)

    private LocalDate startWeightDate;  // 开始减重日期

    // 饮食忌口（TEXT，逗号分隔，如"海鲜,辛辣,酒精"）
    private String dietaryTaboo;

    // 作息时间
    private LocalTime sleepStart;  // 睡眠开始时间
    private LocalTime sleepEnd;    // 起床时间

    private Boolean reminderEnabled = true;  // 是否启用体重提醒

    private Integer reminderIntervalHours = 24;  // 提醒间隔(小时)

    private Double basicMetabolism;  // 基础代谢(kcal)

    // 新增字段
    private String phone;  // 手机号（可选，用于用户联系方式）

    private String sessionKey;  // 微信session_key

    private LocalDateTime lastLoginAt;  // 最后登录时间

    // ============ 用户类型分类 ============
    // weight_loss: 减重, shaping: 塑形, maintenance: 维持, muscle_gain: 增肌
    @Column(nullable = false)
    private String userType = "weight_loss";

    // ============ 体质标签（多选，逗号分隔）============
    // 易水肿,代谢低,碳水敏感,压力胖,熬夜体质,宫寒,易胖体质,肌肉量低,膝盖不好
    private String constitutionTags;

    // ============ 运动习惯 ============
    private Integer exerciseFrequency;      // 每周运动次数
    private String exercisePreference;      // 居家,健身房,户外,游泳
    private Boolean hasKneeIssue;           // 是否有膝盖问题
    private String fitnessLevel;            // 新手,中级,高级

    // ============ 饮食习惯 ============
    private String dietPreference;          // 外卖多,自己做饭,清淡,重口
    private Boolean breakfastHabit = true; // 是否吃早餐
    private String mealTimes;              // 三餐时间如 8:00,12:30,19:00

    // ============ 生活状态 ============
    private Integer workPressure;          // 工作压力 1-5
    private Integer waterIntake;            // 每日饮水量目标(ml)
    private Integer standingHours;          // 每日久坐时长(小时)

    // ============ 目标详情 ============
    private Double targetFat;               // 目标体脂率
    private String targetAreas;             // 重点塑形部位如 腰,腹,臀

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
