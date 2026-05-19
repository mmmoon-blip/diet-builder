package com.dietbutler.service;

import com.dietbutler.config.WechatProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信模板消息推送服务
 * 用于AI主动发送晨间问候、运动提醒、饮水提醒等到用户
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatTemplateMessageService {

    private final WebClient webClient;
    private final WechatProperties wechatProperties;
    private final ObjectMapper objectMapper;

    /**
     * 发送模板消息
     */
    public boolean sendTemplateMessage(String openid, String templateId, Map<String, Object> data) {
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + getAccessToken();

            Map<String, Object> request = new HashMap<>();
            request.put("touser", openid);
            request.put("template_id", templateId);
            request.put("data", data);

            String response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(request), Map.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            int errcode = (int) result.getOrDefault("errcode", -1);
            return errcode == 0;
        } catch (Exception e) {
            log.error("发送模板消息失败", e);
            return false;
        }
    }

    /**
     * 获取AccessToken
     */
    private String getAccessToken() {
        try {
            String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    wechatProperties.getAppid(), wechatProperties.getSecret());

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Map<String, Object> result = objectMapper.readValue(response, Map.class);
            return (String) result.get("access_token");
        } catch (Exception e) {
            log.error("获取AccessToken失败", e);
            return "";
        }
    }

    // TODO: 模板消息ID暂未配置，需要在微信公众平台申请后替换
    // 模板消息必须在用户产生交互后的48小时内发送
    // 或考虑使用订阅消息 / 客服消息接口
    private static final String TEMPLATE_ID_MORNING = "YOUR_TEMPLATE_ID_MORNING"; // 晨间问候
    private static final String TEMPLATE_ID_WATER = "YOUR_TEMPLATE_ID_WATER"; // 饮水提醒
    private static final String TEMPLATE_ID_EXERCISE = "YOUR_TEMPLATE_ID_EXERCISE"; // 运动提醒
    private static final String TEMPLATE_ID_SUMMARY = "YOUR_TEMPLATE_ID_SUMMARY"; // 晚间复盘
    private static final String TEMPLATE_ID_MILESTONE = "YOUR_TEMPLATE_ID_MILESTONE"; // 里程碑提醒
    private static final String TEMPLATE_ID_WEEKLY = "YOUR_TEMPLATE_ID_WEEKLY"; // 周报
    private static final String TEMPLATE_ID_MONTHLY = "YOUR_TEMPLATE_ID_MONTHLY"; // 月报
    private static final String TEMPLATE_ID_SEDENTARY = "YOUR_TEMPLATE_ID_SEDENTARY"; // 久坐提醒
    private static final String TEMPLATE_ID_EMOTION = "YOUR_TEMPLATE_ID_EMOTION"; // 情绪安抚

    /**
     * 发送晨间问候（早8点）
     */
    public boolean sendMorningGreeting(String openid, String nickname, Double todayWeight, Double changeFromStart) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "早安呀～" + nickname + "！🌅"));
        data.put("keyword1", Map.of("value", "减减"));
        data.put("keyword2", Map.of("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        data.put("remark", Map.of("value", todayWeight != null
                ? "今日体重：" + todayWeight + "kg，累计变化：" + formatChange(changeFromStart)
                : "记得今早称重哦～"));

        return sendTemplateMessage(openid, TEMPLATE_ID_MORNING, data);
    }

    /**
     * 发送饮水提醒
     */
    public boolean sendWaterReminder(String openid, int glasses, int goal) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "喝水提醒 💧"));
        data.put("keyword1", Map.of("value", glasses + "杯"));
        data.put("keyword2", Map.of("value", goal + "杯（每日目标）"));
        data.put("remark", Map.of("value", "保持水分有助于代谢哦～"));

        return sendTemplateMessage(openid, TEMPLATE_ID_WATER, data);
    }

    /**
     * 发送运动提醒
     */
    public boolean sendExerciseReminder(String openid, String exerciseType, String duration) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "运动提醒 🏃"));
        data.put("keyword1", Map.of("value", exerciseType));
        data.put("keyword2", Map.of("value", duration));
        data.put("remark", Map.of("value", "今天还没运动哦，趁着状态好多消耗点热量吧！"));

        return sendTemplateMessage(openid, TEMPLATE_ID_EXERCISE, data);
    }

    /**
     * 发送晚间复盘
     */
    public boolean sendEveningSummary(String openid, Double weight, String summary) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "今日复盘 🌙"));
        data.put("keyword1", Map.of("value", weight != null ? weight + "kg" : "未记录"));
        data.put("keyword2", Map.of("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        data.put("remark", Map.of("value", summary != null ? summary : "明天继续加油！"));

        return sendTemplateMessage(openid, TEMPLATE_ID_SUMMARY, data);
    }

    /**
     * 发送体重里程碑提醒
     */
    public boolean sendMilestoneReminder(String openid, Double achievedWeight, Double targetWeight) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "🎉 恭喜达成里程碑！"));
        data.put("keyword1", Map.of("value", achievedWeight + "kg"));
        data.put("keyword2", Map.of("value", targetWeight + "kg"));
        data.put("remark", Map.of("value", "你真的太棒了！坚持就是胜利～"));

        return sendTemplateMessage(openid, TEMPLATE_ID_MILESTONE, data);
    }

    /**
     * 发送周报
     */
    public boolean sendWeeklyReport(String openid, String reportContent) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "📊 您的健康周报来了"));
        data.put("keyword1", Map.of("value", "上周健康总结"));
        data.put("keyword2", Map.of("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        data.put("remark", Map.of("value", reportContent));

        return sendTemplateMessage(openid, TEMPLATE_ID_WEEKLY, data);
    }

    /**
     * 发送月报
     */
    public boolean sendMonthlyReport(String openid, String reportContent) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "📊 您的健康月报来了"));
        data.put("keyword1", Map.of("value", "本月健康总结"));
        data.put("keyword2", Map.of("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        data.put("remark", Map.of("value", reportContent));

        return sendTemplateMessage(openid, TEMPLATE_ID_MONTHLY, data);
    }

    /**
     * 发送久坐提醒
     */
    public boolean sendSedentaryReminder(String openid, int standCount, int sedentaryMinutes) {
        Map<String, Object> data = new HashMap<>();
        data.put("first", Map.of("value", "🧘 站立提醒"));
        data.put("keyword1", Map.of("value", sedentaryMinutes + "分钟"));
        data.put("keyword2", Map.of("value", standCount + "次"));
        data.put("remark", Map.of("value", "您已经久坐过久了，站起来活动一下吧！"));

        return sendTemplateMessage(openid, TEMPLATE_ID_SEDENTARY, data);
    }

    /**
     * 发送情绪安抚消息
     */
    public boolean sendEmotionComfort(String openid, String emotionType, String comfortMessage) {
        Map<String, Object> data = new HashMap<>();
        String title = switch (emotionType) {
            case "binged" -> "暴食安抚 💝";
            case "anxious" -> "焦虑安抚 💝";
            case "frustrated" -> "沮丧安慰 💝";
            default -> "我在这里 💝";
        };
        data.put("first", Map.of("value", title));
        data.put("keyword1", Map.of("value", "减减AI"));
        data.put("keyword2", Map.of("value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))));
        data.put("remark", Map.of("value", comfortMessage));

        return sendTemplateMessage(openid, TEMPLATE_ID_EMOTION, data);
    }

    private String formatChange(Double change) {
        if (change == null) return "暂无数据";
        return (change >= 0 ? "+" : "") + String.format("%.1f", change) + "kg";
    }
}
