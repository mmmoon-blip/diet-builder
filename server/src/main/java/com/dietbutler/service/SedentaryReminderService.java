package com.dietbutler.service;

import com.dietbutler.entity.SedentaryRecord;
import com.dietbutler.entity.User;
import com.dietbutler.repository.SedentaryRecordRepository;
import com.dietbutler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 久坐提醒服务
 * 每小时检测用户是否久坐过久，发送提醒
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SedentaryReminderService {

    private final UserRepository userRepository;
    private final SedentaryRecordRepository sedentaryRecordRepository;
    private final WechatTemplateMessageService wechatTemplateMessageService;

    /**
     * 久坐提醒定时检查 - 每小时执行
     * 检查用户是否需要站立活动
     */
    public void checkAndRemindSedentary() {
        log.info("开始久坐提醒检查");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            try {
                // 检查是否在工作时间（9-18点）
                LocalTime now = LocalTime.now();
                if (now.getHour() < 9 || now.getHour() >= 18) continue;

                // 检查久坐记录
                SedentaryRecord record = getOrCreateTodayRecord(user);
                if (record.getTotalSedentaryMinutes() != null &&
                    record.getTotalSedentaryMinutes() >= record.getGoalMinutes()) {
                    // 久坐超时，发送提醒
                    sendSedentaryReminder(user, record);
                }
            } catch (Exception e) {
                log.error("久坐提醒处理失败: {}", user.getOpenid(), e);
            }
        }
    }

    /**
     * 记录站立活动
     */
    public void recordStandUp(Long userId) {
        LocalDate today = LocalDate.now();
        SedentaryRecord record = sedentaryRecordRepository.findByUserIdAndRecordDate(userId, today)
                .orElseGet(() -> createDefaultRecord(userId, today));

        record.setStandCount(record.getStandCount() == null ? 1 : record.getStandCount() + 1);

        // 每次站立减少30分钟久坐时间
        if (record.getTotalSedentaryMinutes() != null) {
            record.setTotalSedentaryMinutes(Math.max(0, record.getTotalSedentaryMinutes() - 30));
        }

        sedentaryRecordRepository.save(record);
        log.info("记录站立活动: userId={}, standCount={}", userId, record.getStandCount());
    }

    /**
     * 累加久坐时间
     */
    public void addSedentaryMinutes(Long userId, int minutes) {
        LocalDate today = LocalDate.now();
        SedentaryRecord record = sedentaryRecordRepository.findByUserIdAndRecordDate(userId, today)
                .orElseGet(() -> createDefaultRecord(userId, today));

        record.setTotalSedentaryMinutes(
                (record.getTotalSedentaryMinutes() == null ? 0 : record.getTotalSedentaryMinutes()) + minutes);

        sedentaryRecordRepository.save(record);
    }

    private SedentaryRecord getOrCreateTodayRecord(User user) {
        LocalDate today = LocalDate.now();
        return sedentaryRecordRepository.findByUserIdAndRecordDate(user.getId(), today)
                .orElseGet(() -> createDefaultRecord(user.getId(), today));
    }

    private SedentaryRecord createDefaultRecord(Long userId, LocalDate date) {
        SedentaryRecord record = new SedentaryRecord();
        record.setUserId(userId);
        record.setRecordDate(date);
        record.setStandCount(0);
        record.setTotalSedentaryMinutes(0);
        record.setGoalMinutes(60); // 默认每60分钟需要站起来
        return record;
    }

    private void sendSedentaryReminder(User user, SedentaryRecord record) {
        String message = String.format(
            "🧘 站立提醒！\n\n您已经连续久坐%d分钟了，站起来活动一下吧！\n\n💡 建议：\n• 站起来走动2-3分钟\n• 伸个懒腰，拉伸一下\n• 喝杯水\n\n保持活力，工作效率更高哦～",
            record.getTotalSedentaryMinutes()
        );

        // 通过模板消息或聊天推送
        wechatTemplateMessageService.sendSedentaryReminder(
            user.getOpenid(),
            record.getStandCount() != null ? record.getStandCount() : 0,
            record.getTotalSedentaryMinutes() != null ? record.getTotalSedentaryMinutes() : 0
        );
    }
}