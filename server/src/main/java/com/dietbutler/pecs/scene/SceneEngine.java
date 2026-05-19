package com.dietbutler.pecs.scene;

import com.dietbutler.pecs.PersonContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
public class SceneEngine {

    public SceneResult recognize(PersonContext person, LocalTime currentTime, String userMessage) {
        SceneResult result = new SceneResult();

        // 第一层: 基于时间识别场景
        recognizeTimeBasedScenes(person, currentTime, result);

        // 第二层: 基于用户档案识别场景
        recognizeProfileBasedScenes(person, result);

        // 第三层: 基于消息内容识别场景
        recognizeMessageBasedScenes(userMessage, result);

        return result;
    }

    private void recognizeTimeBasedScenes(PersonContext person, LocalTime currentTime, SceneResult result) {
        // 早晨空腹场景 (6:00-9:00 且未吃早餐)
        if (currentTime.isAfter(LocalTime.of(6, 0)) && currentTime.isBefore(LocalTime.of(9, 0))) {
            if (person.getBreakfastHabit() == null || !person.getBreakfastHabit()) {
                result.addScene(SceneType.MORNING_FASTING);
            }
        }

        // 熬夜场景 (当前时间晚于23:00)
        if (currentTime.isAfter(LocalTime.of(23, 0))) {
            result.addScene(SceneType.LATE_NIGHT);
        }

        // 检查用户作息中的熬夜迹象
        String sleepEnd = person.getSleepEnd();
        if (sleepEnd != null) {
            try {
                int endHour = parseHour(sleepEnd);
                if (endHour >= 23 || endHour <= 5) {
                    result.addScene(SceneType.LATE_NIGHT);
                }
            } catch (Exception e) {
                // ignore parse error
            }
        }
    }

    private void recognizeProfileBasedScenes(PersonContext person, SceneResult result) {
        // 工作压力大
        if (person.getWorkPressure() != null && person.getWorkPressure() >= 4) {
            result.addScene(SceneType.WORK_OVERTIME);
        }

        // 久坐
        if (person.getStandingHours() != null && person.getStandingHours() >= 6) {
            result.addScene(SceneType.SEDENTARY);
        }

        // 经期检测
        if (person.getMenstrualCycleLength() != null && person.getLastMenstrualDate() != null) {
            LocalDate today = LocalDate.now();
            PersonContext.LocalDate lastPeriod = person.getLastMenstrualDate();

            try {
                LocalDate lastPeriodDate = LocalDate.of(lastPeriod.getYear(), lastPeriod.getMonth(), lastPeriod.getDay());
                int daysSinceLastPeriod = (int) (today.toEpochDay() - lastPeriodDate.toEpochDay());
                int cycleLength = person.getMenstrualCycleLength();

                // 经期第1-3天
                if (daysSinceLastPeriod >= 0 && daysSinceLastPeriod <= 3) {
                    result.addScene(SceneType.MENSTRUAL);
                }
            } catch (Exception e) {
                // ignore date parse error
            }
        }

        // 熬夜体质标签
        if (person.getConstitutionTags() != null && person.getConstitutionTags().contains("熬夜体质")) {
            result.addScene(SceneType.LATE_NIGHT);
        }

        // 压力胖体质标签
        if (person.getConstitutionTags() != null && person.getConstitutionTags().contains("压力胖")) {
            result.addScene(SceneType.STRESS_EATING);
        }
    }

    private void recognizeMessageBasedScenes(String message, SceneResult result) {
        if (message == null || message.isEmpty()) {
            return;
        }

        String lowerMessage = message.toLowerCase();

        // 经期相关关键词
        if (containsAny(lowerMessage, "肚子疼", "来例假", "经期", "月经", "大姨妈", "痛经")) {
            result.addScene(SceneType.MENSTRUAL);
        }

        // 熬夜相关关键词
        if (containsAny(lowerMessage, "熬夜", "失眠", "没睡觉", "昨晚", "通宵")) {
            result.addScene(SceneType.LATE_NIGHT);
        }

        // 加班相关关键词
        if (containsAny(lowerMessage, "加班", "工作忙", "开会", "赶项目", "上班累")) {
            result.addScene(SceneType.WORK_OVERTIME);
        }

        // 情绪化进食相关关键词
        if (containsAny(lowerMessage, "暴食", "控制不住", "想吃", "馋", "压力大", "心情不好", "焦虑", "沮丧")) {
            result.addScene(SceneType.STRESS_EATING);
        }

        // 情绪低落相关关键词
        if (containsAny(lowerMessage, "心情不好", "难受", "难过", "沮丧", "不开心", "郁闷")) {
            result.addScene(SceneType.FEELING_DOWN);
        }

        // 久坐相关关键词
        if (containsAny(lowerMessage, "久坐", "一直坐着", "没动")) {
            result.addScene(SceneType.SEDENTARY);
        }

        // 运动后相关关键词
        if (containsAny(lowerMessage, "刚运动完", "跑完步", "健身后", "运动完", "练完")) {
            result.addScene(SceneType.POST_EXERCISE);
        }

        // 聚餐相关关键词
        if (containsAny(lowerMessage, "聚餐", "聚会", "约会", "吃饭", "宴席")) {
            result.addScene(SceneType.FEAST_DAY);
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int parseHour(String timeStr) {
        try {
            if (timeStr.contains(":")) {
                return Integer.parseInt(timeStr.split(":")[0]);
            }
            return Integer.parseInt(timeStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
