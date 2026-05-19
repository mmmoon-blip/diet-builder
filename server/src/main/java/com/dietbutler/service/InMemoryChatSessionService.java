package com.dietbutler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI对话Session管理 - 内存版本（Redis不可用时的备选）
 */
@Slf4j
@Service
@ConditionalOnMissingBean(name = "chatSessionManager")
public class InMemoryChatSessionService implements ChatSessionManager {

    private final Map<String, ChatSession> store = new ConcurrentHashMap<>();

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final int MAX_MESSAGES = 20;
    private static final int SLIDING_WINDOW_REMOVE = 10;

    public String createSession(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String key = buildKey(userId, sessionId);

        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setMessages(new ArrayList<>());
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());

        store.put(key, session);
        log.info("[内存] 创建新会话: userId={}, sessionId={}", userId, sessionId);
        return sessionId;
    }

    public String getOrCreateSession(Long userId, String existingSessionId) {
        if (existingSessionId != null && !existingSessionId.isEmpty()) {
            String key = buildKey(userId, existingSessionId);
            if (store.containsKey(key)) {
                ChatSession session = store.get(key);
                session.setLastActiveAt(LocalDateTime.now());
                return existingSessionId;
            }
        }
        return createSession(userId);
    }

    public ChatSession getSession(Long userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        return store.get(key);
    }

    public List<?> getMessages(Long userId, String sessionId) {
        ChatSession session = getSession(userId, sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return session.getMessages();
    }

    public void addMessage(Long userId, String sessionId, String role, String content) {
        String key = buildKey(userId, sessionId);
        ChatSession session = store.get(key);

        if (session == null) {
            session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setMessages(new ArrayList<>());
            session.setCreatedAt(LocalDateTime.now());
        }

        ChatMessage msg = new ChatMessage();
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        session.getMessages().add(msg);

        if (session.getMessages().size() > MAX_MESSAGES) {
            List<ChatMessage> keptMessages = session.getMessages().subList(
                    SLIDING_WINDOW_REMOVE,
                    session.getMessages().size()
            );
            session.setMessages(new ArrayList<>(keptMessages));
        }

        session.setLastActiveAt(LocalDateTime.now());
        store.put(key, session);
    }

    public void clearSession(Long userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        store.remove(key);
        log.info("[内存] 清空会话: userId={}, sessionId={}", userId, sessionId);
    }

    public void deleteUserSessions(Long userId) {
        String prefix = SESSION_KEY_PREFIX + userId + ":";
        store.entrySet().removeIf(e -> e.getKey().startsWith(prefix));
        log.info("[内存] 删除用户所有会话: userId={}", userId);
    }

    private String buildKey(Long userId, String sessionId) {
        return SESSION_KEY_PREFIX + userId + ":" + sessionId;
    }

    private static final String SESSION_KEY_PREFIX = "chat:session:";

    @lombok.Data
    public static class ChatSession {
        private String sessionId;
        private Long userId;
        private List<ChatMessage> messages;
        private LocalDateTime createdAt;
        private LocalDateTime lastActiveAt;
    }

    @lombok.Data
    public static class ChatMessage {
        private String role;
        private String content;
        private LocalDateTime createdAt;
    }
}