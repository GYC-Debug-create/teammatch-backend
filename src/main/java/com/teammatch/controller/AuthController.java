package com.teammatch.controller;

import com.teammatch.common.Result;
import com.teammatch.dto.LoginRequest;
import com.teammatch.dto.LoginResponse;
import com.teammatch.dto.PasswordRequest;
import com.teammatch.dto.SendEmailCodeRequest;
import com.teammatch.dto.UsernameRequest;
import com.teammatch.dto.VerifyEmailCodeRequest;
import com.teammatch.entity.User;
import com.teammatch.service.AuthService;
import com.teammatch.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器 - M3 模块
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 微信 Mock 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = authService.mockLogin(request);
        String token = ((AuthServiceImpl) authService).generateToken(user);
        
        // 【实习要点2】构建 DTO，只返回前端需要的字段
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setEmailVerified(user.getEmailVerified());
        response.setFormalProfileCompleted(user.getFormalProfileCompleted());
        response.setCreditScore(user.getCreditScore());
        response.setToken(token);
        
        return Result.success(response);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email/send")
    public Result<Void> sendEmailCode(@RequestBody SendEmailCodeRequest request, 
                                @RequestHeader("Authorization") String token) {
        authService.sendEmailCode(request, token);
        return Result.success(null);
    }

    /**
     * 验证邮箱验证码
     */
    @PostMapping("/email/verify")
    public Result<String> verifyEmailCode(@RequestBody VerifyEmailCodeRequest request, 
                                  @RequestHeader("Authorization") String token) {
        try {
            boolean success = authService.verifyEmailCode(request, token);
            if (success) {
                return Result.success("邮箱验证成功");
            } else {
                return Result.fail(com.teammatch.common.ReasonCode.INVALID_CODE);
            }
        } catch (RuntimeException e) {
            // 根据错误消息判断具体的错误码
            String message = e.getMessage();
            if (message != null && message.contains("邮箱已被其他用户绑定")) {
                return Result.fail(com.teammatch.common.ReasonCode.EMAIL_OCCUPIED);
            }
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 管理员创建用户密码
     * 需要管理员权限（role=admin）
     */
    @PostMapping("/password/create")
    public Result<Void> createPassword(@RequestBody PasswordRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            authService.adminCreatePassword(request, token);
            return Result.success(null);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("需要管理员权限")) {
                    return Result.fail(com.teammatch.common.ReasonCode.ADMIN_REQUIRED);
                }
                if (message.contains("密码长度")) {
                    return Result.fail(com.teammatch.common.ReasonCode.PASSWORD_TOO_SHORT);
                }
            }
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 管理员修改用户密码
     * 需要管理员权限（role=admin）
     */
    @PostMapping("/password/update")
    public Result<Void> updatePassword(@RequestBody PasswordRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            authService.adminUpdatePassword(request, token);
            return Result.success(null);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("需要管理员权限")) {
                    return Result.fail(com.teammatch.common.ReasonCode.ADMIN_REQUIRED);
                }
                if (message.contains("密码长度")) {
                    return Result.fail(com.teammatch.common.ReasonCode.PASSWORD_TOO_SHORT);
                }
                if (message.contains("旧密码不正确")) {
                    return Result.fail(com.teammatch.common.ReasonCode.OLD_PASSWORD_INCORRECT);
                }
            }
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 用户首次绑定用户名
     */
    @PostMapping("/username/bind")
    public Result<Void> bindUsername(@RequestBody UsernameRequest request,
                                     @RequestHeader("Authorization") String token) {
        try {
            authService.bindUsername(request, token);
            return Result.success(null);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null) {
                if (message.contains("用户名已绑定")) {
                    return Result.fail(com.teammatch.common.ReasonCode.USERNAME_ALREADY_BOUND);
                }
                if (message.contains("用户名已被占用")) {
                    return Result.fail(com.teammatch.common.ReasonCode.USERNAME_OCCUPIED);
                }
            }
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 用户修改用户名
     */
    @PutMapping("/username/update")
    public Result<Void> updateUsername(@RequestBody UsernameRequest request,
                                       @RequestHeader("Authorization") String token) {
        try {
            authService.updateUsername(request, token);
            return Result.success(null);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("用户名已被占用")) {
                return Result.fail(com.teammatch.common.ReasonCode.USERNAME_OCCUPIED);
            }
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }
}
