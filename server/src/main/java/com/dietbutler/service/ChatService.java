package com.dietbutler.service;

import com.dietbutler.config.LlmProperties;
import com.dietbutler.dto.ChatResponse;
import com.dietbutler.dto.LlmRequest;
import com.dietbutler.dto.LlmResponse;
import com.dietbutler.entity.User;
import com.dietbutler.entity.WeightRecord;
import com.dietbutler.entity.BodyMeasurement;
import com.dietbutler.entity.MenstrualRecord;
import com.dietbutler.service.MenstrualService;
import com.dietbutler.service.BodyMeasurementService;
import com.dietbutler.pecs.UserContextHolder;
import com.dietbutler.pecs.PersonContext;
import com.dietbutler.pecs.solution.PecsReasoner;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final WebClient webClient;
    private final LlmProperties llmProperties;
    private final UserService userService;
    private final WeightRecordService weightRecordService;
    private final MenstrualService menstrualService;
    private final BodyMeasurementService bodyMeasurementService;
    private final ObjectMapper objectMapper;
    private final DailyPlanService dailyPlanService;
    private final EmotionSupportService emotionSupportService;
    private final ExerciseRecordService exerciseRecordService;
    private final DietRecordService dietRecordService;

    // PECS 架构组件
    private final PecsReasoner pecsReasoner;
    private final UserContextHolder userContextHolder;

    // Spring AI
    private final AiService aiService;

    // 对话Session管理（Redis/内存）
    private final ChatSessionManager chatSessionService;

    // 体重匹配模式
    private static final Pattern WEIGHT_PATTERN = Pattern.compile(
            "(\\d+\\.?\\d*)\\s*(公斤|kg|千克|斤|KG)"
    );

    /**
     * 创建新的对话会话
     */
    public String createSession(Long userId) {
        return chatSessionService.createSession(userId);
    }

    public ChatResponse chat(Long userId, String userMessage, String sessionId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return ChatResponse.builder()
                    .reply("抱歉，未找到您的用户信息，请联系管理员。")
                    .intent("error")
                    .build();
        }

        // 获取或创建会话
        sessionId = chatSessionService.getOrCreateSession(userId, sessionId);

        // 保存用户消息到Redis会话
        chatSessionService.addMessage(userId, sessionId, "user", userMessage);

        // 检测负面情绪并自动干预
        emotionSupportService.detectAndLogEmotion(userId, userMessage);

        boolean isProfileComplete = userService.isProfileComplete(user);
        String intent = detectIntent(userMessage);

        // 意图分发
        ChatResponse response;
        switch (intent) {
            case "greeting":
                return handleGreeting(userId, user, userMessage, sessionId);

            case "profile_query":
                return handleProfileQuery(userId, user, sessionId);

            case "weight_record":
                return handleWeightRecord(userId, user, userMessage, sessionId);

            case "weight_curve":
                return handleWeightCurve(userId, user, sessionId);

            case "profile_update":
                return handleProfileUpdate(userId, user, userMessage, sessionId);

            case "daily_plan":
                return handleDailyPlan(userId, user, sessionId);

            case "checkin":
                return handleCheckin(userId, user, sessionId);

            case "emotion":
                return handleEmotion(userId, user, userMessage, sessionId);

            case "standup":
                return handleStandup(userId, sessionId);

            case "exercise_record":
                return handleExerciseRecord(userId, user, userMessage, sessionId);

            case "diet_record":
                return handleDietRecord(userId, user, userMessage, sessionId);

            default:
                // 新用户且未建档：引导建档
                if (!isProfileComplete) {
                    response = handleProfileSetup(userId, user, userMessage, sessionId);
                } else {
                    // 老用户：正常对话 + 检查是否需要提醒称重
                    response = handleNormalChat(userId, user, userMessage, sessionId);
                }
        }

        // 保存助手回复到会话（不保存情绪回复，避免干扰）
        if (response != null && response.getReply() != null && !"emotion".equals(response.getIntent())) {
            chatSessionService.addMessage(userId, sessionId, "assistant", response.getReply());
        }

        return response;
    }

    /**
     * 处理打招呼
     */
    private ChatResponse handleGreeting(Long userId, User user, String userMessage, String sessionId) {
        String reply;
        boolean isProfileComplete = userService.isProfileComplete(user);
        String userTypeName = getUserTypeName(user.getUserType() != null ? user.getUserType() : "weight_loss");

        if (!isProfileComplete) {
            reply = "你好呀！👋 我是减减，你的专属AI私教！\n\n" +
                    "很高兴认识你！为了更好地帮助您，请先完成信息建档：\n\n" +
                    "📝 **建档模板**（直接复制填写）：\n" +
                    "```\n" +
                    "我叫[昵称]，今年[年龄]岁，是[男/女]，身高[身高]cm，初始体重[体重]公斤，目标体重[体重]公斤，我是[减重/塑形/维持/增肌]\n" +
                    "```\n\n" +
                    "📌 **示例**：\n" +
                    "```\n" +
                    "我叫小明，今年25岁，是男，身高175cm，初始体重80公斤，目标体重65公斤，我是减重\n" +
                    "```\n\n" +
                    "💡 也可以分几次告诉我，比如先说「我叫小明」，下次再补充其他信息～ 😊\n\n\n" +
                    "📌 您随时可以点击左上角「我的」按钮修改档案信息哦！";
        } else {
            Optional<WeightRecord> todayRecord = weightRecordService.getToday(userId);
            String todayStatus = todayRecord.isEmpty() ? "今天还没称重哦！📋" : "今天已记录：" + todayRecord.get().getWeight() + "kg ✅";
            reply = "你好呀！" + user.getNickname() + "～ 👋\n\n" +
                    "你是【" + userTypeName + "】，加油！💪\n\n" +
                    todayStatus + "\n\n" +
                    "有什么想看的？\n" +
                    "• 📈 查看体重变化曲线\n" +
                    "• 📋 查看我的档案\n" +
                    "• 💬 和我聊聊";
        }

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("greeting").build();
    }

    /**
     * 处理建档
     */
    private ChatResponse handleProfileSetup(Long userId, User user, String userMessage, String sessionId) {
        boolean updated = tryUpdateProfile(user, userMessage);
        updated |= tryUpdateMenstrual(userId, userMessage);
        updated |= tryUpdateMeasurement(userId, userMessage);

        if (updated) {
            userService.updateUser(user);
        }

        if (userService.isProfileComplete(user)) {
            String reply = "✅ 建档完成！欢迎【" + user.getNickname() + "】！\n\n" +
                    buildCompleteProfile(user) +
                    "\n每天早上空腹称重并告诉我，我会帮你记录和分析！";
            saveMessage(userId, sessionId, "assistant", reply);
            return ChatResponse.builder().reply(reply).intent("profile_complete").build();
        }

        // 仍然不完整，给出下一步引导
        String reply = buildSetupGuide(user);
        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("profile_setup").build();
    }

    private String buildSetupGuide(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("好的，已经记住这个信息了！✅\n\n");

        List<String> missing = new ArrayList<>();
        if (user.getNickname() == null || "减减用户".equals(user.getNickname())) missing.add("昵称");
        if (user.getAge() == null) missing.add("年龄");
        if (user.getGender() == null) missing.add("性别");
        if (user.getHeight() == null) missing.add("身高");
        if (user.getTargetWeight() == null) missing.add("目标体重");

        if (!missing.isEmpty()) {
            sb.append("📝 还需要告诉我：");
            for (String m : missing) {
                sb.append("【").append(m).append("】");
            }
            sb.append("\n\n可以继续告诉我，比如：「").append(missing.get(0)).append("是xxx」");
        }

        return sb.toString();
    }

    private String buildCompleteProfile(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **您的档案**\n");
        sb.append("• 昵称：").append(user.getNickname()).append("\n");
        if (user.getAge() != null) sb.append("• 年龄：").append(user.getAge()).append("岁\n");
        if (user.getGender() != null) sb.append("• 性别：").append(user.getGender() == 1 ? "男" : "女").append("\n");
        if (user.getHeight() != null) sb.append("• 身高：").append(user.getHeight()).append("cm\n");
        if (user.getInitialWeight() != null) sb.append("• 初始体重：").append(user.getInitialWeight()).append("kg\n");
        if (user.getTargetWeight() != null) sb.append("• 目标体重：").append(user.getTargetWeight()).append("kg\n");
        if (user.getWeightLossPeriod() != null) sb.append("• 减重周期：").append(user.getWeightLossPeriod()).append("天\n");
        if (user.getStartWeightDate() != null) sb.append("• 开始日期：").append(user.getStartWeightDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        if (user.getDietaryTaboo() != null) sb.append("• 饮食忌口：").append(user.getDietaryTaboo()).append("\n");
        if (user.getSleepStart() != null && user.getSleepEnd() != null) sb.append("• 作息：").append(user.getSleepStart()).append("-").append(user.getSleepEnd()).append("\n");
        if (user.getBasicMetabolism() != null) sb.append("• 基础代谢：").append(user.getBasicMetabolism()).append(" kcal/天\n");
        return sb.toString();
    }

    /**
     * 处理体重记录
     */
    private ChatResponse handleWeightRecord(Long userId, User user, String userMessage, String sessionId) {
        Double weight = extractWeight(userMessage);
        String reply;

        if (weight != null) {
            // 提取作息时间，若用户未提供则使用档案中的默认值
            String sleepSchedule = extractSleepSchedule(userMessage);
            java.time.LocalTime sleepStart = null;
            java.time.LocalTime sleepEnd = null;
            if (sleepSchedule != null) {
                String[] parts = sleepSchedule.split("-");
                if (parts.length == 2) {
                    sleepStart = java.time.LocalTime.parse(parts[0]);
                    sleepEnd = java.time.LocalTime.parse(parts[1]);
                }
            } else {
                // 用户未输入作息，使用档案默认值
                sleepStart = user.getSleepStart();
                sleepEnd = user.getSleepEnd();
            }
            weightRecordService.addWeight(userId, weight, null, sleepStart, sleepEnd, null);
            WeightStatistics stats = getWeightStatistics(userId);

            reply = "📝 体重记录成功！\n\n";
            reply += "• 今日体重：" + weight + "kg\n";
            if (user.getInitialWeight() != null) {
                double change = weight - user.getInitialWeight();
                reply += "• 初始体重：" + user.getInitialWeight() + "kg\n";
                reply += "• 累计变化：" + (change >= 0 ? "+" : "") + String.format("%.1f", change) + "kg\n";
            }
            if (user.getTargetWeight() != null) {
                double toGoal = weight - user.getTargetWeight();
                reply += "• 距离目标：" + (toGoal > 0 ? String.format("%.1f", toGoal) + "kg" : "已达成！🎉") + "\n";
            }
        } else {
            reply = "想记录体重吗？直接告诉我就可以了，比如：「今天体重68公斤」😊";
        }

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("weight_record").build();
    }

    /**
     * 处理体重曲线查询
     */
    private ChatResponse handleWeightCurve(Long userId, User user, String sessionId) {
        List<WeightRecord> history = weightRecordService.getHistory(userId);

        if (history.isEmpty()) {
            String reply = "还没有体重记录呢～ 告诉我今天的体重吧，比如：「今天体重68公斤」";
            saveMessage(userId, sessionId, "assistant", reply);
            return ChatResponse.builder().reply(reply).intent("weight_curve").build();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📈 **体重变化曲线**\n\n");

        // 最近7天的数据
        int days = Math.min(7, history.size());
        sb.append("最近").append(days).append("天记录：\n");

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");

        List<WeightRecord> recentRecords = history.subList(0, days);
        Collections.reverse(recentRecords); // 从早到晚排序

        for (WeightRecord r : recentRecords) {
            String date = r.getRecordDate().format(fmt);
            sb.append(date).append("  ");
            // 简单可视化
            double diff = r.getWeight() - (user.getTargetWeight() != null ? user.getTargetWeight() : r.getWeight());
            int bars = Math.max(1, Math.min(10, (int) (diff / 2)));
            for (int i = 0; i < bars; i++) sb.append("█");
            sb.append(" ").append(r.getWeight()).append("kg\n");
        }

        // 统计
        WeightStatistics stats = getWeightStatistics(userId);
        if (stats.initialWeight != null && stats.latestWeight != null) {
            double totalChange = stats.latestWeight - stats.initialWeight;
            sb.append("\n📊 累计变化：").append(totalChange >= 0 ? "+" : "").append(String.format("%.1f", totalChange)).append("kg\n");
        }
        if (user.getTargetWeight() != null && stats.latestWeight != null) {
            double toGoal = stats.latestWeight - user.getTargetWeight();
            sb.append("🎯 距离目标：").append(toGoal > 0 ? String.format("%.1f", toGoal) + "kg" : "已达成！").append("\n");
        }

        saveMessage(userId, sessionId, "assistant", sb.toString());
        return ChatResponse.builder().reply(sb.toString()).intent("weight_curve").build();
    }

    private WeightStatistics getWeightStatistics(Long userId) {
        List<WeightRecord> history = weightRecordService.getHistory(userId);
        WeightStatistics stats = new WeightStatistics();
        if (!history.isEmpty()) {
            stats.latestWeight = history.get(0).getWeight();
            stats.initialWeight = history.get(history.size() - 1).getWeight();
            stats.totalChange = stats.latestWeight - stats.initialWeight;
        }
        return stats;
    }

    /**
     * 处理档案查询
     */
    private ChatResponse handleProfileQuery(Long userId, User user, String sessionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("📋 **您的完整档案**\n\n");

        // === 基本信息 ===
        sb.append("【基本信息】\n");
        sb.append("• 昵称：").append(user.getNickname()).append("\n");
        sb.append("• 用户类型：").append(getUserTypeName(user.getUserType() != null ? user.getUserType() : "weight_loss")).append("\n");
        if (user.getGender() != null) sb.append("• 性别：").append(user.getGender() == 1 ? "男" : "女").append("\n");
        if (user.getAge() != null) sb.append("• 年龄：").append(user.getAge()).append("岁\n");
        if (user.getHeight() != null) sb.append("• 身高：").append(user.getHeight()).append("cm\n");

        // === 体质标签 ===
        if (user.getConstitutionTags() != null && !user.getConstitutionTags().isEmpty()) {
            sb.append("\n【体质标签】\n");
            sb.append("• ").append(user.getConstitutionTags()).append("\n");
        }

        // === 体重信息 ===
        sb.append("\n【体重信息】\n");
        List<WeightRecord> history = weightRecordService.getHistory(userId);
        if (!history.isEmpty()) {
            WeightRecord latest = history.get(0);
            WeightRecord oldest = history.get(history.size() - 1);
            sb.append("• 当前体重：").append(latest.getWeight()).append("kg\n");
            sb.append("• 初始体重：").append(oldest.getWeight()).append("kg\n");
            double change = latest.getWeight() - oldest.getWeight();
            sb.append("• 累计变化：").append(change > 0 ? "+" : "").append(String.format("%.1f", change)).append("kg\n");
            if (user.getTargetWeight() != null) {
                sb.append("• 目标体重：").append(user.getTargetWeight()).append("kg\n");
                double toGoal = latest.getWeight() - user.getTargetWeight();
                sb.append("• 距离目标：").append(toGoal > 0 ? String.format("%.1f", toGoal) + "kg" : "已达成！🎉").append("\n");
            }
        } else {
            if (user.getInitialWeight() != null) sb.append("• 初始体重：").append(user.getInitialWeight()).append("kg\n");
            if (user.getTargetWeight() != null) sb.append("• 目标体重：").append(user.getTargetWeight()).append("kg\n");
        }

        if (user.getStartWeightDate() != null) sb.append("• 开始减重：").append(user.getStartWeightDate()).append("\n");
        if (user.getWeightLossPeriod() != null) sb.append("• 减重周期：").append(user.getWeightLossPeriod()).append("天\n");
        if (user.getBasicMetabolism() != null) sb.append("• 基础代谢：").append(user.getBasicMetabolism()).append(" kcal/天\n");

        // === 饮食作息 ===
        sb.append("\n【饮食作息】\n");
        if (user.getDietaryTaboo() != null && !user.getDietaryTaboo().isEmpty()) {
            sb.append("• 饮食忌口：").append(user.getDietaryTaboo()).append("\n");
        } else {
            sb.append("• 饮食忌口：无\n");
        }
        if (user.getSleepStart() != null && user.getSleepEnd() != null) {
            sb.append("• 作息：").append(user.getSleepStart().toString()).append(" 睡 - ").append(user.getSleepEnd().toString()).append(" 起\n");
        } else {
            sb.append("• 作息：未设置\n");
        }

        // === 经期信息 ===
        sb.append("\n【经期信息】\n");
        MenstrualService.CyclePhase phase = menstrualService.getCurrentPhase(userId);
        List<MenstrualRecord> menstrualHistory = menstrualService.getHistory(userId);
        if (phase != null && phase.getPhase() != null && !"未记录经期".equals(phase.getPhase())) {
            if (Boolean.TRUE.equals(phase.getIsInPeriod())) {
                // 当前明确处于经期
                sb.append("• 是否处于经期：是\n");
                if (!menstrualHistory.isEmpty()) {
                    MenstrualRecord latest = menstrualHistory.get(0);
                    sb.append("• 经期开始：").append(latest.getCycleStartDate().toString()).append("\n");
                    if (latest.getCycleEndDate() != null) sb.append("• 经期结束：").append(latest.getCycleEndDate().toString()).append("\n");
                }
                if (phase.getDaysRemaining() != null && phase.getDaysRemaining() > 0) sb.append("• 预计结束：约").append(phase.getDaysRemaining()).append("天后\n");
            } else {
                // 当前不在经期（或未明确记录）
                sb.append("• 是否处于经期：否\n");
                if (!menstrualHistory.isEmpty()) {
                    MenstrualRecord latest = menstrualHistory.get(0);
                    sb.append("• 上次经期开始：").append(latest.getCycleStartDate().toString()).append("\n");
                    if (latest.getCycleEndDate() != null) sb.append("• 上次经期结束：").append(latest.getCycleEndDate().toString()).append("\n");
                }
                sb.append("• 当前阶段：").append(phase.getPhase()).append("\n");
                if (phase.getDaysUntilNextPeriod() != null) sb.append("• 距离下次经期：约").append(phase.getDaysUntilNextPeriod()).append("天\n");
            }
            // 显示经期详细信息
            if (!menstrualHistory.isEmpty()) {
                MenstrualRecord latest = menstrualHistory.get(0);
                if (latest.getFlowLevel() != null) {
                    String flow = "medium".equals(latest.getFlowLevel()) ? "中" : "light".equals(latest.getFlowLevel()) ? "少" : "多";
                    sb.append("• 经量：").append(flow).append("\n");
                }
                if (Boolean.TRUE.equals(latest.getHasPain())) {
                    sb.append("• 是否疼痛：是\n");
                }
                if (latest.getOtherInfo() != null && !latest.getOtherInfo().isEmpty()) {
                    sb.append("• 其他信息：").append(latest.getOtherInfo()).append("\n");
                }
            }
            if (phase.getExerciseRecommendation() != null) sb.append("• 运动建议：").append(phase.getExerciseRecommendation()).append("\n");
            if (phase.getDietRecommendation() != null) sb.append("• 饮食建议：").append(phase.getDietRecommendation()).append("\n");
        } else {
            sb.append("• 暂无经期记录\n");
        }

        // === 维度信息 ===
        sb.append("\n【身体维度】\n");
        Optional<BodyMeasurement> latestMeasurement = bodyMeasurementService.getLatest(userId);
        if (latestMeasurement.isPresent()) {
            BodyMeasurement m = latestMeasurement.get();
            sb.append("• 最近记录：").append(m.getRecordDate().toString()).append("\n");
            if (m.getWaist() != null) sb.append("• 腰围：").append(m.getWaist()).append("cm\n");
            if (m.getHip() != null) sb.append("• 臀围：").append(m.getHip()).append("cm\n");
            if (m.getChest() != null) sb.append("• 胸围：").append(m.getChest()).append("cm\n");
            if (m.getUpperArm() != null) sb.append("• 上臂围：").append(m.getUpperArm()).append("cm\n");
            if (m.getThigh() != null) sb.append("• 大腿围：").append(m.getThigh()).append("cm\n");
        } else {
            sb.append("• 暂无维度记录\n");
        }

        String reply = sb.toString();
        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("profile_query").build();
    }

    /**
     * 处理档案修改（自然语言）
     */
    private ChatResponse handleProfileUpdate(Long userId, User user, String userMessage, String sessionId) {
        boolean updated = tryUpdateProfile(user, userMessage);
        updated |= tryUpdateMenstrual(userId, userMessage);
        updated |= tryUpdateMeasurement(userId, userMessage);

        if (updated) {
            userService.updateUser(user);
            // 档案更新后，仍使用 LLM 来回复，这样更自然
            String reply = buildAndCallLLM(user, userMessage);
            saveMessage(userId, sessionId, "assistant", reply);
            return ChatResponse.builder().reply(reply).intent("profile_update").build();
        }

        // 没有识别到修改内容，给出引导
        String reply = "想修改档案吗？直接告诉我就可以了，比如：\n" +
                "• 「目标体重改成60公斤」\n" +
                "• 「我的昵称是小明」\n" +
                "• 「年龄改成28岁」\n" +
                "• 「我今天经期来了」\n" +
                "• 「记录一下维度：腰围80cm」";
        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("profile_update").build();
    }

    /**
     * 处理正常对话（老用户）
     */
    private ChatResponse handleNormalChat(Long userId, User user, String userMessage, String sessionId) {
        // 检查是否需要提醒称重
        Optional<WeightRecord> todayRecord = weightRecordService.getToday(userId);
        String reminder = "";
        if (todayRecord.isEmpty()) {
            List<WeightRecord> history = weightRecordService.getHistory(userId);
            if (!history.isEmpty()) {
                long hoursSince = ChronoUnit.HOURS.between(history.get(0).getCreatedAt(), LocalDateTime.now());
                if (hoursSince >= 20) {
                    reminder = "📋 提醒：您已经有" + hoursSince + "小时没有记录体重啦，记得称重哦～\n\n";
                }
            }
        }

        String reply;
        try {
            reply = buildAndCallLLM(user, userMessage);
            reply = reminder + reply;
        } catch (Exception e) {
            log.error("对话失败", e);
            reply = "抱歉，我现在有点忙，请稍后再试～";
        }

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("chat").build();
    }

    /**
     * 尝试从消息中提取并更新用户档案信息
     */
    private boolean tryUpdateProfile(User user, String message) {
        boolean updated = false;

        // 昵称
        String nickname = extractNickname(message);
        if (nickname != null && !nickname.isEmpty() && !nickname.equals(user.getNickname())) {
            user.setNickname(nickname);
            updated = true;
        }

        // 年龄
        Integer age = extractAge(message);
        if (age != null && !age.equals(user.getAge())) {
            user.setAge(age);
            updated = true;
        }

        // 性别
        Integer gender = extractGender(message);
        if (gender != null && !gender.equals(user.getGender())) {
            user.setGender(gender);
            updated = true;
        }

        // 身高
        Double height = extractHeight(message);
        if (height != null && !height.equals(user.getHeight())) {
            user.setHeight(height);
            updated = true;
        }

        // 初始体重
        Double initialWeight = extractInitialWeight(message);
        if (initialWeight != null && !initialWeight.equals(user.getInitialWeight())) {
            user.setInitialWeight(initialWeight);
            updated = true;
        }

        // 目标体重
        Double targetWeight = extractTargetWeight(message);
        if (targetWeight != null && !targetWeight.equals(user.getTargetWeight())) {
            user.setTargetWeight(targetWeight);
            updated = true;
        }

        // 减重周期
        Integer period = extractWeightLossPeriod(message);
        if (period != null && !period.equals(user.getWeightLossPeriod())) {
            user.setWeightLossPeriod(period);
            updated = true;
        }

        // 开始减重日期
        LocalDate startDate = extractStartWeightDate(message);
        if (startDate != null && !startDate.equals(user.getStartWeightDate())) {
            user.setStartWeightDate(startDate);
            updated = true;
        }

        // 饮食忌口
        String dietaryTaboo = extractDietaryTaboo(message);
        if (dietaryTaboo != null && !dietaryTaboo.equals(user.getDietaryTaboo())) {
            user.setDietaryTaboo(dietaryTaboo);
            updated = true;
        }

        // 作息时间
        String sleepSchedule = extractSleepSchedule(message);
        if (sleepSchedule != null) {
            // 格式: "HH:00-HH:00"，分别是睡眠时间和起床时间
            String[] parts = sleepSchedule.split("-");
            if (parts.length == 2) {
                user.setSleepStart(java.time.LocalTime.parse(parts[0]));
                user.setSleepEnd(java.time.LocalTime.parse(parts[1]));
                updated = true;
            }
        }

        // 提醒设置
        updated |= tryUpdateReminderSettings(user, message);

        // 用户类型（减重/塑形/维持/增肌）
        String userType = extractUserType(message);
        if (userType != null && !userType.equals(user.getUserType())) {
            user.setUserType(userType);
            updated = true;
        }

        // 体质标签
        String constitutionTags = extractConstitutionTags(message);
        if (constitutionTags != null) {
            user.setConstitutionTags(constitutionTags);
            updated = true;
        }

        // 运动习惯
        Integer exerciseFrequency = extractExerciseFrequency(message);
        if (exerciseFrequency != null) {
            user.setExerciseFrequency(exerciseFrequency);
            updated = true;
        }

        // 饮食习惯
        String dietPreference = extractDietPreference(message);
        if (dietPreference != null) {
            user.setDietPreference(dietPreference);
            updated = true;
        }

        // 生活压力
        Integer workPressure = extractWorkPressure(message);
        if (workPressure != null) {
            user.setWorkPressure(workPressure);
            updated = true;
        }

        // 饮水量目标
        Integer waterIntake = extractWaterIntake(message);
        if (waterIntake != null) {
            user.setWaterIntake(waterIntake);
            updated = true;
        }

        return updated;
    }

    /**
     * 尝试从消息中提取并更新经期信息
     */
    private boolean tryUpdateMenstrual(Long userId, String message) {
        // 检查是否提及经期相关
        if (!message.contains("经期") && !message.contains("月经") && !message.contains("来例假")) {
            return false;
        }

        Boolean isInPeriod = extractIsInPeriod(message);
        LocalDate cycleStartDate = extractMenstrualStartDate(message);
        LocalDate cycleEndDate = extractMenstrualEndDate(message);
        String flowLevel = extractFlowLevel(message);
        Boolean hasPain = extractHasPain(message);
        String otherInfo = extractOtherInfo(message);

        // 如果没有提取到任何有用的经期信息，返回false
        if (isInPeriod == null && cycleStartDate == null && flowLevel == null && hasPain == null && otherInfo == null) {
            return false;
        }

        // 创建新的经期记录
        MenstrualRecord record = new MenstrualRecord();
        record.setUserId(userId);
        record.setIsInPeriod(isInPeriod != null ? isInPeriod : false);

        // 如果提供了新的经期开始日期
        if (cycleStartDate != null) {
            record.setCycleStartDate(cycleStartDate);
        } else if (isInPeriod != null && isInPeriod) {
            // 如果说"经期来了"但没提供日期，默认今天
            record.setCycleStartDate(LocalDate.now());
        }

        record.setCycleEndDate(cycleEndDate);
        record.setFlowLevel(flowLevel);
        record.setHasPain(hasPain != null ? hasPain : false);
        record.setOtherInfo(otherInfo);

        if (cycleEndDate != null && record.getCycleStartDate() != null) {
            record.setCycleLength((int) ChronoUnit.DAYS.between(record.getCycleStartDate(), cycleEndDate) + 1);
        }

        menstrualService.addRecord(
                record.getUserId(),
                record.getCycleStartDate(),
                record.getCycleEndDate(),
                record.getFlowLevel(),
                record.getIsInPeriod(),
                record.getHasPain(),
                record.getOtherInfo()
        );
        return true;
    }

    /**
     * 尝试从消息中提取并更新身体维度信息
     */
    private boolean tryUpdateMeasurement(Long userId, String message) {
        // 检查是否提及维度相关
        if (!message.contains("腰围") && !message.contains("臀围") && !message.contains("胸围") &&
                !message.contains("上臂围") && !message.contains("大腿围") && !message.contains("维度")) {
            return false;
        }

        Double waist = extractMeasurement(message, "腰围");
        Double hip = extractMeasurement(message, "臀围");
        Double chest = extractMeasurement(message, "胸围");
        Double arm = extractMeasurement(message, "上臂围");
        Double thigh = extractMeasurement(message, "大腿围");

        // 如果没有提取到任何有用的维度信息，返回false
        if (waist == null && hip == null && chest == null && arm == null && thigh == null) {
            return false;
        }

        // 使用 addMeasurement 方法
        bodyMeasurementService.addMeasurement(userId, waist, hip, chest, arm, thigh, null, null, null, null);
        return true;
    }

    private Boolean extractIsInPeriod(String message) {
        if (message.contains("经期来了") || message.contains("来经期") || message.contains("来月经") ||
                message.contains("经期开始") || message.contains("正在经期") || message.contains("经期中")) {
            return true;
        }
        if (message.contains("经期结束") || message.contains("经期停了")) {
            return false;
        }
        return null;
    }

    private LocalDate extractMenstrualStartDate(String message) {
        // 格式：xxxx年xx月xx日, xxxx-xx-xx, xxxx/mm/dd, 今天, 昨天
        if (message.contains("今天")) {
            return LocalDate.now();
        }
        if (message.contains("昨天")) {
            return LocalDate.now().minusDays(1);
        }
        Pattern[] patterns = {
                Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日"),
                Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})"),
                Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int day = Integer.parseInt(m.group(3));
                    return LocalDate.of(year, month, day);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private LocalDate extractMenstrualEndDate(String message) {
        if (message.contains("经期结束") || message.contains("结束日期")) {
            Pattern[] patterns = {
                    Pattern.compile("结束[是到]?(\\d{4})年(\\d{1,2})月(\\d{1,2})日"),
                    Pattern.compile("结束[是到]?(\\d{4})-(\\d{1,2})-(\\d{1,2})"),
                    Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日结束"),
                    Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})结束")
            };
            for (Pattern p : patterns) {
                Matcher m = p.matcher(message);
                if (m.find()) {
                    try {
                        int year = Integer.parseInt(m.group(1));
                        int month = Integer.parseInt(m.group(2));
                        int day = Integer.parseInt(m.group(3));
                        return LocalDate.of(year, month, day);
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

    private String extractFlowLevel(String message) {
        if (message.contains("量多") || message.contains("经量多") || message.contains("血量多")) {
            return "heavy";
        }
        if (message.contains("量少") || message.contains("经量少") || message.contains("血量少")) {
            return "light";
        }
        if (message.contains("量中") || message.contains("经量中") || message.contains("正常")) {
            return "medium";
        }
        return null;
    }

    private Boolean extractHasPain(String message) {
        if (message.contains("痛经") || message.contains("疼痛") || message.contains("肚子疼") ||
                message.contains("经痛") || message.contains("不舒服")) {
            return true;
        }
        if (message.contains("不疼") || message.contains("没有疼痛") || message.contains("无痛")) {
            return false;
        }
        return null;
    }

    private String extractOtherInfo(String message) {
        // 提取症状信息
        List<String> symptoms = new ArrayList<>();
        if (message.contains("腹胀")) symptoms.add("腹胀");
        if (message.contains("疲倦") || message.contains("疲劳")) symptoms.add("疲倦");
        if (message.contains("头痛")) symptoms.add("头痛");
        if (message.contains("情绪波动") || message.contains("心情不好")) symptoms.add("情绪波动");
        if (message.contains("长痘") || message.contains("爆痘")) symptoms.add("长痘");
        if (!symptoms.isEmpty()) {
            return String.join(",", symptoms);
        }
        return null;
    }

    private Double extractMeasurement(String message, String field) {
        Pattern pattern = Pattern.compile(field + "\\s*[是到]?\\s*(\\d+\\.?\\d*)\\s*(cm|厘米)?");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }

    /**
     * 从消息中提取昵称
     */
    private String extractNickname(String message) {
        Pattern[] patterns = {
            Pattern.compile("叫([^\\s，,。！!]+)"),
            Pattern.compile("昵称是([^\\s，,。！!]+)"),
            Pattern.compile("名字是([^\\s，,。！!]+)"),
            Pattern.compile("我是([^\\s，,。！!]+)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                String name = m.group(1).trim();
                if (name.length() <= 20) return name;
            }
        }
        return null;
    }

    /**
     * 从消息中提取年龄
     */
    private Integer extractAge(String message) {
        Pattern pattern = Pattern.compile("(?:年龄|岁)是?(\\d+)");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            int age = Integer.parseInt(m.group(1));
            if (age > 0 && age < 150) return age;
        }
        return null;
    }

    /**
     * 从消息中提取性别
     */
    private Integer extractGender(String message) {
        if (message.contains("男") || message.contains("男生") || message.contains("男孩")) {
            return 1;
        }
        if (message.contains("女") || message.contains("女生") || message.contains("女孩")) {
            return 2;
        }
        return null;
    }

    /**
     * 从消息中提取身高
     */
    private Double extractHeight(String message) {
        Pattern pattern = Pattern.compile("身高(\\d+\\.?\\d*)\\s*(cm|厘米|厘米)?");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }

    /**
     * 从消息中提取目标体重
     */
    private Double extractTargetWeight(String message) {
        // 排除"初始体重"等
        if (message.contains("初始体重")) return null;

        Pattern[] patterns = {
            Pattern.compile("目标体重[是改成为到]?(\\d+\\.?\\d*)"),
            Pattern.compile("目标[是改成为到]?(\\d+\\.?\\d*)\\s*(公斤|kg|千克)?"),
            Pattern.compile("(\\d+\\.?\\d*)\\s*(公斤|kg|千克)\\s*目标")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        }
        return null;
    }

    /**
     * 从消息中提取初始体重
     */
    private Double extractInitialWeight(String message) {
        Pattern[] patterns = {
            Pattern.compile("初始体重[是改成为到]?(\\d+\\.?\\d*)"),
            Pattern.compile("初始[是改成为到]?(\\d+\\.?\\d*)\\s*(公斤|kg|千克)?")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                return Double.parseDouble(m.group(1));
            }
        }
        return null;
    }

    /**
     * 从消息中提取减重周期
     */
    private Integer extractWeightLossPeriod(String message) {
        Pattern pattern = Pattern.compile("周期[是改成为到]?(\\d+)\\s*(天|周|个月)?");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            int period = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            if (unit != null) {
                if (unit.contains("周")) period *= 7;
                else if (unit.contains("个月")) period *= 30;
            }
            return period;
        }
        return null;
    }

    /**
     * 从消息中提取开始减重日期
     */
    private LocalDate extractStartWeightDate(String message) {
        // 格式：xxxx年xx月xx日, xxxx-xx-xx, xxxx/mm/dd
        Pattern[] patterns = {
            Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日"),
            Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})"),
            Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int day = Integer.parseInt(m.group(3));
                    return LocalDate.of(year, month, day);
                } catch (Exception e) {
                    // ignore invalid dates
                }
            }
        }
        return null;
    }

    /**
     * 从消息中提取饮食忌口
     */
    private String extractDietaryTaboo(String message) {
        // 匹配"忌口xxx"或"不能吃xxx"或"不吃xxx"
        Pattern[] patterns = {
            Pattern.compile("忌口([^，。！!]+)"),
            Pattern.compile("不能吃([^，。！!]+)"),
            Pattern.compile("不吃([^，。！!]+)"),
            Pattern.compile("过敏([^，。！!]+)")
        };
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                String taboo = m.group(1).trim();
                // 清理并返回
                taboo = taboo.replaceAll("[的物]", "").trim();
                if (!taboo.isEmpty() && taboo.length() <= 100) {
                    return taboo;
                }
            }
        }
        return null;
    }

    /**
     * 从消息中提取作息时间
     * 格式如: "作息22:00-06:00" -> "22:00-06:00"
     */
    private String extractSleepSchedule(String message) {
        // 先统一处理中文标点
        String normalized = message.replace('：', ':').replace('，', ',').replace('。', '.');

        // 尝试从消息中分别提取睡眠时间和起床时间（返回完整时间字符串，保留分钟）
        String sleepTime = extractSleepTime(normalized);
        String wakeTime = extractWakeTime(normalized);

        if (sleepTime != null && wakeTime != null) {
            return sleepTime + "-" + wakeTime;
        }

        // 尝试传统的作息格式
        Pattern[] patterns = {
            // 睡眠HH:MM，起床HH:MM
            Pattern.compile("睡眠.*?(\\d{1,2}):(\\d{2}).*?起床.*?(\\d{1,2}):(\\d{2})"),
            // 作息22:00-06:00
            Pattern.compile("作息[^\\d]*?(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})"),
            // 22:00-06:00
            Pattern.compile("(\\d{1,2}):(\\d{2})-(\\d{1,2}):(\\d{2})"),
            // HH点睡HH点起
            Pattern.compile("(\\d{1,2})点睡(\\d{1,2})点起"),
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(normalized);
            if (m.find()) {
                try {
                    int sHour, sMin = 0, wHour, wMin = 0;
                    if (m.groupCount() >= 4) {
                        sHour = Integer.parseInt(m.group(1));
                        sMin = Integer.parseInt(m.group(2));
                        wHour = Integer.parseInt(m.group(3));
                        wMin = Integer.parseInt(m.group(4));
                    } else {
                        sHour = Integer.parseInt(m.group(1));
                        wHour = Integer.parseInt(m.group(2));
                    }
                    return String.format("%02d:%02d-%02d:%02d", sHour, sMin, wHour, wMin);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    // 从消息中提取睡眠时间（晚上几点睡，保留分钟）
    private String extractSleepTime(String message) {
        // 匹配 "昨晚11点睡" "昨晚11:30睡觉" "11点睡" 等
        Pattern[] patterns = {
            Pattern.compile("昨晚(\\d{1,2}):(\\d{2}).*?(睡|睡觉)"),
            Pattern.compile("昨晚(\\d{1,2})点(\\d{2})?\\s*(睡|睡觉)"),
            Pattern.compile("(\\d{1,2}):(\\d{2}).*?(睡|睡觉)"),
            Pattern.compile("(\\d{1,2})点(\\d{2})?\\s*(睡|睡觉)"),
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    int hour = Integer.parseInt(m.group(1));
                    int min = m.group(2) != null ? Integer.parseInt(m.group(2)) : 0;
                    // 昨晚的时间需要转换为24小时制，11点 = 23点
                    if (hour < 12) {
                        hour += 12;
                    }
                    return String.format("%02d:%02d", hour, min);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    // 从消息中提取起床时间（早上几点醒，保留分钟）
    private String extractWakeTime(String message) {
        // 匹配 "今早7:30起床" "今早7点醒" "早上6点起床" "7点醒" "7:30起床" 等
        Pattern[] patterns = {
            // 今早7:30起床 或 早上7:30起床
            Pattern.compile("(今早|今晨|早上|早晨|上午)?\\s*(\\d{1,2}):(\\d{2}).*?(起床|醒|醒来|醒了)"),
            // 7:30起床（没有时间修饰语）
            Pattern.compile("(\\d{1,2}):(\\d{2}).*?(起床|醒|醒来|醒了)"),
            // 今早7点起床
            Pattern.compile("(今早|今晨|早上|早晨|上午)?\\s*(\\d{1,2})点(\\d{2})?\\s*(起床|醒|醒来|醒了)"),
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    int hour = Integer.parseInt(m.group(2));
                    int min = m.group(3) != null ? Integer.parseInt(m.group(3)) : 0;
                    return String.format("%02d:%02d", hour, min);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * 尝试更新提醒设置
     */
    private boolean tryUpdateReminderSettings(User user, String message) {
        boolean updated = false;

        // 开启/关闭提醒
        if (message.contains("开启提醒") || message.contains("启用提醒")) {
            user.setReminderEnabled(true);
            updated = true;
        }
        if (message.contains("关闭提醒") || message.contains("取消提醒") || message.contains("停止提醒")) {
            user.setReminderEnabled(false);
            updated = true;
        }

        // 提醒间隔
        Pattern intervalPattern = Pattern.compile("每(\\d+)\\s*(小时|天|周)");
        Matcher m = intervalPattern.matcher(message);
        if (m.find()) {
            int value = Integer.parseInt(m.group(1));
            String unit = m.group(2);
            int hours = value;
            if (unit.contains("天")) hours = value * 24;
            else if (unit.contains("周")) hours = value * 24 * 7;
            user.setReminderIntervalHours(hours);
            user.setReminderEnabled(true);
            updated = true;
        }

        return updated;
    }

    /**
     * 从消息中提取体重
     */
    private Double extractWeight(String message) {
        // 排除目标体重
        if (message.contains("目标体重")) return null;

        Matcher matcher = WEIGHT_PATTERN.matcher(message);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        Pattern simplePattern = Pattern.compile("(?:体重|称)是?(\\d+\\.?\\d*)");
        Matcher simpleMatcher = simplePattern.matcher(message);
        if (simpleMatcher.find()) {
            return Double.parseDouble(simpleMatcher.group(1));
        }
        return null;
    }

    /**
     * 识别用户意图
     */
    private String detectIntent(String message) {
        String lowerMsg = message.toLowerCase();
        String msg = message.toLowerCase();

        // 打招呼
        if (msg.contains("你好") || msg.contains("您好") || msg.contains("hi") ||
            msg.contains("hey") || msg.contains("hello") ||
            msg.contains("你是谁") || msg.contains("你是") || msg.contains("叫什么")) {
            return "greeting";
        }

        // 档案查询
        if (msg.contains("档案") || msg.contains("信息") || msg.contains("资料") ||
            msg.contains("个人") || msg.contains("查看档案") || msg.contains("我的信息") ||
            msg.contains("查档案") || msg.contains("看档案") || msg.contains("基本情况") ||
            msg.contains("我的情况") || msg.contains("我的状态") || msg.contains("我的基本")) {
            return "profile_query";
        }

        // 体重曲线
        if (msg.contains("曲线") || msg.contains("变化") || msg.contains("趋势") ||
            msg.contains("历史")) {
            return "weight_curve";
        }

        // 档案修改（自然语言）
        if (msg.contains("改成") || msg.contains("改为") || msg.contains("修改") ||
            msg.contains("更新") || msg.contains("昵称") || msg.contains("年龄") ||
            msg.contains("性别") || msg.contains("目标体重") ||
            msg.contains("经期") || msg.contains("月经") || msg.contains("腰围") ||
            msg.contains("臀围") || msg.contains("胸围") || msg.contains("维度") ||
            msg.contains("用户类型") || msg.contains("体质标签") || msg.contains("我是")) {
            return "profile_update";
        }

        // 体重记录
        if (msg.contains("体重") || msg.contains("称重") || msg.contains("今天体重") ||
            msg.contains("早上体重") || msg.contains("空腹体重")) {
            return "weight_record";
        }

        // 每日方案
        if (msg.contains("今日方案") || msg.contains("今天计划") || msg.contains("每日计划") ||
            msg.contains("今日建议") || msg.contains("今天怎么吃") || msg.contains("今天做什么")) {
            return "daily_plan";
        }

        // 打卡
        if (msg.contains("打卡") || msg.contains("签到")) {
            return "checkin";
        }

        // 情绪表达
        if (msg.contains("心情不好") || msg.contains("难受") || msg.contains("焦虑") ||
            msg.contains("暴食") || msg.contains("吃多了") || msg.contains("沮丧")) {
            return "emotion";
        }

        // 站立记录
        if (msg.contains("站起来了") || msg.contains("站立") || msg.contains("起来活动")) {
            return "standup";
        }

        // 运动记录
        if (msg.contains("跑步") || msg.contains("公里") || msg.contains("km") ||
            msg.contains("运动") || msg.contains("健身") || msg.contains("跑步机") ||
            msg.contains("游泳") || msg.contains("骑行") || msg.contains("瑜伽") ||
            msg.contains("走路") || msg.contains("步数") || msg.contains("消耗")) {
            return "exercise_record";
        }

        // 饮食记录
        if (msg.contains("吃了") || msg.contains("吃的是") || msg.contains("饮食") ||
            msg.contains("早餐") || msg.contains("午餐") || msg.contains("晚餐") ||
            msg.contains("加餐") || msg.contains("宵夜") || msg.contains("外卖") ||
            msg.contains("米饭") || msg.contains("蔬菜") || msg.contains("肉类") ||
            msg.contains("水果") || msg.contains("零食") || msg.contains("饮料") ||
            msg.contains("咖啡") || msg.contains("奶茶")) {
            return "diet_record";
        }

        return "chat";
    }

    /**
     * 处理每日方案请求 - PECS架构
     */
    private ChatResponse handleDailyPlan(Long userId, User user, String sessionId) {
        // 构建用户上下文
        PersonContext personContext = userContextHolder.buildFromUser(user);
        // 获取当前体重
        var stats = weightRecordService.getStatistics(userId);
        if (stats.getLatestWeight() != null) {
            personContext.setCurrentWeight(stats.getLatestWeight());
        }

        // 使用PECS推理生成方案
        String plan = pecsReasoner.reasonDiet(personContext, "今日饮食方案");
        saveMessage(userId, sessionId, "assistant", plan);
        return ChatResponse.builder().reply(plan).intent("daily_plan").build();
    }

    /**
     * 处理打卡
     */
    private ChatResponse handleCheckin(Long userId, User user, String sessionId) {
        String reply = "✅ 打卡成功！\n\n";
        reply += "今日完成：\n";
        reply += "• 体重记录：" + (weightRecordService.getToday(userId).isPresent() ? "已记录" : "未记录") + "\n";
        reply += "\n💪 坚持就是胜利！";

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("checkin").build();
    }

    /**
     * 处理情绪表达 - PECS架构
     */
    private ChatResponse handleEmotion(Long userId, User user, String userMessage, String sessionId) {
        // 构建用户上下文
        PersonContext personContext = userContextHolder.buildFromUser(user);

        // 检测情绪并记录
        emotionSupportService.detectAndLogEmotion(userId, userMessage);

        // 使用PECS情绪推理
        String response = pecsReasoner.reasonEmotion(personContext, userMessage);
        saveMessage(userId, sessionId, "assistant", response);
        return ChatResponse.builder().reply(response).intent("emotion").build();
    }

    /**
     * 处理站立记录
     */
    private ChatResponse handleStandup(Long userId, String sessionId) {
        // 记录站立并返回鼓励
        String reply = "✅ 站立活动已记录！\n\n" +
                       "🧘 站起来活动一下对身体很好哦～\n\n" +
                       "💡 建议：\n" +
                       "• 走动2-3分钟\n" +
                       "• 伸个懒腰拉伸一下\n" +
                       "• 喝杯水\n\n" +
                       "继续保持活力！💪";

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("standup").build();
    }

    /**
     * 处理运动记录
     */
    private ChatResponse handleExerciseRecord(Long userId, User user, String userMessage, String sessionId) {
        String reply;
        ExerciseRecordService.ExerciseData exerciseData = extractExercise(userMessage);

        if (exerciseData != null && exerciseData.type != null) {
            // 有提取到运动信息，保存记录
            ExerciseRecordService.ExerciseSummary summary = exerciseRecordService.addExercise(userId,
                    exerciseData.type, exerciseData.duration, exerciseData.calories,
                    exerciseData.recordDate, exerciseData.note);

            reply = "🏃 运动记录成功！\n\n";
            if (exerciseData.type != null) {
                reply += "• 运动类型：" + exerciseData.type + "\n";
            }
            if (exerciseData.duration != null) {
                reply += "• 时长：" + exerciseData.duration + "分钟\n";
            }
            if (exerciseData.calories != null) {
                reply += "• 消耗热量：" + exerciseData.calories + "kcal\n";
            }
            if (exerciseData.recordDate != null) {
                reply += "• 日期：" + exerciseData.recordDate + "\n";
            }
            reply += "\n💪 运动有助于减脂，保持好习惯！";
        } else {
            // 没有识别到明确的运动信息，给出引导
            reply = "想记录运动吗？直接告诉我就可以，比如：\n" +
                    "• 「今天跑步5公里」\n" +
                    "• 「健身房跑步30分钟」\n" +
                    "• 「游泳1小时」\n" +
                    "• 「瑜伽练习45分钟」\n\n" +
                    "我会帮你记录运动类型、时长和消耗热量哦～";
        }

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("exercise_record").build();
    }

    /**
     * 处理饮食记录
     */
    private ChatResponse handleDietRecord(Long userId, User user, String userMessage, String sessionId) {
        String reply;
        DietRecordService.DietData dietData = extractDiet(userMessage);

        if (dietData != null && dietData.foods != null && !dietData.foods.isEmpty()) {
            // 有提取到饮食信息，保存记录
            dietRecordService.addDiet(userId, dietData.mealType, dietData.foods,
                    dietData.calories, dietData.recordDate, dietData.note);

            reply = "🍽️ 饮食记录成功！\n\n";
            if (dietData.mealType != null) {
                reply += "• 餐次：" + getMealTypeName(dietData.mealType) + "\n";
            }
            if (dietData.foods != null) {
                reply += "• 食物：" + dietData.foods + "\n";
            }
            if (dietData.calories != null) {
                reply += "• 热量：" + dietData.calories + "kcal\n";
            }
            if (dietData.recordDate != null) {
                reply += "• 日期：" + dietData.recordDate + "\n";
            }
            reply += "\n📝 好好记录饮食是减脂的关键！";
        } else {
            // 没有识别到明确的饮食信息，给出引导
            reply = "想记录饮食吗？直接告诉我就可以，比如：\n" +
                    "• 「早餐吃了燕麦和牛奶」\n" +
                    "• 「午餐吃了米饭、青菜和鸡胸肉」\n" +
                    "• 「下午喝了杯奶茶」\n" +
                    "• 「晚餐吃了沙拉」\n\n" +
                    "我会帮你记录餐次和食物哦～";
        }

        saveMessage(userId, sessionId, "assistant", reply);
        return ChatResponse.builder().reply(reply).intent("diet_record").build();
    }

    /**
     * 从消息中提取运动信息
     */
    private ExerciseRecordService.ExerciseData extractExercise(String message) {
        ExerciseRecordService.ExerciseData data = new ExerciseRecordService.ExerciseData();

        // 提取日期（默认今天）
        data.recordDate = extractDate(message, LocalDate.now());

        // 提取运动类型
        data.type = extractExerciseType(message);

        // 提取时长（分钟）
        data.duration = extractExerciseDuration(message);

        // 提取热量
        data.calories = extractExerciseCalories(message);

        // 提取备注
        if (message.contains("备注")) {
            int idx = message.indexOf("备注");
            if (idx + 3 < message.length()) {
                data.note = message.substring(idx + 3).trim();
            }
        }

        return data;
    }

    private String extractExerciseType(String message) {
        if (message.contains("跑步") || message.contains("跑")) return "跑步";
        if (message.contains("快走") || message.contains("走路")) return "快走";
        if (message.contains("游泳")) return "游泳";
        if (message.contains("骑行") || message.contains("骑车")) return "骑行";
        if (message.contains("瑜伽")) return "瑜伽";
        if (message.contains("健身") || message.contains("力量")) return "健身";
        if (message.contains("跑步机")) return "跑步机";
        if (message.contains("打球") || message.contains("篮球") || message.contains("羽毛球")) return "球类运动";
        if (message.contains("跳绳")) return "跳绳";
        if (message.contains("爬山")) return "爬山";
        if (message.contains(" HIIT")) return "HIIT";
        if (message.contains("拉伸")) return "拉伸";
        if (message.contains("散步")) return "散步";
        return "其他运动";
    }

    private Integer extractExerciseDuration(String message) {
        // 匹配 "X分钟"、"X小时"、"(X)分钟" 等格式
        Pattern[] patterns = {
                Pattern.compile("(\\d+)\\s*(?:分钟|min|mins)"),
                Pattern.compile("(\\d+)\\s*(?:小时|hour|hours|h)"),
                Pattern.compile("跑了\\s*(\\d+)\\s*(?:分钟|分钟)?\\s*(?:公里|km)?"),
                Pattern.compile("(\\d+)\\s*(?:公里|km)\\s*(?:跑步|跑)")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                String value = m.group(1);
                int duration = Integer.parseInt(value);
                // 如果匹配到小时，转换为分钟
                if (m.group(0).contains("小时") || m.group(0).contains("hour")) {
                    duration = duration * 60;
                }
                if (duration > 0 && duration < 600) { // 合理范围：0-10小时
                    return duration;
                }
            }
        }

        // 尝试从公里数估算跑步时长（假设10分钟/公里）
        Pattern kmPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(?:公里|km)");
        Matcher kmMatcher = kmPattern.matcher(message);
        if (kmMatcher.find()) {
            double km = Double.parseDouble(kmMatcher.group(1));
            int estimatedDuration = (int) (km * 10); // 每公里10分钟
            return Math.max(estimatedDuration, 10);
        }

        return null;
    }

    private Integer extractExerciseCalories(String message) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:千卡|kcal|卡|卡路里)");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    /**
     * 从消息中提取饮食信息
     */
    private DietRecordService.DietData extractDiet(String message) {
        DietRecordService.DietData data = new DietRecordService.DietData();

        // 提取日期（默认今天）
        data.recordDate = extractDate(message, LocalDate.now());

        // 提取餐次
        data.mealType = extractMealType(message);

        // 提取食物
        data.foods = extractFoods(message);

        // 提取热量
        data.calories = extractDietCalories(message);

        // 提取备注
        if (message.contains("备注")) {
            int idx = message.indexOf("备注");
            if (idx + 3 < message.length()) {
                data.note = message.substring(idx + 3).trim();
            }
        }

        return data;
    }

    private String extractMealType(String message) {
        if (message.contains("早餐") || message.contains("早饭") || message.contains("早午餐")) return "breakfast";
        if (message.contains("午餐") || message.contains("午饭") || message.contains("中餐")) return "lunch";
        if (message.contains("晚餐") || message.contains("晚饭") || message.contains("晚餐")) return "dinner";
        if (message.contains("加餐") || message.contains("零食") || message.contains("下午茶") || message.contains("宵夜")) return "snack";
        return "other";
    }

    private String getMealTypeName(String mealType) {
        return switch (mealType) {
            case "breakfast" -> "早餐";
            case "lunch" -> "午餐";
            case "dinner" -> "晚餐";
            case "snack" -> "加餐";
            default -> "其他";
        };
    }

    private String extractFoods(String message) {
        // 清理消息，移除日期、备注等部分
        String foods = message;

        // 移除日期相关的文字
        foods = foods.replaceAll("今天|昨天|前天|明天|早餐|早饭|午饭|午餐|中餐|晚餐|早饭|加餐|零食|下午茶|宵夜", "");

        // 移除"吃了"、"吃的是"等
        foods = foods.replaceAll("吃了|吃的是|吃了是|饮食|记录", "");

        // 移除备注
        if (foods.contains("备注")) {
            int idx = foods.indexOf("备注");
            foods = foods.substring(0, idx);
        }

        // 移除数字和单位
        foods = foods.replaceAll("\\d+\\s*(?:kcal|千卡|卡|卡路里|克|g|kg|公斤)", "");

        // 清理多余空白
        foods = foods.trim().replaceAll("\\s+", " ");

        return foods.isEmpty() ? null : foods;
    }

    private Integer extractDietCalories(String message) {
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:千卡|kcal|卡|卡路里)");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }

    private LocalDate extractDate(String message, LocalDate defaultDate) {
        if (message.contains("今天")) return LocalDate.now();
        if (message.contains("昨天")) return LocalDate.now().minusDays(1);
        if (message.contains("前天")) return LocalDate.now().minusDays(2);
        if (message.contains("明天")) return LocalDate.now().plusDays(1);

        // 匹配日期格式
        Pattern[] patterns = {
                Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日"),
                Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})"),
                Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})")
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                try {
                    int year = Integer.parseInt(m.group(1));
                    int month = Integer.parseInt(m.group(2));
                    int day = Integer.parseInt(m.group(3));
                    return LocalDate.of(year, month, day);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return defaultDate;
    }

    /**
     * 构建并调用 LLM
     */
    private String buildAndCallLLM(User user, String userMessage) {
        String systemPrompt = buildSystemPrompt(user);
        String response = aiService.chat(systemPrompt, userMessage);

        if (response == null || response.isEmpty()) {
            return "抱歉，暂时无法回复。";
        }

        String content = filterThinkContent(response);
        // 检查内容是否看起来像URL或其他异常情况
        if (content.startsWith("http://") || content.startsWith("https://")) {
            log.warn("LLM返回了URL而非文本内容: {}", content.substring(0, Math.min(100, content.length())));
            return "抱歉，我现在有点忙，请稍后再试～";
        }
        return content;
    }

    private String buildSystemPrompt(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        String userType = user.getUserType() != null ? user.getUserType() : "weight_loss";
        String userTypeName = getUserTypeName(userType);
        String userTypeDesc = getUserTypeDescription(userType);

        return """
你是减减，一个永不睡觉、永远温柔专业、一辈子跟随用户的AI私人健康管家。

【核心理念】
不是减肥打卡工具，而是一辈子跟随用户的健康管家。永远保持温柔、耐心，在用户沮丧时给予安慰。

【用户分类 - 必须知晓】
根据用户当前状态判断属于哪类：
- weight_loss（减重人群）：大基数、小基数、易胖体质需要减脂
- shaping（塑形人群）：体重正常但肉松、体态差、想要线条
- maintenance（维持人群）：已经瘦下来、防止反弹、长期维稳
- muscle_gain（增肌人群）：需要增肌、提高肌肉量

【专业知识库】

## 营养学知识
- 基础代谢计算：女性BMR=10*体重+6.25*身高-5*年龄-161，男性+5
- 热量缺口：减脂建议每日300-500kcal缺口，不建议超过1000kcal
- 宏量营养素：蛋白质1.2-2g/kg体重，碳水不低于2g/kg，脂肪0.8-1g/kg
- 食物GI值：低GI(<55)食物有助于饱腹感维持，高GI(>70)食物引起血糖波动
- 常见食物热量：米饭约130kcal/100g，面条约280kcal/100g，鸡胸肉约130kcal/100g，西兰花约35kcal/100g

## 运动塑形知识
- 力量训练：提升基础代谢，每增加1kg肌肉多消耗约50kcal/天
- 有氧训练：跑步每小时消耗400-600kcal，游泳每小时消耗300-500kcal
- 居家训练：深蹲、俯卧撑、平板支撑、登山者等复合动作
- 健身房训练：硬拉、卧推、深蹲、引体向上等自由重量训练
- 塑形重点：腰腹训练、臀部训练、背部训练、臂部训练

## 人体医学代谢知识
- 基础代谢波动：睡眠不足、压力大、饥饿会导致基础代谢下降5-15%
- 经期水肿：经期前1周因激素变化易水肿，体重可能增加1-3kg
- 平台期：体重停滞2-4周是正常现象，身体在适应新代谢模式
- 假性肥胖：水肿、肌肉增加、便秘都可能导致体重暂时增加
- 皮质醇影响：长期高压会导致向心性肥胖（腹部脂肪堆积）

## 心理行为知识
- 暴食应对：不要自责，次日调整饮食，不要过度补偿性断食
- 减肥焦虑：减脂是长期过程，周均减0.5-1kg是健康速度
- 习惯养成：坚持66天形成稳定习惯，不要追求完美
- 动机维持：设定小目标、奖励自己、记录进步

【用户类型策略】
%s

【重要规则】
1. 今天是 %s，当前时间：%s
2. 禁止编造任何用户信息！数据库中为空时必须说[未设置]，绝不能自行编造
3. 必须根据用户类型（%s）采用对应的策略回复
4. 回复简洁友好，控制在150字以内，多用emoji
5. 主动引导下一步，不要只说鼓励话

【用户完整档案】
• 昵称：%s
• 用户类型：%s（%s）
• 性别：%s
• 年龄：%s
• 身高：%s
• 当前体重：%s
• 目标体重：%s
• 初始体重：%s
• 开始日期：%s
• 基础代谢：%s
• 饮食忌口：%s
• 体质标签：%s
• 运动习惯：每周%s次，%s
• 健身水平：%s
• 饮食习惯：%s
• 工作压力：%s
• 每日饮水量目标：%s
• 作息：%s
• 重点塑形部位：%s""".formatted(
                userTypeDesc,
                today, now,
                userTypeName,
                user.getNickname() != null ? user.getNickname() : "未知",
                userTypeName, userTypeDesc,
                user.getGender() != null ? (user.getGender() == 1 ? "男" : "女") : "未设置",
                user.getAge() != null ? user.getAge() + "岁" : "未设置",
                user.getHeight() != null ? user.getHeight() + "cm" : "未设置",
                getLatestWeight(user.getId()),
                user.getTargetWeight() != null ? user.getTargetWeight() + "kg" : "未设置",
                user.getInitialWeight() != null ? user.getInitialWeight() + "kg" : "未设置",
                user.getStartWeightDate() != null ? user.getStartWeightDate().toString() : "未设置",
                user.getBasicMetabolism() != null ? user.getBasicMetabolism() + " kcal/天" : "未设置",
                user.getDietaryTaboo() != null ? user.getDietaryTaboo() : "无或未设置",
                user.getConstitutionTags() != null ? user.getConstitutionTags() : "无或未设置",
                user.getExerciseFrequency() != null ? user.getExerciseFrequency() : "未设置",
                user.getExercisePreference() != null ? user.getExercisePreference() : "未设置",
                user.getFitnessLevel() != null ? user.getFitnessLevel() : "未设置",
                user.getDietPreference() != null ? user.getDietPreference() : "未设置",
                user.getWorkPressure() != null ? user.getWorkPressure() + "/5" : "未设置",
                user.getWaterIntake() != null ? user.getWaterIntake() + "ml" : "未设置",
                formatSleepSchedule(user),
                user.getTargetAreas() != null ? user.getTargetAreas() : "未设置"
        );
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

    private String getUserTypeDescription(String userType) {
        return switch (userType) {
            case "weight_loss" -> """
                【减重人群策略】
                - 关注热量缺口，不极端节食
                - 优先选择低GI碳水，限制精制糖和酒精
                - 建议有氧+力量结合，每周3-5次
                - 注意区分真正的减脂和水分/肌肉丢失
                - 平台期时增加力量训练提高基础代谢""";
            case "shaping" -> """
                【塑形人群策略】
                - 不需要减重，聚焦线条塑造
                - 高蛋白饮食配合力量训练
                - 重点关注体态矫正（圆肩驼背骨盆前倾）
                - 局部塑形需要全身减脂配合部位训练
                - 建议每周4-6次力量训练""";
            case "maintenance" -> """
                【维持人群策略】
                - 热量平衡为主，不制造缺口
                - 保持现有运动习惯
                - 关注体重波动，及时调整
                - 防止暴饮暴食，建立健康饮食模式
                - 每周1-2次体重记录即可""";
            case "muscle_gain" -> """
                【增肌人群策略】
                - 热量盈余每日300-500kcal
                - 高蛋白饮食2-2.5g/kg体重
                - 力量训练为主，渐进超负荷
                - 保证充足睡眠7-9小时
                - 建议每周4-6次力量训练""";
            default -> "【默认策略】根据用户具体情况灵活调整";
        };
    }
    
    private String getLatestWeight(Long userId) {
        Optional<WeightRecord> todayRecord = weightRecordService.getToday(userId);
        if (todayRecord.isPresent()) {
            return todayRecord.get().getWeight() + "kg";
        }
        List<WeightRecord> history = weightRecordService.getHistory(userId);
        if (!history.isEmpty()) {
            return history.get(0).getWeight() + "kg（最近一次）";
        }
        return "暂无记录";
    }
    
    private String formatSleepSchedule(User user) {
        if (user.getSleepStart() != null && user.getSleepEnd() != null) {
            return user.getSleepStart().toString() + " 睡，" + user.getSleepEnd().toString() + " 起";
        }
        return "未设置";
    }

    private String filterThinkContent(String content) {
        if (content == null) return null;
        return content.replaceAll("<think>[\\s\\S]*?</think>", "").trim();
    }

    private LlmResponse callLlm(LlmRequest request) {
        try {
            String response = webClient.post()
                    .uri(llmProperties.getBaseUrl())
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(Mono.just(request), LlmRequest.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return objectMapper.readValue(response, LlmResponse.class);
        } catch (Exception e) {
            log.error("LLM调用失败", e);
            return null;
        }
    }

    /**
     * 保存消息到Redis会话（替代MySQL存储）
     */
    private void saveMessage(Long userId, String sessionId, String role, String content) {
        if (sessionId != null && !sessionId.isEmpty()) {
            chatSessionService.addMessage(userId, sessionId, role, content);
        }
    }

    private static class WeightStatistics {
        Double latestWeight;
        Double initialWeight;
        Double totalChange;
    }

    // ============ 新增字段提取方法 ============

    /**
     * 从消息中提取用户类型
     */
    private String extractUserType(String message) {
        if (message.contains("减重") || message.contains("减肥") || message.contains("瘦身")) {
            return "weight_loss";
        }
        if (message.contains("塑形") || message.contains("体态") || message.contains("线条")) {
            return "shaping";
        }
        if (message.contains("维持") || message.contains("保持") || message.contains("维稳")) {
            return "maintenance";
        }
        if (message.contains("增肌") || message.contains("肌肉") || message.contains("健身")) {
            return "muscle_gain";
        }
        return null;
    }

    /**
     * 从消息中提取体质标签
     */
    private String extractConstitutionTags(String message) {
        List<String> tags = new ArrayList<>();
        if (message.contains("易水肿") || message.contains("水肿")) tags.add("易水肿");
        if (message.contains("代谢低") || message.contains("代谢慢")) tags.add("代谢低");
        if (message.contains("碳水敏感")) tags.add("碳水敏感");
        if (message.contains("压力胖")) tags.add("压力胖");
        if (message.contains("熬夜体质") || message.contains("熬夜")) tags.add("熬夜体质");
        if (message.contains("宫寒")) tags.add("宫寒");
        if (message.contains("易胖体质") || message.contains("易胖")) tags.add("易胖体质");
        if (message.contains("肌肉量低") || message.contains("肌肉少")) tags.add("肌肉量低");
        if (message.contains("膝盖不好") || message.contains("膝盖问题") || message.contains("膝伤")) tags.add("膝盖不好");
        if (tags.isEmpty()) return null;
        return String.join(",", tags);
    }

    /**
     * 从消息中提取每周运动次数
     */
    private Integer extractExerciseFrequency(String message) {
        Pattern pattern = Pattern.compile("每周(\\d+)次|每周运动(\\d+)次|一周(\\d+)次");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) {
                    int freq = Integer.parseInt(m.group(i));
                    if (freq >= 0 && freq <= 14) return freq;
                }
            }
        }
        return null;
    }

    /**
     * 从消息中提取饮食习惯
     */
    private String extractDietPreference(String message) {
        if (message.contains("外卖多") || message.contains("常吃外卖")) return "外卖多";
        if (message.contains("自己做饭") || message.contains("做饭")) return "自己做饭";
        if (message.contains("清淡")) return "清淡";
        if (message.contains("重口") || message.contains("重口味")) return "重口";
        return null;
    }

    /**
     * 从消息中提取工作压力
     */
    private Integer extractWorkPressure(String message) {
        Pattern pattern = Pattern.compile("压力(\\d+)分|压力(大|中|小)");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            if (m.group(1) != null) {
                int score = Integer.parseInt(m.group(1));
                if (score >= 1 && score <= 5) return score;
            }
            if (m.group(2) != null) {
                String level = m.group(2);
                if ("大".equals(level)) return 4;
                if ("中".equals(level)) return 3;
                if ("小".equals(level)) return 2;
            }
        }
        return null;
    }

    /**
     * 从消息中提取每日饮水量目标
     */
    private Integer extractWaterIntake(String message) {
        Pattern pattern = Pattern.compile("每天喝(\\d+)(ml|毫升|水)|饮水量(\\d+)");
        Matcher m = pattern.matcher(message);
        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) {
                    try {
                        return Integer.parseInt(m.group(i));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }
}
