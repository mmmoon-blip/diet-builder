package com.dietbutler.service;

import com.dietbutler.entity.EmotionLog;
import com.dietbutler.entity.User;
import com.dietbutler.repository.EmotionLogRepository;
import com.dietbutler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 情绪安抚服务
 * 识别用户负面情绪，主动干预和安抚
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionSupportService {

    private final EmotionLogRepository emotionLogRepository;
    private final UserRepository userRepository;

    // 负面情绪关键词
    private static final Pattern[] NEGATIVE_PATTERNS = {
        Pattern.compile("暴食|暴饮暴食|吃多了|忍不住吃"),
        Pattern.compile("焦虑|着急|心慌|烦躁"),
        Pattern.compile("不掉秤|没效果|减不下去"),
        Pattern.compile("不想减了|想放弃|坚持不下去"),
        Pattern.compile("沮丧|难过|伤心|失落"),
        Pattern.compile("平台期|卡住了|瓶颈")
    };

    private static final String[] NEGATIVE_EMOTIONS = {
        "binged", "anxious", "frustrated", "overeat"
    };

    /**
     * 检测消息中的负面情绪
     */
    public EmotionLog detectAndLogEmotion(Long userId, String message) {
        String emotion = detectEmotion(message);
        if (emotion == null) return null;

        // 记录情绪日志
        EmotionLog log = new EmotionLog();
        log.setUserId(userId);
        log.setRecordDate(LocalDate.now());
        log.setEmotion(emotion);
        log.setLevel(estimateEmotionLevel(message));
        log.setEmotionTrigger(extractTrigger(message));
        log.setAiIntervened(false);
        log.setCreatedAt(LocalDateTime.now());

        emotionLogRepository.save(log);
        return log;
    }

    /**
     * 获取AI情绪干预回复
     */
    public String getAIEmotionResponse(Long userId, EmotionLog emotionLog) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        String response = buildEmotionResponse(user, emotionLog);

        // 更新日志，记录AI干预
        emotionLog.setAiIntervened(true);
        emotionLog.setAiResponse(response);
        emotionLogRepository.save(emotionLog);

        return response;
    }

    /**
     * 根据情绪类型构建安抚话术
     */
    private String buildEmotionResponse(User user, EmotionLog emotionLog) {
        String emotion = emotionLog.getEmotion();
        String nickname = user.getNickname() != null ? user.getNickname() : "";

        switch (emotion) {
            case "binged":
                return buildBingeResponse(nickname);
            case "anxious":
                return buildAnxietyResponse(nickname);
            case "frustrated":
                return buildFrustrationResponse(nickname, user);
            case "overeat":
                return buildOvereatResponse(nickname);
            default:
                return buildDefaultComfort(nickname);
        }
    }

    private String buildBingeResponse(String nickname) {
        return String.format("""
            %s，先抱抱你～ 🤗

            偶尔吃多了真的很正常，不要责怪自己！

            重要的是：
            • 明天正常吃就好，不要断食报复
            • 多喝水，帮助代谢
            • 可以适当增加一点运动

            体重波动主要是水分，不是脂肪。2-3天就会恢复的～

            你已经很棒了，偶尔的放纵不代表失败。加油！💪
            """, nickname);
    }

    private String buildAnxietyResponse(String nickname) {
        return String.format("""
            %s，感受到你的焦虑了 🤗

            减肥期间焦虑很常见，但请相信：
            • 体重波动是正常的，不是努力没效果
            • 减脂是长期过程，不会每天都掉秤
            • 偶尔的平台期是为了更好的突破

            建议：
            • 放下体重秤，去关注围度变化
            • 做些放松的事情，缓解压力
            • 和我聊聊，我一直在～

            你不是一个人在战斗！🌸
            """, nickname);
    }

    private String buildFrustrationResponse(String nickname, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s，我能理解你的沮丧... 😔\n\n", nickname));

        // 根据用户情况给出具体建议
        if (user.getConstitutionTags() != null && user.getConstitutionTags().contains("代谢低")) {
            sb.append("你是代谢偏低的体质，减重会比别人慢一些，这是正常的。\n\n");
        }

        sb.append("给你几个建议：\n");
        sb.append("• 不要只看体重，看整体围度和照片对比\n");
        sb.append("• 增加力量训练，提高基础代谢\n");
        sb.append("• 保证睡眠，睡眠不足会影响代谢\n");

        if (user.getWorkPressure() != null && user.getWorkPressure() >= 4) {
            sb.append("• 工作压力大时，尝试冥想或深呼吸放松\n");
        }

        sb.append("\n你已经坚持到现在了，这本身就很了不起！💪\n");
        sb.append("平台期一般2-4周就会过去，坚持就是胜利！");

        return sb.toString();
    }

    private String buildOvereatResponse(String nickname) {
        return String.format("""
            %s，吃多了就吃多了呗，没关系的～ 🍜

            身体需要能量，吃多了说明它需要！

            这样做：
            • 不要有罪恶感，正常对待
            • 下一餐少吃或清淡一些
            • 增加喝水量
            • 明天多走动

            一天的饮食不会决定成败，长期的习惯才是关键。
            偶尔的放纵是为了更好的坚持！🌟
            """, nickname);
    }

    private String buildDefaultComfort(String nickname) {
        return String.format("""
            %s，我在这里～ 🤗

            减肥路上难免有低谷，但请记住：
            • 你已经很努力了
            • 偶尔的挫折不代表失败
            • 坚持下去就会有收获

            如果想聊聊，我随时都在。
            我们一起加油！💪🌸
            """, nickname);
    }

    /**
     * 检测消息中的情绪
     */
    private String detectEmotion(String message) {
        for (int i = 0; i < NEGATIVE_PATTERNS.length; i++) {
            if (NEGATIVE_PATTERNS[i].matcher(message).find()) {
                return NEGATIVE_EMOTIONS[i];
            }
        }
        return null;
    }

    /**
     * 估算情绪强度
     */
    private int estimateEmotionLevel(String message) {
        // 简单估算，根据感叹号和情绪词数量
        int level = 1;
        if (message.contains("！") || message.contains("!")) level = Math.min(5, level + 1);
        if (message.contains("太") || message.contains("真的")) level = Math.min(5, level + 1);
        if (message.contains("完全") || message.contains("彻底")) level = Math.min(5, level + 2);
        return level;
    }

    /**
     * 提取触发原因
     */
    private String extractTrigger(String message) {
        // 简化版，实际可以用更复杂的NLP
        if (message.contains("压力") || message.contains("工作")) return "工作压力";
        if (message.contains("经期") || message.contains("姨妈")) return "经期";
        if (message.contains("聚会") || message.contains("外卖")) return "饮食失控";
        if (message.contains("熬夜") || message.contains("失眠")) return "睡眠问题";
        return "未知";
    }

    /**
     * 检查是否有需要干预的负面情绪
     */
    public void checkAndInterveneNegativeEmotions() {
        // 检查过去1小时内是否有负面情绪但未被AI干预
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<EmotionLog> unhandledEmotions = emotionLogRepository
                .findByUserIdAndAiIntervenedFalseAndCreatedAtAfter(null, oneHourAgo);

        for (EmotionLog emotion : unhandledEmotions) {
            if (shouldIntervene(emotion)) {
                String response = getAIEmotionResponse(emotion.getUserId(), emotion);
                if (response != null) {
                    log.info("AI情绪干预已发送: userId={}, emotion={}", emotion.getUserId(), emotion.getEmotion());
                }
            }
        }
    }

    /**
     * 判断是否需要干预
     */
    private boolean shouldIntervene(EmotionLog emotion) {
        // 高强度负面情绪需要干预
        return emotion.getLevel() != null && emotion.getLevel() >= 3;
    }

    /**
     * 获取用户情绪历史
     */
    public List<EmotionLog> getUserEmotionHistory(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return emotionLogRepository.findByUserIdAndRecordDateBetweenOrderByCreatedAtDesc(userId, start, end);
    }
}