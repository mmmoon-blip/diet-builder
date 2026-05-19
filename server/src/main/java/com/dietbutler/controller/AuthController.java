package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.entity.User;
import com.dietbutler.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody LoginRequest request) {
        Map<String, Object> result = authService.wechatLogin(
                request.getCode(),
                request.getNickname(),
                request.getAvatarUrl()
        );

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ApiResponse.success("登录成功", result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ApiResponse.error("未登录");
        }

        Optional<User> userOpt = authService.getCurrentUser(token);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return ApiResponse.success(user);
        } else {
            return ApiResponse.error("token无效或已过期");
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ApiResponse<Map<String, Object>> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ApiResponse.error("token不能为空");
        }

        Map<String, Object> result = authService.validateToken(token);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ApiResponse.success(result);
        } else {
            return ApiResponse.error((String) result.get("message"));
        }
    }

    @lombok.Data
    public static class LoginRequest {
        private String code;       // 微信code
        private String nickname;    // 昵称（可选）
        private String avatarUrl;   // 头像（可选）
    }
}