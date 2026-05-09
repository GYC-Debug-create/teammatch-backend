package com.teammatch.controller;

import com.teammatch.common.Result;
import com.teammatch.dto.AddSkillTagRequest;
import com.teammatch.dto.UpdateProfileRequest;
import com.teammatch.entity.SkillTag;
import com.teammatch.entity.User;
import com.teammatch.service.ProfileService;
import com.teammatch.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * M3 个人档案控制器
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    private static final String JWT_SECRET = "<REDACTED>";

    /**
     * 从 Token 中解析用户 ID
     */
    private Long getUserIdFromToken(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
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
     * D. 更新个人档案
     * 路径: PUT /api/profile/update
     */
    @PutMapping("/update")
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest request,
                                      @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);

        User profile = new User();
        profile.setNickname(request.getNickname());
        profile.setAvatarUrl(request.getAvatarUrl());
        profile.setSchool(request.getSchool());
        profile.setMajor(request.getMajor());
        profile.setGrade(request.getGrade());
        profile.setBio(request.getBio());

        profileService.updateProfile(userId, profile);
        return Result.success(null);
    }

    /**
     * E. 绑定 GitHub 账号
     * 路径: POST /api/profile/github/bind
     */
    @PostMapping("/github/bind")
    public Result<Void> bindGithub(@RequestBody java.util.Map<String, String> params,
                                   @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        String githubUsername = params.get("githubUsername");
        profileService.bindGithub(userId, githubUsername);
        return Result.success(null);
    }

    /**
     * F. 添加技能标签
     * 路径: POST /api/profile/skills/add
     */
    @PostMapping("/skills/add")
    public Result<Void> addSkill(@RequestBody java.util.Map<String, Long> params,
                                 @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        Long skillTagId = params.get("skillTagId");
        profileService.addSkill(userId, skillTagId);
        return Result.success(null);
    }

    /**
     * G. 添加新的技能标签到数据库（管理员功能）
     * 路径: POST /api/profile/skills/tag/add
     */
    @PostMapping("/skills/tag/add")
    public Result<Void> addSkillTag(@RequestBody AddSkillTagRequest request) {
        SkillTag skillTag = new SkillTag();
        skillTag.setName(request.getName());
        skillTag.setCategory(request.getCategory());
        skillTag.setStatus("active"); // 强制设为 active
        profileService.addSkillTag(skillTag);
        return Result.success(null);
    }
}
