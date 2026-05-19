package com.dietbutler.service;

import com.dietbutler.entity.User;
import com.dietbutler.entity.WeightRecord;
import com.dietbutler.repository.UserRepository;
import com.dietbutler.repository.WeightRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务服务 - AI主动陪伴与督促
 * 实现晨间问候（含每日方案）、饮水提醒、运动提醒等主动推送功能
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class TaskScheduleService {

    private final UserRepository userRepository;
    private final WeightRecordRepository weightRecordRepository;
    private final WechatTemplateMessageService wechatTemplateMessageService;
    private final HealthReportService healthReportService;
    private final DailyPlanService dailyPlanService;
    private final SedentaryReminderService sedentaryReminderService;
    private final EmotionSupportService emotionSupportService;

    /**
     * 晨间问候 - 每天8:00发送
     * 包含今日体重记录提醒 + 今日建议
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningGreetings() {
        log.info("开始发送晨间问候");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            // 获取今日体重
            WeightRecord todayRecord = weightRecordRepository.findByUserIdAndRecordDate(
                    user.getId(), java.time.LocalDate.now()).orElse(null);

            // 计算累计变化
            Double changeFromStart = null;
            if (todayRecord != null && user.getInitialWeight() != null) {
                changeFromStart = todayRecord.getWeight() - user.getInitialWeight();
            }

            boolean success = wechatTemplateMessageService.sendMorningGreeting(
                    user.getOpenid(),
                    user.getNickname(),
                    todayRecord != null ? todayRecord.getWeight() : null,
                    changeFromStart
            );

            if (success) {
                log.info("晨间问候发送成功: {}", user.getOpenid());
            }
        }
    }

    /**
     * 饮水提醒 - 每天10:00、14:00、18:00发送
     */
    @Scheduled(cron = "0 0 10,14,18 * * *")
    public void sendWaterReminders() {
        log.info("开始发送饮水提醒");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            int goal = user.getWaterIntake() != null ? user.getWaterIntake() / 250 : 8; // 默认8杯
            // 简化版：每次提醒喝1-2杯
            int reminderTimes = (goal + 2) / 3; // 一天三次提醒

            wechatTemplateMessageService.sendWaterReminder(
                    user.getOpenid(),
                    1, // 简化：每次提醒1杯
                    reminderTimes
            );
        }
    }

    /**
     * 晚间复盘 - 每天21:00发送
     * 包含今日体重回顾 + 明日建议
     */
    @Scheduled(cron = "0 0 21 * * *")
    public void sendEveningSummaries() {
        log.info("开始发送晚间复盘");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            // 获取今日体重
            WeightRecord todayRecord = weightRecordRepository.findByUserIdAndRecordDate(
                    user.getId(), java.time.LocalDate.now()).orElse(null);

            String summary = buildEveningSummary(user, todayRecord);

            wechatTemplateMessageService.sendEveningSummary(
                    user.getOpenid(),
                    todayRecord != null ? todayRecord.getWeight() : null,
                    summary
            );
        }
    }

    /**
     * 每周一体重报告 - 每周一9:00发送
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyReport() {
        log.info("开始发送周报");
        healthReportService.generateAndSendWeeklyReports();
    }

    /**
     * 久坐提醒 - 每小时执行（工作时间9-18点）
     */
    @Scheduled(cron = "0 0 9-17 * * *")
    public void checkSedentaryAndRemind() {
        log.info("开始久坐提醒检查");
        sedentaryReminderService.checkAndRemindSedentary();
    }

    /**
     * 情绪干预检查 - 每30分钟执行
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void checkEmotionAndIntervene() {
        log.info("开始情绪干预检查");
        emotionSupportService.checkAndInterveneNegativeEmotions();
    }

    /**
     * 每月1号发送月报
     */
    @Scheduled(cron = "0 0 10 1 * *")
    public void sendMonthlyReport() {
        log.info("开始发送月报");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            try {
                String report = healthReportService.generateMonthlyReport(user);
                if (report != null) {
                    wechatTemplateMessageService.sendMonthlyReport(user.getOpenid(), report);
                }
            } catch (Exception e) {
                log.error("月报发送失败: {}", user.getOpenid(), e);
            }
        }
    }

    private String buildEveningSummary(User user, WeightRecord todayRecord) {
        StringBuilder sb = new StringBuilder();

        if (todayRecord != null) {
            sb.append("今日体重已记录，");
            if (user.getTargetWeight() != null) {
                double toGoal = todayRecord.getWeight() - user.getTargetWeight();
                if (toGoal > 0) {
                    sb.append("距离目标还剩").append(String.format("%.1f", toGoal)).append("kg，");
                } else {
                    sb.append("已达成目标！🎉");
                }
            }
        } else {
            sb.append("今日体重还未记录，明天记得称重哦～");
        }

        sb.append("早点休息，明天继续加油！");
        return sb.toString();
    }
}
