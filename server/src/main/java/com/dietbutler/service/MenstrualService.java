package com.dietbutler.service;

import com.dietbutler.entity.MenstrualRecord;
import com.dietbutler.repository.MenstrualRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenstrualService {

    private final MenstrualRecordRepository menstrualRecordRepository;

    // 默认周期28天，可根据历史记录计算平均
    private static final int DEFAULT_CYCLE_LENGTH = 28;

    // 经期默认持续5天
    private static final int DEFAULT_PERIOD_DAYS = 5;

    // 卵泡期开始（经期结束后）
    private static final int FOLLICULAR_START = 6;

    // 排卵日（假设周期第14天，扣掉经期5天 + 卵泡期8天）
    private static final int OVULATION_DAY = 14;

    public MenstrualRecord addRecord(Long userId, LocalDate startDate, LocalDate endDate,
                                     String flowLevel, Boolean isInPeriod, Boolean hasPain, String otherInfo) {
        MenstrualRecord record = new MenstrualRecord();
        record.setUserId(userId);
        record.setCycleStartDate(startDate);
        record.setCycleEndDate(endDate);
        if (endDate != null && startDate != null) {
            record.setCycleLength((int) ChronoUnit.DAYS.between(startDate, endDate) + 1);
        }
        record.setFlowLevel(flowLevel);
        record.setIsInPeriod(isInPeriod != null ? isInPeriod : false);
        record.setHasPain(hasPain != null ? hasPain : false);
        record.setOtherInfo(otherInfo);
        return menstrualRecordRepository.save(record);
    }

    public List<MenstrualRecord> getHistory(Long userId) {
        return menstrualRecordRepository.findByUserIdOrderByCycleStartDateDesc(userId);
    }

    public Optional<MenstrualRecord> getLatest(Long userId) {
        return menstrualRecordRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }

    public MenstrualRecord updateRecord(Long id, LocalDate cycleStartDate, LocalDate cycleEndDate,
                                         String flowLevel, Boolean isInPeriod, Boolean hasPain, String otherInfo) {
        Optional<MenstrualRecord> opt = menstrualRecordRepository.findById(id);
        if (opt.isEmpty()) return null;
        MenstrualRecord record = opt.get();
        if (cycleStartDate != null) record.setCycleStartDate(cycleStartDate);
        if (cycleEndDate != null) record.setCycleEndDate(cycleEndDate);
        if (flowLevel != null) record.setFlowLevel(flowLevel);
        if (isInPeriod != null) record.setIsInPeriod(isInPeriod);
        if (hasPain != null) record.setHasPain(hasPain);
        if (otherInfo != null) record.setOtherInfo(otherInfo);
        return menstrualRecordRepository.save(record);
    }

    public void deleteRecord(Long id) {
        menstrualRecordRepository.deleteById(id);
    }

    /**
     * 计算当前所处经期阶段
     */
    public CyclePhase getCurrentPhase(Long userId) {
        Optional<MenstrualRecord> latestOpt = getLatest(userId);
        int avgCycleLength = calculateAvgCycleLength(userId);

        if (latestOpt.isEmpty()) {
            return CyclePhase.builder()
                    .phase("未记录经期")
                    .avgCycleLength(avgCycleLength)
                    .exerciseRecommendation("暂无建议，请先记录经期")
                    .dietRecommendation("暂无建议，请先记录经期")
                    .bodyStatus("暂无数据")
                    .build();
        }

        MenstrualRecord latest = latestOpt.get();

        // 优先使用 isInPeriod 字段判断
        if (Boolean.TRUE.equals(latest.getIsInPeriod())) {
            LocalDate today = LocalDate.now();
            int cycleDay = (int) ChronoUnit.DAYS.between(latest.getCycleStartDate(), today) + 1;
            int daysRemaining = latest.getCycleEndDate() != null
                    ? (int) ChronoUnit.DAYS.between(today, latest.getCycleEndDate())
                    : DEFAULT_PERIOD_DAYS - cycleDay + 1;
            if (daysRemaining < 0) daysRemaining = 0;

            return CyclePhase.builder()
                    .phase("经期")
                    .cycleDay(cycleDay)
                    .daysRemaining(daysRemaining)
                    .isInPeriod(true)
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(latest.getCycleStartDate().toString())
                    .exerciseRecommendation("建议轻度运动，如散步、瑜伽、拉伸，避免剧烈运动")
                    .dietRecommendation("饮食温热清淡，补铁（红肉、动物肝脏、菠菜），喝红糖姜茶，避免生冷辛辣")
                    .bodyStatus("可能感到疲劳、腹部不适，保持充足睡眠和心情愉悦")
                    .build();
        }

        // 不在经期，使用周期计算
        LocalDate lastPeriodStart = latest.getCycleStartDate();
        LocalDate today = LocalDate.now();

        // 计算从上次经期开始到今天过了多少天（可能为负数，表示经期还没到）
        long daysSinceStart = ChronoUnit.DAYS.between(lastPeriodStart, today);

        // 确保avgCycleLength有效（最少1天，最多60天）
        if (avgCycleLength < 1) avgCycleLength = DEFAULT_CYCLE_LENGTH;
        if (avgCycleLength > 60) avgCycleLength = DEFAULT_CYCLE_LENGTH;

        // 计算下次经期预计开始日期 = 上次经期开始 + 平均周期
        LocalDate nextPeriodDate = lastPeriodStart.plusDays(avgCycleLength);
        long daysUntilNext = ChronoUnit.DAYS.between(today, nextPeriodDate);

        // 安全检查：daysUntilNext最小为0（不能为负）
        if (daysUntilNext < 0) daysUntilNext = 0;

        // 计算当前周期第几天（处理负数情况）
        int cycleDay;
        if (daysSinceStart >= 0) {
            cycleDay = (int) ((daysSinceStart % avgCycleLength) + 1);
        } else {
            // 今天在经期开始之前，说明下次经期还没到
            // cycleDay应该是一个较大的值（接近下次经期的天数）
            cycleDay = (int) (avgCycleLength + daysSinceStart + 1);
            if (cycleDay > avgCycleLength) cycleDay = avgCycleLength;
            if (cycleDay < 1) cycleDay = 1;
        }

        // 判断阶段
        CyclePhase phase;
        if (daysSinceStart < 0 && cycleDay > DEFAULT_PERIOD_DAYS) {
            // 今天在经期开始之前，下次经期还没到
            phase = CyclePhase.builder()
                    .phase("经期未开始")
                    .cycleDay(cycleDay)
                    .daysUntilNextPeriod((int) -daysSinceStart)
                    .isInPeriod(false)
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(lastPeriodStart.toString())
                    .exerciseRecommendation("经期还没开始，保持良好生活习惯")
                    .dietRecommendation("均衡饮食，补充营养，迎接下次经期")
                    .bodyStatus("经期预计还有 " + (-daysSinceStart) + " 天到来")
                    .build();
        } else if (cycleDay <= DEFAULT_PERIOD_DAYS) {
            // 经期 - 只有当明确记录isInPeriod=true时才标记为经期
            phase = CyclePhase.builder()
                    .phase("经期")
                    .cycleDay(cycleDay)
                    .daysRemaining(DEFAULT_PERIOD_DAYS - cycleDay + 1)
                    .isInPeriod(false)  // 周期计算不作为经期依据，只用最新记录的isInPeriod字段
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(lastPeriodStart.toString())
                    .exerciseRecommendation("建议轻度运动，如散步、瑜伽、拉伸，避免剧烈运动")
                    .dietRecommendation("饮食温热清淡，补铁（红肉、动物肝脏、菠菜），喝红糖姜茶，避免生冷辛辣")
                    .bodyStatus("可能感到疲劳、腹部不适，保持充足睡眠和心情愉悦")
                    .build();
        } else if (cycleDay <= OVULATION_DAY) {
            // 卵泡期（经期后~排卵前）
            phase = CyclePhase.builder()
                    .phase("卵泡期")
                    .cycleDay(cycleDay)
                    .daysUntilNextPeriod((int) daysUntilNext)
                    .isInPeriod(false)
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(lastPeriodStart.toString())
                    .exerciseRecommendation("运动黄金期！适合高强度燃脂、空腹有氧、力量训练，代谢率高")
                    .dietRecommendation("高蛋白饮食（鸡胸肉、鱼、蛋）、适量碳水（糙米、燕麦）、多吃蔬菜水果")
                    .bodyStatus("精力充沛，新陈代谢快，是减脂最佳时期")
                    .build();
        } else if (cycleDay == OVULATION_DAY + 1) {
            // 排卵日
            phase = CyclePhase.builder()
                    .phase("排卵日")
                    .cycleDay(cycleDay)
                    .daysUntilNextPeriod((int) daysUntilNext)
                    .isInPeriod(false)
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(lastPeriodStart.toString())
                    .exerciseRecommendation("适中强度运动，如游泳、骑行、操课，排卵日身体状态佳")
                    .dietRecommendation("均衡营养，多吃富含锌的食物（牡蛎、牛肉），补充叶酸")
                    .bodyStatus("身体状态最佳，情绪愉悦，食欲可能增加")
                    .build();
        } else {
            // 黄体期（排卵后~下次经期前）
            phase = CyclePhase.builder()
                    .phase("黄体期")
                    .cycleDay(cycleDay)
                    .daysUntilNextPeriod((int) daysUntilNext)
                    .isInPeriod(false)
                    .avgCycleLength(avgCycleLength)
                    .lastPeriodStart(lastPeriodStart.toString())
                    .exerciseRecommendation("中轻度运动为主（瑜伽、普拉提、散步），避免高强度以防水肿")
                    .dietRecommendation("控盐（每天<6g）、补镁（坚果、深绿蔬菜）、补充优质脂肪（牛油果、橄榄油），少吃油炸食品")
                    .bodyStatus("可能情绪波动、易倦、轻微水肿，属正常现象，注意休息")
                    .build();
        }

        return phase;
    }

    private int calculateAvgCycleLength(Long userId) {
        List<MenstrualRecord> records = getHistory(userId);
        if (records.size() < 2) return DEFAULT_CYCLE_LENGTH;

        int sum = 0;
        int count = 0;
        for (int i = 0; i < records.size() - 1; i++) {
            MenstrualRecord current = records.get(i);
            MenstrualRecord previous = records.get(i + 1);
            if (current.getCycleStartDate() != null && previous.getCycleStartDate() != null) {
                int diff = (int) ChronoUnit.DAYS.between(previous.getCycleStartDate(), current.getCycleStartDate());
                if (diff > 0 && diff < 60) { // 合理周期范围 1-60 天
                    sum += diff;
                    count++;
                }
            }
        }
        return count > 0 ? sum / count : DEFAULT_CYCLE_LENGTH;
    }

    @lombok.Data
    @lombok.Builder
    public static class CyclePhase {
        private String phase;           // 经期/卵泡期/排卵日/黄体期
        private Integer cycleDay;       // 当前周期第几天
        private Integer daysRemaining;  // 该阶段剩余天数
        private Integer daysUntilNextPeriod;  // 距离下次经期天数
        private Boolean isInPeriod;     // 是否处于经期
        private Integer avgCycleLength; // 平均周期长度
        private String lastPeriodStart; // 最近一次经期开始日期

        // 以下为建议内容
        private String exerciseRecommendation;  // 运动建议
        private String dietRecommendation;      // 饮食建议
        private String bodyStatus;              // 身体状态描述
    }
}
