package com.teammatch.config;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证相关全局异常处理器
 * 
 * 【实习要点】统一处理认证异常，避免各模块重复编写 try-catch
 * 
 * 处理的异常类型：
 * - MalformedJwtException: Token 格式错误
 * - ExpiredJwtException: Token 已过期
 * - SignatureException: Token 签名验证失败
 * - RuntimeException: AuthService 抛出的认证相关异常
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    /**
     * 处理 JWT 相关异常
     * 
     * @param e JWT 异常
     * @return 统一的未授权响应
     */
    @ExceptionHandler({MalformedJwtException.class, ExpiredJwtException.class, SignatureException.class})
    public Result<Void> handleJwtException(Exception e) {
        return Result.fail(ReasonCode.UNAUTHORIZED);
    }

    /**
     * 处理认证相关的 RuntimeException
     * 注意：只处理来自 AuthService 的认证异常，避免误捕其他业务异常
     * 
     * @param e 运行时异常
     * @return 统一的未授权响应或未知错误
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        // 检查是否是认证相关的异常
        if (isAuthRelatedException(e)) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }
        // 其他 RuntimeException 返回未知错误
        return Result.fail(ReasonCode.UNKNOWN_ERROR);
    }

    /**
     * 判断异常是否与认证相关
     * 
     * @param e 异常对象
     * @return true-认证相关异常，false-其他异常
     */
    private boolean isAuthRelatedException(RuntimeException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        // 检查异常消息是否包含认证相关的关键词
        return message.contains("Invalid or expired token") ||
               message.contains("token") ||
               message.contains("Token");
    }
}
