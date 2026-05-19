package com.dietbutler.service;

import com.dietbutler.dto.AddWeightRequest;
import com.dietbutler.dto.UpdateUserRequest;
import com.dietbutler.entity.WeightRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ToolService {

    private final WeightRecordService weightRecordService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public Function<Map<String, Object>, String> getToolHandler(String toolName) {
        return switch (toolName) {
            case "add_weight" -> this::addWeight;
            case "update_user" -> this::updateUser;
            case "get_weight_history" -> this::getWeightHistory;
            case "get_weight_statistics" -> this::getWeightStatistics;
            default -> args -> "未知工具: " + toolName;
        };
    }

    private String addWeight(Map<String, Object> args) {
        try {
            Long userId = Long.valueOf(args.get("user_id").toString());
            Double weight = Double.valueOf(args.get("weight").toString());
            String note = args.get("note") != null ? args.get("note").toString() : null;

            WeightRecord record = weightRecordService.addWeight(userId, weight, note, null, null, null);
            return "体重记录成功！记录ID: " + record.getId() + "，体重: " + record.getWeight() + "kg";
        } catch (Exception e) {
            log.error("添加体重记录失败", e);
            return "添加体重记录失败: " + e.getMessage();
        }
    }

    private String updateUser(Map<String, Object> args) {
        try {
            Long userId = Long.valueOf(args.get("user_id").toString());
            UpdateUserRequest request = new UpdateUserRequest();

            if (args.get("nickname") != null) request.setNickname(args.get("nickname").toString());
            if (args.get("height") != null) request.setHeight(Double.valueOf(args.get("height").toString()));
            if (args.get("target_weight") != null) request.setTargetWeight(Double.valueOf(args.get("target_weight").toString()));
            if (args.get("age") != null) request.setAge(Integer.valueOf(args.get("age").toString()));
            if (args.get("gender") != null) request.setGender(Integer.valueOf(args.get("gender").toString()));

            var user = userService.getUserById(userId);
            if (user == null) return "用户不存在";

            if (request.getNickname() != null) user.setNickname(request.getNickname());
            if (request.getHeight() != null) user.setHeight(request.getHeight());
            if (request.getTargetWeight() != null) user.setTargetWeight(request.getTargetWeight());
            if (request.getAge() != null) user.setAge(request.getAge());
            if (request.getGender() != null) user.setGender(request.getGender());

            userService.updateUser(user);
            return "用户信息更新成功！";
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return "更新用户信息失败: " + e.getMessage();
        }
    }

    private String getWeightHistory(Map<String, Object> args) {
        try {
            Long userId = Long.valueOf(args.get("user_id").toString());
            var records = weightRecordService.getHistory(userId);
            if (records.isEmpty()) return "暂无体重记录";

            StringBuilder sb = new StringBuilder("体重记录历史：\n");
            records.forEach(r -> sb.append("- ")
                    .append(r.getRecordDate()).append(": ")
                    .append(r.getWeight()).append("kg")
                    .append(r.getNote() != null ? " (" + r.getNote() + ")" : "")
                    .append("\n"));
            return sb.toString();
        } catch (Exception e) {
            log.error("获取体重历史失败", e);
            return "获取体重历史失败: " + e.getMessage();
        }
    }

    private String getWeightStatistics(Map<String, Object> args) {
        try {
            Long userId = Long.valueOf(args.get("user_id").toString());
            var stats = weightRecordService.getStatistics(userId);

            StringBuilder sb = new StringBuilder("体重统计：\n");
            sb.append("- 最新体重: ").append(stats.getLatestWeight()).append("kg\n");
            sb.append("- 初始体重: ").append(stats.getStartWeight()).append("kg\n");
            sb.append("- 累计变化: ").append(String.format("%.1f", stats.getChange())).append("kg\n");
            sb.append("- 记录次数: ").append(stats.getRecordCount()).append("次");
            return sb.toString();
        } catch (Exception e) {
            log.error("获取体重统计失败", e);
            return "获取体重统计失败: " + e.getMessage();
        }
    }
}