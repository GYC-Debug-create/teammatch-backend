package com.teammatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.common.ReasonCode;
import com.teammatch.dto.LoginRequest;
import com.teammatch.dto.PasswordRequest;
import com.teammatch.dto.SendEmailCodeRequest;
import com.teammatch.dto.UsernameRequest;
import com.teammatch.dto.VerifyEmailCodeRequest;
import com.teammatch.entity.User;
import com.teammatch.mapper.UserMapper;
import com.teammatch.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * M3 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CODE_PREFIX = "email:code:";
    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String JWT_SECRET = "<REDACTED>";
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Override
    public User mockLogin(LoginRequest request) {
        // Mock 逻辑：直接使用 code 作为 openid 查找或创建用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, request.getCode());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setOpenid(request.getCode());
            user.setNickname("User_" + request.getCode().substring(0, Math.min(6, request.getCode().length())));
            user.setCreditScore(100);
            user.setRole("user");
            user.setStatus("active");
            userMapper.insert(user);
        }
        return user;
    }

    @Override
    public void sendEmailCode(SendEmailCodeRequest request, String token) {
        Long userId = getUserIdFromToken(token);
        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = CODE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, code, 10, TimeUnit.MINUTES);
        // 实际项目中应调用邮件发送服务
        System.out.println("Email code for user " + userId + " (" + request.getEmail() + "): " + code);
    }

    @Override
    public boolean verifyEmailCode(VerifyEmailCodeRequest request, String token) {
        Long userId = getUserIdFromToken(token);
        String key = CODE_PREFIX + userId;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode != null && storedCode.equals(request.getCode())) {
            redisTemplate.delete(key);
            
            // 检查该邮箱是否已被其他用户占用
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, request.getEmail());
            User existingUser = userMapper.selectOne(wrapper);
            
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException(ReasonCode.EMAIL_OCCUPIED.getMessage());
            }
            
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setEmail(request.getEmail());
                user.setEmailVerified(true);
                
                // 【实习要点4】邮箱验证成功后，重新计算正式档案状态
                // 避免用户先填写档案再验证邮箱时，formalProfileCompleted 没有更新
                checkAndUpdateFormalProfile(user);
                
                userMapper.updateById(user);
            }
            return true;
        }
        return false;
    }

    @Override
    public void adminCreatePassword(PasswordRequest request, String token) {
        // 【实习要点1】管理员权限校验：任何管理端接口都必须验证当前用户是否为管理员
        // 面试时可以说："我实现了基于角色的访问控制（RBAC），确保只有管理员才能操作敏感数据"
        Long adminId = getUserIdFromToken(token);
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            throw new RuntimeException(ReasonCode.ADMIN_REQUIRED.getMessage());
        }
        
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException(ReasonCode.PASSWORD_TOO_SHORT.getMessage());
        }
        
        // 检查用户是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user != null && user.getPasswordHash() != null) {
            throw new RuntimeException("该用户已设置密码，请使用修改密码接口");
        }
        
        // 如果用户不存在，创建新用户
        if (user == null) {
            user = new User();
            user.setUsername(request.getUsername());
            user.setRole("user");
            user.setStatus("active");
            userMapper.insert(user);
        }
        
        // 设置密码
        user.setPasswordHash(PASSWORD_ENCODER.encode(request.getPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void adminUpdatePassword(PasswordRequest request, String token) {
        // 【实习要点1】管理员权限校验
        Long adminId = getUserIdFromToken(token);
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            throw new RuntimeException(ReasonCode.ADMIN_REQUIRED.getMessage());
        }
        
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException(ReasonCode.PASSWORD_TOO_SHORT.getMessage());
        }
        
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果提供了旧密码，需要验证旧密码是否正确
        if (request.getOldPassword() != null && !request.getOldPassword().isEmpty()) {
            if (user.getPasswordHash() == null || !PASSWORD_ENCODER.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new RuntimeException(ReasonCode.OLD_PASSWORD_INCORRECT.getMessage());
            }
        }
        
        // 更新密码
        user.setPasswordHash(PASSWORD_ENCODER.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void bindUsername(UsernameRequest request, String token) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        Long userId = getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果已经绑定过 username，不允许重复绑定
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            throw new RuntimeException(ReasonCode.USERNAME_ALREADY_BOUND.getMessage());
        }
        
        // 检查用户名是否已被其他用户占用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(wrapper);
        
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException(ReasonCode.USERNAME_OCCUPIED.getMessage());
        }
        
        user.setUsername(request.getUsername());
        userMapper.updateById(user);
    }

    @Override
    public void updateUsername(UsernameRequest request, String token) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        Long userId = getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查用户名是否已被其他用户占用
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User existingUser = userMapper.selectOne(wrapper);
        
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException("该用户名已被占用");
        }
        
        user.setUsername(request.getUsername());
        userMapper.updateById(user);
    }

    /**
     * 从 Token 中解析用户 ID
     */
    private Long getUserIdFromToken(String token) {
        // 移除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 直接从 JWT 中解析 Subject (userId)
        try {
            String userIdStr = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired token", e);
        }
    }

    /**
     * 生成 JWT Token（供 Controller 调用）
     */
    public String generateToken(User user) {
        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7天
                .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
                .compact();
        
        // 将 token 与 userId 关联存储到 Redis
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, String.valueOf(user.getId()), 7, TimeUnit.DAYS);
        
        return token;
    }

    /**
     * 【实习要点4】检查并更新正式档案状态
     * 在以下场景调用：
     * 1. 邮箱验证成功后
     * 2. 更新个人档案后
     * 
     * 正式档案完成条件：
     * - 邮箱已验证
     * - 昵称不为空
     * - 学校不为空
     */
    private void checkAndUpdateFormalProfile(User user) {
        boolean completed = Boolean.TRUE.equals(user.getEmailVerified()) &&
                user.getNickname() != null && !user.getNickname().isEmpty() &&
                user.getSchool() != null && !user.getSchool().isEmpty();
        user.setFormalProfileCompleted(completed);
    }
}
