package com.teammatch.service;

import com.teammatch.dto.LoginRequest;
import com.teammatch.dto.PasswordLoginRequest;
import com.teammatch.dto.SendEmailCodeRequest;
import com.teammatch.dto.VerifyEmailCodeRequest;
import com.teammatch.dto.PasswordRequest;
import com.teammatch.dto.UsernameRequest;
import com.teammatch.entity.User;

/**
 * M3 认证服务接口
 */
public interface AuthService {

    /**
     * 微信 Mock 登录
     */
    User mockLogin(LoginRequest request);

    /**
     * 密码登录
     */
    User passwordLogin(PasswordLoginRequest request);

    /**
     * 发送邮箱验证码
     */
    void sendEmailCode(SendEmailCodeRequest request, String token);

    /**
     * 验证邮箱验证码
     */
    boolean verifyEmailCode(VerifyEmailCodeRequest request, String token);

    /**
     * 管理员创建用户密码
     * 需要校验当前用户是否为管理员
     */
    void adminCreatePassword(PasswordRequest request, String token);

    /**
     * 管理员修改用户密码
     * 需要校验当前用户是否为管理员
     */
    void adminUpdatePassword(PasswordRequest request, String token);

    /**
     * 用户自己修改密码
     */
    void changePassword(PasswordRequest request, String token);

    /**
     * 用户自己设置用户名（首次绑定）
     */
    void bindUsername(UsernameRequest request, String token);

    /**
     * 用户自己修改用户名
     */
    void updateUsername(UsernameRequest request, String token);

    /**
     * 校验当前用户是否为管理员
     * 
     * @param token JWT Token
     * @return true-是管理员，false-不是管理员
     * @throws RuntimeException Token 无效或用户不存在
     */
    boolean isAdmin(String token);

    /**
     * 生成 JWT Token 并存储到 Redis
     * 
     * @param user 用户实体
     * @return JWT Token 字符串
     */
    String generateToken(User user);
}
