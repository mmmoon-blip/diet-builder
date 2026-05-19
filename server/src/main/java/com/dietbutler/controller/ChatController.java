package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.dto.ChatRequest;
import com.dietbutler.dto.ChatResponse;
import com.dietbutler.service.AuthService;
import com.dietbutler.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    @PostMapping("/send")
    public ApiResponse<ChatResponse> send(@RequestBody ChatRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String token,
                                          @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        // 从token中获取用户ID
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Optional<Long> userIdOpt = authService.getUserIdFromToken(token);
            if (userIdOpt.isPresent()) {
                userId = userIdOpt.get();
            }
        }

        // 如果token中没有userId，使用请求中的userId
        if (userId == null && request.getUserId() != null) {
            userId = request.getUserId();
        }

        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }

        ChatResponse response = chatService.chat(userId, request.getMessage(), sessionId);
        return ApiResponse.success(response);
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public ApiResponse<String> createSession(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Optional<Long> userIdOpt = authService.getUserIdFromToken(token);
            if (userIdOpt.isPresent()) {
                userId = userIdOpt.get();
            }
        }

        if (userId == null) {
            return ApiResponse.error("用户未登录");
        }

        String sessionId = chatService.createSession(userId);
        return ApiResponse.success("会话创建成功", sessionId);
    }
}