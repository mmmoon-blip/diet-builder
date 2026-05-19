package com.dietbutler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI对话Session管理 - 基于Redis
 *
 * 功能说明：
 * - 使用Redis存储对话上下文，替代MySQL存储chat_message表
 * - 采用滑动窗口机制，控制对话长度在20条以内
 * - 24小时无互动自动过期
 *
 * 数据结构：
 * - Key: chat:session:{userId}:{sessionId}
 * - Value: JSON字符串，包含消息列表和元数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Primary
@ConditionalOnBean(StringRedisTemplate.class)
public class ChatSessionService implements ChatSessionManager {

    private final StringRedisTemplate redisTemplate;

    // Redis key前缀
    private static final String SESSION_KEY_PREFIX = "chat:session:";

    // 默认会话有效期（24小时）
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    // 最大消息数量（超过后滑动窗口删除旧消息）
    private static final int MAX_MESSAGES = 20;

    // 滑动窗口删除数量
    private static final int SLIDING_WINDOW_REMOVE = 10;

    /**
     * 创建新会话
     */
    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String key = buildKey(userId, sessionId);

        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setMessages(new ArrayList<>());
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());

        redisTemplate.opsForValue().set(key, session.toJson(), DEFAULT_TTL);
        log.info("创建新会话: userId={}, sessionId={}", userId, sessionId);

        return sessionId;
    }

    /**
     * 获取或创建会话
     */
    public String getOrCreateSession(Long userId, String existingSessionId) {
        if (existingSessionId != null && !existingSessionId.isEmpty()) {
            String key = buildKey(userId, existingSessionId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                // 更新最后活跃时间
                ChatSession session = getSession(userId, existingSessionId);
                if (session != null) {
                    session.setLastActiveAt(LocalDateTime.now());
                    redisTemplate.opsForValue().set(key, session.toJson(), DEFAULT_TTL);
                    return existingSessionId;
                }
            }
        }
        return createSession(userId);
    }

    /**
     * 获取会话
     */
    public ChatSession getSession(Long userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return ChatSession.fromJson(json);
    }

    /**
     * 获取会话的消息列表（用于LLM上下文）
     */
    public List<ChatMessage> getMessages(Long userId, String sessionId) {
        ChatSession session = getSession(userId, sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return session.getMessages();
    }

    /**
     * 添加消息到会话
     */
    public void addMessage(Long userId, String sessionId, String role, String content) {
        String key = buildKey(userId, sessionId);
        ChatSession session = getSession(userId, sessionId);

        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setMessages(new ArrayList<>());
            session.setCreatedAt(LocalDateTime.now());
        }

        // 添加消息
        ChatMessage msg = new ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        session.getMessages().add(msg);

        // 滑动窗口：如果消息超过MAX_MESSAGES，移除最早的SLIDING_WINDOW_REMOVE条
        if (session.getMessages().size() > MAX_MESSAGES) {
            List<ChatMessage> keptMessages = session.getMessages().subList(
                    SLIDING_WINDOW_REMOVE,
                    session.getMessages().size()
            );
            session.setMessages(new ArrayList<>(keptMessages));
            log.info("会话消息超过{}条，滑动窗口移除{}条", MAX_MESSAGES, SLIDING_WINDOW_REMOVE);
        }

        session.setLastActiveAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(key, session.toJson(), DEFAULT_TTL);
    }

    /**
     * 清空会话消息
     */
    public void clearSession(Long userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        redisTemplate.delete(key);
        log.info("清空会话: userId={}, sessionId={}", userId, sessionId);
    }

    /**
     * 删除用户的所有会话
     */
    public void deleteUserSessions(Long userId) {
        String pattern = SESSION_KEY_PREFIX + userId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("删除用户所有会话: userId={}, count={}", userId, keys.size());
        }
    }

    private String buildKey(Long userId, String sessionId) {
        return SESSION_KEY_PREFIX + userId + ":" + sessionId;
    }

    // ==================== 内部类 ====================

    @lombok.Data
    public static class ChatSession {
        private String sessionId;
        private Long userId;
        private List<ChatMessage> messages;
        private LocalDateTime createdAt;
        private LocalDateTime lastActiveAt;

        public String toJson() {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                        .writeValueAsString(this);
            } catch (Exception e) {
                log.error("序列化ChatSession失败", e);
                return "{}";
            }
        }

        public static ChatSession fromJson(String json) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                        .readValue(json, ChatSession.class);
            } catch (Exception e) {
                log.error("反序列化ChatSession失败", e);
                return null;
            }
        }
    }

    @lombok.Data
    public static class ChatMessage {
        private String role;        // user / assistant
        private String content;     // 消息内容
        private LocalDateTime createdAt;  // 创建时间
    }
}