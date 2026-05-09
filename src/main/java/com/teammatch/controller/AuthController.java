package com.teammatch.controller;

import com.teammatch.common.Result;
import com.teammatch.dto.LoginRequest;
import com.teammatch.dto.PasswordRequest;
import com.teammatch.dto.SendEmailCodeRequest;
import com.teammatch.dto.UsernameRequest;
import com.teammatch.dto.VerifyEmailCodeRequest;
import com.teammatch.entity.User;
import com.teammatch.service.AuthService;
import com.teammatch.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = authService.mockLogin(request);
        String token = ((AuthServiceImpl) authService).generateToken(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
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
        boolean success = authService.verifyEmailCode(request, token);
        if (success) {
            return Result.success("邮箱验证成功");
        } else {
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * 管理员创建用户密码
     */
    @PostMapping("/password/create")
    public Result<Void> createPassword(@RequestBody PasswordRequest request) {
        authService.adminCreatePassword(request);
        return Result.success(null);
    }

    /**
     * 管理员修改用户密码
     */
    @PostMapping("/password/update")
    public Result<Void> updatePassword(@RequestBody PasswordRequest request) {
        authService.adminUpdatePassword(request);
        return Result.success(null);
    }

    /**
     * 用户首次绑定用户名
     */
    @PostMapping("/username/bind")
    public Result<Void> bindUsername(@RequestBody UsernameRequest request,
                                     @RequestHeader("Authorization") String token) {
        authService.bindUsername(request, token);
        return Result.success(null);
    }

    /**
     * 用户修改用户名
     */
    @PutMapping("/username/update")
    public Result<Void> updateUsername(@RequestBody UsernameRequest request,
                                       @RequestHeader("Authorization") String token) {
        authService.updateUsername(request, token);
        return Result.success(null);
    }
}
