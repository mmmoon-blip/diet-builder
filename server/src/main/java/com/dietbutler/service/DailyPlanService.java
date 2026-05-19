package com.dietbutler.service;

import com.dietbutler.config.LlmProperties;
import com.dietbutler.dto.LlmRequest;
import com.dietbutler.dto.LlmResponse;
import com.dietbutler.entity.User;
import com.dietbutler.entity.WeightRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI个性化方案生成服务
 * 根据用户档案和专业知识库，生成每日饮食和运动方案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPlanService {

    private final WebClient webClient;
    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final WeightRecordService weightRecordService;

    /**
     * 生成每日健康方案
     */
    public String generateDailyPlan(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return "无法生成方案：未找到用户信息";
        }

        // 获取最新体重
        Optional<WeightRecord> todayRecord = weightRecordService.getToday(userId);
        double currentWeight = todayRecord.map(WeightRecord::getWeight)
                .orElseGet(() -> user.getInitialWeight() != null ? user.getInitialWeight() : 0);

        // 获取用户类型
        String userType = user.getUserType() != null ? user.getUserType() : "weight_loss";
        String userTypeName = getUserTypeName(userType);

        // 根据用户类型构建方案
        String plan = buildDailyPlan(user, currentWeight, userType);

        return plan;
    }

    /**
     * 根据用户类型构建方案
     */
    private String buildDailyPlan(User user, double currentWeight, String userType) {
        StringBuilder plan = new StringBuilder();
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        plan.append("🌅 早安！").append(user.getNickname()).append("\n");
        plan.append("📅 ").append(today.format(DateTimeFormatter.ofPattern("MM月dd日"))).append(" ");
        plan.append(getWeekdayName(dayOfWeek)).append("\n\n");

        // 今日概况
        plan.append("📊 今日概况\n");
        plan.append("• 当前体重：").append(String.format("%.1f", currentWeight)).append("kg\n");
        if (user.getTargetWeight() != null) {
            double toGoal = currentWeight - user.getTargetWeight();
            plan.append("• 距离目标：").append(toGoal > 0 ? String.format("%.1f", toGoal) + "kg" : "已达成！🎉").append("\n");
        }
        plan.append("• 用户类型：").append(getUserTypeName(userType)).append("\n\n");

        // 饮食方案
        plan.append("🍽️ 今日饮食建议\n");
        plan.append(buildDietPlan(user, userType, currentWeight));

        // 运动方案
        plan.append("\n🏃 今日运动建议\n");
        plan.append(buildExercisePlan(user, userType, dayOfWeek));

        // 今日提醒
        plan.append("\n💡 今日小贴士\n");
        plan.append(getDailyTip(user, userType));

        return plan.toString();
    }

    /**
     * 构建饮食方案
     */
    private String buildDietPlan(User user, String userType, double currentWeight) {
        StringBuilder diet = new StringBuilder();

        // 计算基础代谢
        double bmr = calculateBMR(user);
        // 活动系数
        double activityFactor = getActivityFactor(user);
        // 每日总消耗
        double tdee = bmr * activityFactor;

        // 根据用户类型设置目标
        double targetCalorie;
        double proteinRatio;
        double carbRatio;
        double fatRatio;

        switch (userType) {
            case "weight_loss":
                targetCalorie = tdee * 0.8; // 80%热量
                proteinRatio = 0.35;
                carbRatio = 0.40;
                fatRatio = 0.25;
                break;
            case "shaping":
                targetCalorie = tdee; // 维持热量
                proteinRatio = 0.40;
                carbRatio = 0.35;
                fatRatio = 0.25;
                break;
            case "maintenance":
                targetCalorie = tdee * 0.95;
                proteinRatio = 0.30;
                carbRatio = 0.45;
                fatRatio = 0.25;
                break;
            case "muscle_gain":
                targetCalorie = tdee * 1.15; // 115%热量
                proteinRatio = 0.35;
                carbRatio = 0.45;
                fatRatio = 0.20;
                break;
            default:
                targetCalorie = tdee * 0.8;
                proteinRatio = 0.35;
                carbRatio = 0.40;
                fatRatio = 0.25;
        }

        // 计算克数
        double protein = (targetCalorie * proteinRatio) / 4; // 蛋白质4kcal/g
        double carb = (targetCalorie * carbRatio) / 4;
        double fat = (targetCalorie * fatRatio) / 9;

        diet.append("• 目标热量：").append((int) targetCalorie).append("kcal\n");
        diet.append("• 蛋白质：").append((int) protein).append("g\n");
        diet.append("• 碳水：").append((int) carb).append("g\n");
        diet.append("• 脂肪：").append((int) fat).append("g\n");

        // 根据体质标签给出饮食建议
        diet.append("\n饮食注意：\n");
        if (user.getConstitutionTags() != null) {
            if (user.getConstitutionTags().contains("易水肿")) {
                diet.append("• 少盐饮食，少吃腌制品\n");
            }
            if (user.getConstitutionTags().contains("碳水敏感")) {
                diet.append("• 碳水放在训练前后吃\n");
            }
            if (user.getConstitutionTags().contains("代谢低")) {
                diet.append("• 少食多餐，提高代谢\n");
            }
        }
        if (user.getDietaryTaboo() != null) {
            diet.append("• 忌口：").append(user.getDietaryTaboo()).append("\n");
        }

        return diet.toString();
    }

    /**
     * 构建运动方案
     */
    private String buildExercisePlan(User user, String userType, DayOfWeek dayOfWeek) {
        StringBuilder exercise = new StringBuilder();

        // 获取运动偏好和频率
        String preference = user.getExercisePreference() != null ? user.getExercisePreference() : "居家";
        Integer frequency = user.getExerciseFrequency() != null ? user.getExerciseFrequency() : 3;
        Boolean hasKneeIssue = user.getHasKneeIssue();

        // 根据星期安排训练
        boolean isTrainingDay = isTrainingDay(dayOfWeek, frequency);

        if (!isTrainingDay) {
            exercise.append("今日休息日，可以轻度活动：\n");
            exercise.append("• 散步20-30分钟\n");
            exercise.append("• 拉伸放松10分钟\n");
            exercise.append("• 多喝水，帮助恢复\n");
        } else {
            switch (userType) {
                case "weight_loss":
                    exercise.append(buildWeightLossExercise(user, preference, hasKneeIssue));
                    break;
                case "shaping":
                    exercise.append(buildShapingExercise(user, preference, hasKneeIssue));
                    break;
                case "muscle_gain":
                    exercise.append(buildMuscleGainExercise(user, preference));
                    break;
                default:
                    exercise.append("• 快走或慢跑30分钟\n");
                    exercise.append("• 力量训练30分钟\n");
            }
        }

        return exercise.toString();
    }

    private String buildWeightLossExercise(User user, String preference, Boolean hasKneeIssue) {
        StringBuilder ex = new StringBuilder();
        ex.append("今日训练：减脂为主\n\n");

        ex.append("热身（5分钟）：\n");
        ex.append("• 高抬腿30秒\n");
        ex.append("• 原地开合跳30秒\n");
        ex.append("• 动态拉伸2分钟\n\n");

        ex.append("正式训练（约40分钟）：\n");
        if ("居家".equals(preference)) {
            ex.append("• 登山者3组x30秒\n");
            ex.append("• 深蹲3组x15次\n");
            if (!Boolean.TRUE.equals(hasKneeIssue)) {
                ex.append("• 蛙跳2组x10次\n");
            }
            ex.append("• 平板支撑3组x60秒\n");
            ex.append("• 波比跳2组x8次\n\n");
        } else {
            ex.append("• 慢跑/快走30分钟\n");
            ex.append("• 哑铃罗马尼亚硬拉3组x12次\n");
            ex.append("• 哑铃深蹲3组x15次\n");
            ex.append("• 俯卧撑3组x10次\n\n");
        }

        ex.append("有氧（20分钟）：\n");
        ex.append("• 跳绳或快走\n\n");

        ex.append("放松（5分钟）：\n");
        ex.append("• 全身拉伸\n");

        return ex.toString();
    }

    private String buildShapingExercise(User user, String preference, Boolean hasKneeIssue) {
        StringBuilder ex = new StringBuilder();
        ex.append("今日训练：塑形为主\n\n");

        // 根据目标部位安排
        String targetAreas = user.getTargetAreas();
        if (targetAreas == null) {
            targetAreas = "腰,腹,臀";
        }

        ex.append("热身（5分钟）：\n");
        ex.append("• 肩部绕环\n");
        ex.append("• 髋关节绕环\n");
        ex.append("• 脊柱扭转\n\n");

        ex.append("正式训练（45分钟）：\n");
        if (targetAreas.contains("腰") || targetAreas.contains("腹")) {
            ex.append("【核心】\n");
            ex.append("• 卷腹3组x15次\n");
            ex.append("• 平板支撑3组x60秒\n");
            ex.append("• 俄罗斯转体3组x20次\n\n");
        }
        if (targetAreas.contains("臀")) {
            ex.append("【臀部】\n");
            ex.append("• 臀桥3组x20次\n");
            ex.append("• 侧卧抬腿3组x15次/侧\n");
            if (!Boolean.TRUE.equals(hasKneeIssue)) {
                ex.append("• 深蹲跳2组x10次\n");
            }
        }
        if (targetAreas.contains("腿")) {
            ex.append("【腿部】\n");
            ex.append("• 靠墙静蹲3组x60秒\n");
            ex.append("• 哑铃弓箭步3组x12次/侧\n");
        }

        ex.append("放松（5分钟）：\n");
        ex.append("• 重点拉伸腹部、臀部、腿部\n");

        return ex.toString();
    }

    private String buildMuscleGainExercise(User user, String preference) {
        StringBuilder ex = new StringBuilder();
        ex.append("今日训练：增肌为主\n\n");

        ex.append("热身（5分钟）：\n");
        ex.append("• 轻重量的目标肌群热身\n\n");

        ex.append("正式训练（50分钟）：\n");
        ex.append("【复合动作】\n");
        if ("健身房".equals(preference)) {
            ex.append("• 杠铃深蹲4组x8-10次\n");
            ex.append("• 硬拉4组x8次\n");
            ex.append("• 卧推4组x10次\n");
            ex.append("• 引体向上3组x8-10次\n");
        } else {
            ex.append("• 深蹲4组x12次\n");
            ex.append("• 俯卧撑4组x12次\n");
            ex.append("• 哑铃划船4组x10次\n");
            ex.append("• 肩推3组x12次\n");
        }

        ex.append("\n【辅助动作】\n");
        ex.append("• 二头弯举3组x12次\n");
        ex.append("• 三头下压3组x12次\n\n");

        ex.append("训练后补充：\n");
        ex.append("• 蛋白质+碳水（增肌粉或鸡蛋+米饭）\n");

        return ex.toString();
    }

    /**
     * 获取每日小贴士
     */
    private String getDailyTip(User user, String userType) {
        List<String> tips = new ArrayList<>();

        // 通用建议
        String waterTip = "记得多喝水，每天" + (user.getWaterIntake() != null ? user.getWaterIntake() : 2000) + "ml";
        tips.add(waterTip);
        String sleepTip = "保持充足睡眠，晚上" + (user.getSleepStart() != null ? user.getSleepStart().toString() : "22:00") + "前入睡";
        tips.add(sleepTip);

        // 体质相关
        if (user.getConstitutionTags() != null) {
            if (user.getConstitutionTags().contains("熬夜体质")) {
                tips.add("今晚尽量早点休息，熬夜影响代谢");
            }
            if (user.getConstitutionTags().contains("易水肿")) {
                tips.add("下午或晚上可以喝点赤小豆薏仁水");
            }
            if (user.getConstitutionTags().contains("压力胖")) {
                tips.add("工作之余记得放松，可以做做深呼吸");
            }
        }

        // 类型相关
        switch (userType) {
            case "weight_loss":
                tips.add("不要过度节食，保证基础代谢");
                tips.add("称重建议：早起空腹，排空后");
                break;
            case "shaping":
                tips.add("线条塑造需要时间，坚持就是胜利");
                tips.add("注意训练后补充蛋白质");
                break;
            case "maintenance":
                tips.add("维持期重点是习惯，不是强度");
                tips.add("允许偶尔放纵，但要有节制");
                break;
        }

        // 随机选一条
        Collections.shuffle(tips);
        return tips.get(0);
    }

    /**
     * 计算基础代谢
     */
    private double calculateBMR(User user) {
        if (user.getBasicMetabolism() != null) {
            return user.getBasicMetabolism();
        }
        // BMR计算公式
        double weight = user.getInitialWeight() != null ? user.getInitialWeight() : 60;
        double height = user.getHeight() != null ? user.getHeight() : 165;
        int age = user.getAge() != null ? user.getAge() : 25;
        int gender = user.getGender() != null ? user.getGender() : 2;

        double bmr;
        if (gender == 1) {
            bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * height - 5 * age - 161;
        }
        return bmr;
    }

    /**
     * 获取活动系数
     */
    private double getActivityFactor(User user) {
        Integer freq = user.getExerciseFrequency();
        if (freq == null) freq = 0;

        Integer standingHours = user.getStandingHours();
        if (standingHours == null) standingHours = 8;

        // 基础系数
        double baseFactor = 1.2;

        // 根据运动频率调整
        if (freq >= 5) baseFactor = 1.55;
        else if (freq >= 3) baseFactor = 1.375;
        else if (freq >= 1) baseFactor = 1.3;

        // 根据久坐时间调整
        if (standingHours < 4) baseFactor += 0.1;
        else if (standingHours > 8) baseFactor -= 0.05;

        return baseFactor;
    }

    /**
     * 判断是否是训练日
     */
    private boolean isTrainingDay(DayOfWeek dayOfWeek, int frequency) {
        // 简单分配：均匀分布训练日
        int dayIndex = dayOfWeek.getValue(); // 1-7
        return frequency > 0 && (dayIndex % (8 - Math.min(frequency, 7))) == 0;
    }

    private String getUserTypeName(String userType) {
        return switch (userType) {
            case "weight_loss" -> "减重人群";
            case "shaping" -> "塑形人群";
            case "maintenance" -> "维持人群";
            case "muscle_gain" -> "增肌人群";
            default -> "减重人群";
        };
    }

    private String getWeekdayName(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "周一";
            case TUESDAY -> "周二";
            case WEDNESDAY -> "周三";
            case THURSDAY -> "周四";
            case FRIDAY -> "周五";
            case SATURDAY -> "周六";
            case SUNDAY -> "周日";
        };
    }

    /**
     * 发送给用户的每日方案（通过AI对话推送）
     */
    public void pushDailyPlanToUser(Long userId) {
        String plan = generateDailyPlan(userId);
        // 保存到数据库作为AI消息
        savePlanAsMessage(userId, plan);
    }

    private void savePlanAsMessage(Long userId, String content) {
        // 这里可以保存到聊天记录或者单独的方案记录表
        // 目前通过ChatService的消息机制处理
    }
}