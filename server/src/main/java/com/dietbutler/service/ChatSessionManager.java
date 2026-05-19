package com.dietbutler.service;

import java.util.List;

/**
 * 对话Session管理接口
 * 支持Redis实现和内存实现
 */
public interface ChatSessionManager {

    String createSession(Long userId);

    String getOrCreateSession(Long userId, String existingSessionId);

    Object getSession(Long userId, String sessionId);

    List<?> getMessages(Long userId, String sessionId);

    void addMessage(Long userId, String sessionId, String role, String content);

    void clearSession(Long userId, String sessionId);

    void deleteUserSessions(Long userId);
}