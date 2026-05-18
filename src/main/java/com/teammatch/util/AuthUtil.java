package com.teammatch.util;

import com.teammatch.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 认证工具类
 * 
 * 【实习要点】提供便捷的认证方法，简化 Controller 代码
 * 
 * 主要功能：
 * 1. 安全获取用户 ID（token 无效时返回 null，不抛异常）
 * 2. 安全检查管理员权限
 * 3. 强制获取用户 ID（token 无效时抛出异常）
 */
@Component
public class AuthUtil {

    @Autowired
    private AuthService authService;

    /**
     * 获取当前用户 ID，token 无效时返回 null
     * 
     * 适用场景：
     * - 需要优雅处理 token 无效的情况
     * - 不想使用 try-catch
     * 
     * @param token JWT Token（可带 "Bearer " 前缀）
     * @return 用户 ID，token 无效时返回 null
     */
    public Long getCurrentUserId(String token) {
        try {
            return authService.getUserIdFromToken(token);
        } catch (RuntimeException e) {
            // Token 无效时返回 null，由调用方决定如何处理
            return null;
        }
    }

    /**
     * 检查当前用户是否是管理员
     * 
     * 适用场景：
     * - 需要优雅处理 token 无效的情况
     * - 不想使用 try-catch
     * 
     * @param token JWT Token（可带 "Bearer " 前缀）
     * @return true-是管理员，false-不是管理员或 token 无效
     */
    public boolean isCurrentUserAdmin(String token) {
        try {
            return authService.isAdmin(token);
        } catch (RuntimeException e) {
            // Token 无效时返回 false
            return false;
        }
    }

    /**
     * 获取用户 ID 或抛出认证异常
     * 
     * 适用场景：
     * - 必须确保用户已认证
     * - token 无效时需要立即中断流程
     * 
     * @param token JWT Token（可带 "Bearer " 前缀）
     * @return 用户 ID
     * @throws RuntimeException token 无效时抛出异常（会被全局异常处理器捕获）
     */
    public Long getCurrentUserIdOrThrow(String token) {
        Long userId = getCurrentUserId(token);
        if (userId == null) {
            throw new RuntimeException("Invalid or expired token");
        }
        return userId;
    }
}
