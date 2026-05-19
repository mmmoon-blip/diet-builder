package com.dietbutler.service;

import com.dietbutler.entity.User;
import com.dietbutler.repository.UserRepository;
import com.dietbutler.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WebClient webClient;

    @Value("${wechat.appid}")
    private String wechatAppid;

    @Value("${wechat.secret}")
    private String wechatSecret;

    /**
     * 微信登录 - 无状态JWT
     */
    public Map<String, Object> wechatLogin(String code, String nickname, String avatarUrl) {
        // 1. 用code换取openid
        String openid = getOpenidFromCode(code);
        if (openid == null) {
            return Map.of("success", false, "message", "code无效或已过期");
        }

        // 2. 查找或创建用户
        Optional<User> existingUser = userRepository.findByOpenid(openid);
        User user;
        String token;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            if (nickname != null) {
                if ("减减用户".equals(user.getNickname()) || "游客".equals(user.getNickname())) {
                    user.setNickname(nickname);
                }
            }
            if (avatarUrl != null && (user.getAvatar() == null || user.getAvatar().isEmpty())) {
                user.setAvatar(avatarUrl);
            }
            user = userRepository.save(user);
        } else {
            // 新用户首次登录
            user = new User();
            user.setOpenid(openid);
            user.setNickname(nickname != null ? nickname : "减减用户");
            user.setAvatar(avatarUrl);
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
            isNewUser = true;
        }

        // 3. 生成无状态JWT（不存储在数据库）
        token = jwtUtil.generateToken(user.getId(), openid);

        // 4. 返回结果（不返回expireAt，因为是无状态JWT）
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("token", token);
        result.put("user", user);
        result.put("isNewUser", isNewUser);  // 新增：标识是否首次登录

        return result;
    }

    /**
     * 验证Token - 无状态JWT，直接解析验证
     */
    public Map<String, Object> validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return Map.of("success", false, "message", "token不能为空");
        }

        // 去掉Bearer前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            return Map.of("success", false, "message", "token已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Map.of("success", false, "message", "无效token");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }

        User user = userOpt.get();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user", user);
        result.put("isGuest", jwtUtil.isGuestFromToken(token));

        return result;
    }

    /**
     * 获取当前用户
     */
    public Optional<User> getCurrentUser(String token) {
        Map<String, Object> result = validateToken(token);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Optional.of((User) result.get("user"));
        }
        return Optional.empty();
    }

    /**
     * 从token获取用户ID
     */
    public Optional<Long> getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return Optional.empty();
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.validateToken(token)) {
            return Optional.empty();
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Optional.ofNullable(userId);
    }

    /**
     * 用code换取openid（内部方法）
     */
    private String getOpenidFromCode(String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?" +
                    "appid=" + wechatAppid +
                    "&secret=" + wechatSecret +
                    "&js_code=" + code +
                    "&grant_type=authorization_code";

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("WeChat API response: {}", response);

            // 解析返回的JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> respMap = mapper.readValue(response, Map.class);

            if (respMap.containsKey("openid")) {
                return (String) respMap.get("openid");
            } else {
                log.error("Failed to get openid: {}", respMap.get("errmsg"));
                return null;
            }
        } catch (Exception e) {
            log.error("Error getting openid from code", e);
            return null;
        }
    }
}