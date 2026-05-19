package com.dietbutler.service;

import com.dietbutler.entity.User;
import com.dietbutler.entity.WeightRecord;
import com.dietbutler.entity.BodyMeasurement;
import com.dietbutler.repository.UserRepository;
import com.dietbutler.repository.WeightRecordRepository;
import com.dietbutler.repository.BodyMeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI健康报告服务
 * 生成每周/每月的健康分析报告
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportService {

    private final UserRepository userRepository;
    private final WeightRecordRepository weightRecordRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;
    private final WechatTemplateMessageService wechatTemplateMessageService;

    /**
     * 生成周报（供定时任务调用）
     */
    public void generateAndSendWeeklyReports() {
        log.info("开始生成周报");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            if (user.getOpenid() == null || user.getOpenid().isEmpty()) continue;
            if (!Boolean.TRUE.equals(user.getReminderEnabled())) continue;

            try {
                String report = generateWeeklyReport(user);
                if (report != null) {
                    wechatTemplateMessageService.sendWeeklyReport(user.getOpenid(), report);
                    log.info("周报发送成功: {}", user.getOpenid());
                }
            } catch (Exception e) {
                log.error("生成周报失败: {}", user.getOpenid(), e);
            }
        }
    }

    /**
     * 为单个用户生成周报
     */
    public String generateWeeklyReport(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate twoWeeksAgo = today.minusDays(14);

        // 获取本周体重数据
        List<WeightRecord> thisWeekRecords = weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                user.getId(), weekAgo, today);

        // 获取上周体重数据
        List<WeightRecord> lastWeekRecords = weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                user.getId(), twoWeeksAgo, weekAgo);

        if (thisWeekRecords.isEmpty() && lastWeekRecords.isEmpty()) {
            return null; // 没有数据不发送
        }

        StringBuilder report = new StringBuilder();
        report.append("📊 【上周健康周报】\n\n");

        // 1. 体重变化分析
        report.append("📈 体重变化\n");
        if (!thisWeekRecords.isEmpty()) {
            double latestWeight = thisWeekRecords.get(thisWeekRecords.size() - 1).getWeight();
            double earliestWeight = thisWeekRecords.get(0).getWeight();
            double weekChange = latestWeight - earliestWeight;

            report.append("• 本周体重：").append(String.format("%.1f", latestWeight)).append("kg\n");
            report.append("• 本周变化：").append(formatChange(weekChange)).append("\n");

            if (user.getInitialWeight() != null) {
                double totalChange = latestWeight - user.getInitialWeight();
                report.append("• 累计变化：").append(formatChange(totalChange)).append("\n");
            }
        } else {
            report.append("• 本周暂无体重记录\n");
        }

        // 2. 记录情况评估
        report.append("\n📝 记录情况\n");
        report.append("• 记录天数：").append(thisWeekRecords.size()).append("/7天\n");

        if (thisWeekRecords.size() >= 5) {
            report.append("• 表现：⭐⭐⭐⭐⭐ 很棒！坚持记录\n");
        } else if (thisWeekRecords.size() >= 3) {
            report.append("• 表现：⭐⭐⭐ 不错，还可以更好\n");
        } else {
            report.append("• 表现：⭐ 加油！坚持每天记录\n");
        }

        // 3. 与上周对比
        if (!thisWeekRecords.isEmpty() && !lastWeekRecords.isEmpty()) {
            double thisWeekAvg = thisWeekRecords.stream()
                    .mapToDouble(WeightRecord::getWeight)
                    .average().orElse(0);

            double lastWeekAvg = lastWeekRecords.stream()
                    .mapToDouble(WeightRecord::getWeight)
                    .average().orElse(0);

            double avgDiff = thisWeekAvg - lastWeekAvg;
            if (Math.abs(avgDiff) < 0.1) {
                report.append("\n⚖️ 体重稳定，与上周基本持平\n");
            } else if (avgDiff < 0) {
                report.append("\n⚖️ 体重下降，比上周平均降低").append(String.format("%.1f", Math.abs(avgDiff))).append("kg 👍\n");
            } else {
                report.append("\n⚖️ 体重上升，比上周平均增加").append(String.format("%.1f", avgDiff)).append("kg\n");
            }
        }

        // 4. AI建议
        report.append("\n💡 AI建议\n");
        report.append(generateSuggestions(user, thisWeekRecords));

        report.append("\n🌟 感谢使用减减，我们下周见！");

        return report.toString();
    }

    /**
     * 生成AI建议
     */
    private String generateSuggestions(User user, List<WeightRecord> thisWeekRecords) {
        List<String> suggestions = new ArrayList<>();

        // 根据用户类型生成建议
        String userType = user.getUserType() != null ? user.getUserType() : "weight_loss";

        switch (userType) {
            case "weight_loss":
                suggestions.add("继续控制饮食热量，保持适度运动");
                if (user.getConstitutionTags() != null) {
                    if (user.getConstitutionTags().contains("熬夜体质")) {
                        suggestions.add("注意早睡，熬夜会影响代谢");
                    }
                    if (user.getConstitutionTags().contains("易水肿")) {
                        suggestions.add("注意少盐，多吃利尿食物如冬瓜");
                    }
                }
                break;
            case "shaping":
                suggestions.add("保持力量训练，重点关注腰腹和臀部");
                suggestions.add("高蛋白饮食配合训练效果更好");
                break;
            case "maintenance":
                suggestions.add("维持期关键是不要放松，继续保持健康习惯");
                suggestions.add("允许偶尔放纵，但要有节制");
                break;
            case "muscle_gain":
                suggestions.add("增肌期要保证热量盈余和充足蛋白质");
                suggestions.add("训练后及时补充营养");
                break;
        }

        // 通用建议
        if (thisWeekRecords.size() < 7) {
            suggestions.add("建议每天固定时间称重，更准确反映变化");
        }

        if (user.getWaterIntake() != null && user.getWaterIntake() < 2000) {
            suggestions.add("每天饮水要达标，代谢更顺畅");
        }

        if (user.getExerciseFrequency() == null || user.getExerciseFrequency() < 3) {
            suggestions.add("适当增加运动频率，每周3-5次为宜");
        }

        // 限制建议数量
        if (suggestions.size() > 3) {
            suggestions = suggestions.subList(0, 3);
        }

        return suggestions.stream()
                .map(s -> "• " + s)
                .collect(Collectors.joining("\n"));
    }

    private String formatChange(Double change) {
        if (change == null || Math.abs(change) < 0.01) return "无变化";
        return (change >= 0 ? "+" : "") + String.format("%.1f", change) + "kg";
    }

    /**
     * 生成月度报告
     */
    public String generateMonthlyReport(User user) {
        LocalDate today = LocalDate.now();
        LocalDate monthAgo = today.minusDays(30);

        List<WeightRecord> monthRecords = weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                user.getId(), monthAgo, today);

        if (monthRecords.isEmpty()) {
            return null;
        }

        StringBuilder report = new StringBuilder();
        report.append("📊 【本月健康月报】\n\n");

        // 体重统计
        double minWeight = monthRecords.stream().mapToDouble(WeightRecord::getWeight).min().orElse(0);
        double maxWeight = monthRecords.stream().mapToDouble(WeightRecord::getWeight).max().orElse(0);
        double avgWeight = monthRecords.stream().mapToDouble(WeightRecord::getWeight).average().orElse(0);

        report.append("📈 体重统计\n");
        report.append("• 月初体重：").append(String.format("%.1f", monthRecords.get(0).getWeight())).append("kg\n");
        report.append("• 月末体重：").append(String.format("%.1f", monthRecords.get(monthRecords.size() - 1).getWeight())).append("kg\n");
        report.append("• 月最高：").append(String.format("%.1f", maxWeight)).append("kg\n");
        report.append("• 月最低：").append(String.format("%.1f", minWeight)).append("kg\n");
        report.append("• 月均体重：").append(String.format("%.1f", avgWeight)).append("kg\n");

        double monthChange = monthRecords.get(monthRecords.size() - 1).getWeight() - monthRecords.get(0).getWeight();
        report.append("• 月变化：").append(formatChange(monthChange)).append("\n");

        // 记录情况
        report.append("\n📝 记录情况\n");
        report.append("• 记录天数：").append(monthRecords.size()).append("/30天\n");
        report.append("• 完成率：").append(String.format("%.0f", (monthRecords.size() / 30.0) * 100)).append("%\n");

        // 目标进度
        if (user.getTargetWeight() != null && user.getInitialWeight() != null) {
            double totalToLose = user.getInitialWeight() - user.getTargetWeight();
            double alreadyLost = user.getInitialWeight() - monthRecords.get(monthRecords.size() - 1).getWeight();
            double progress = totalToLose > 0 ? (alreadyLost / totalToLose) * 100 : 0;
            report.append("\n🎯 目标进度\n");
            report.append("• 目标：").append(user.getTargetWeight()).append("kg\n");
            report.append("• 已完成：").append(String.format("%.0f", Math.min(100, progress))).append("%\n");
        }

        report.append("\n🌟 下月继续加油！");

        return report.toString();
    }
}