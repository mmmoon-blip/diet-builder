package com.dietbutler.pecs.scene;

import com.dietbutler.pecs.PersonContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class SceneEngine {

    public SceneResult recognize(PersonContext person, LocalTime currentTime, String userMessage) {
        SceneResult result = new SceneResult();
        recognizeTimeBasedScenes(person, currentTime, result);
        recognizeProfileBasedScenes(person, result);
        recognizeMessageBasedScenes(userMessage, result);
        return result;
    }

    private void recognizeTimeBasedScenes(PersonContext person, LocalTime currentTime, SceneResult result) {
        if (currentTime.isAfter(LocalTime.of(6, 0)) && currentTime.isBefore(LocalTime.of(9, 0))) {
            if (person.getBreakfastHabit() == null || !person.getBreakfastHabit()) {
                result.addScene(SceneType.MORNING_FASTING);
            }
        }
        if (currentTime.isAfter(LocalTime.of(23, 0))) {
            result.addScene(SceneType.LATE_NIGHT);
        }
        String sleepEnd = person.getSleepEnd();
        if (sleepEnd != null) {
            try {
                int endHour = parseHour(sleepEnd);
                if (endHour >= 23 || endHour <= 5) {
                    result.addScene(SceneType.LATE_NIGHT);
                }
            } catch (Exception e) { /* ignore */ }
        }
    }

    private void recognizeProfileBasedScenes(PersonContext person, SceneResult result) {
        if (person.getWorkPressure() != null && person.getWorkPressure() >= 4) {
            result.addScene(SceneType.WORK_OVERTIME);
        }
        if (person.getStandingHours() != null && person.getStandingHours() >= 6) {
            result.addScene(SceneType.SEDENTARY);
        }
        if (person.getMenstrualCycleLength() != null && person.getLastMenstrualDate() != null) {
            LocalDate today = LocalDate.now();
            PersonContext.LocalDate lastPeriod = person.getLastMenstrualDate();
            try {
                LocalDate lastPeriodDate = LocalDate.of(lastPeriod.getYear(), lastPeriod.getMonth(), lastPeriod.getDay());
                int daysSinceLastPeriod = (int) (today.toEpochDay() - lastPeriodDate.toEpochDay());
                if (daysSinceLastPeriod >= 0 && daysSinceLastPeriod <= 3) {
                    result.addScene(SceneType.MENSTRUAL);
                }
            } catch (Exception e) { /* ignore */ }
        }
        if (person.getConstitutionTags() != null) {
            if (person.getConstitutionTags().contains("熬夜体质")) result.addScene(SceneType.LATE_NIGHT);
            if (person.getConstitutionTags().contains("压力胖")) result.addScene(SceneType.STRESS_EATING);
        }
    }

    private void recognizeMessageBasedScenes(String message, SceneResult result) {
        if (message == null || message.isEmpty()) return;
        String lowerMessage = message.toLowerCase();

        if (containsAny(lowerMessage, "肚子疼", "来例假", "经期", "月经", "大姨妈", "痛经")) {
            result.addScene(SceneType.MENSTRUAL);
        }
        if (containsAny(lowerMessage, "熬夜", "失眠", "没睡觉", "昨晚", "通宵")) {
            result.addScene(SceneType.LATE_NIGHT);
        }
        if (containsAny(lowerMessage, "加班", "工作忙", "开会", "赶项目", "上班累")) {
            result.addScene(SceneType.WORK_OVERTIME);
        }
        if (containsAny(lowerMessage, "暴食", "控制不住", "想吃", "馋", "压力大", "心情不好", "焦虑", "沮丧")) {
            result.addScene(SceneType.STRESS_EATING);
        }
        if (containsAny(lowerMessage, "心情不好", "难受", "难过", "沮丧", "不开心", "郁闷")) {
            result.addScene(SceneType.FEELING_DOWN);
        }
        if (containsAny(lowerMessage, "久坐", "一直坐着", "没动")) {
            result.addScene(SceneType.SEDENTARY);
        }
        if (containsAny(lowerMessage, "刚运动完", "跑完步", "健身后", "运动完", "练完")) {
            result.addScene(SceneType.POST_EXERCISE);
        }
        if (containsAny(lowerMessage, "聚餐", "聚会", "约会", "吃饭", "宴席")) {
            result.addScene(SceneType.FEAST_DAY);
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
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
