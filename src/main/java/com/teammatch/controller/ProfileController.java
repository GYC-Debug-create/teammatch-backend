package com.teammatch.controller;

import com.teammatch.common.Result;
import com.teammatch.dto.AddSkillTagRequest;
import com.teammatch.dto.UpdateProfileRequest;
import com.teammatch.dto.UpdateUserSkillsRequest;
import com.teammatch.entity.SkillTag;
import com.teammatch.entity.User;
import com.teammatch.exception.DuplicateDataException;
import com.teammatch.exception.NotFoundException;
import com.teammatch.service.AuthService;
import com.teammatch.service.ProfileService;
import com.teammatch.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * M3 个人档案控制器
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthUtil authUtil;

    /**
     * D. 更新个人档案
     * 路径: PUT /api/profile/update
     */
    @PutMapping("/update")
    public Result<Void> updateProfile(@RequestBody UpdateProfileRequest request,
                                      @RequestHeader("Authorization") String token) {
        // 使用 AuthUtil 安全获取用户 ID
        Long userId = authUtil.getCurrentUserId(token);
        if (userId == null) {
            return Result.fail(com.teammatch.common.ReasonCode.UNAUTHORIZED);
        }

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
        // 使用 AuthUtil 安全获取用户 ID
        Long userId = authUtil.getCurrentUserId(token);
        if (userId == null) {
            return Result.fail(com.teammatch.common.ReasonCode.UNAUTHORIZED);
        }
        
        String githubUsername = params.get("githubUsername");
        profileService.bindGithub(userId, githubUsername);
        return Result.success(null);
    }

    /**
     * E1. 更新 GitHub 账号
     * 路径: PUT /api/profile/github/update
     * 说明: 仅当已绑定过 GitHub 账号时可修改，否则返回 M3012 错误
     */
    @PutMapping("/github/update")
    public Result<Void> updateGithub(@RequestBody java.util.Map<String, String> params,
                                     @RequestHeader("Authorization") String token) {
        try {
            // 使用 AuthUtil 安全获取用户 ID
            Long userId = authUtil.getCurrentUserId(token);
            if (userId == null) {
                return Result.fail(com.teammatch.common.ReasonCode.UNAUTHORIZED);
            }
            
            String githubUsername = params.get("githubUsername");
            profileService.updateGithub(userId, githubUsername);
            return Result.success(null);
        } catch (NotFoundException e) {
            // GitHub 账号未绑定
            return Result.fail(e.getReasonCode());
        } catch (Exception e) {
            // 未知异常
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * F. 添加技能标签（旧接口，保留兼容）
     * 路径: POST /api/profile/skills/add
     */
    @PostMapping("/skills/add")
    public Result<Void> addSkill(@RequestBody java.util.Map<String, Long> params,
                                 @RequestHeader("Authorization") String token) {
        // 使用 AuthUtil 安全获取用户 ID
        Long userId = authUtil.getCurrentUserId(token);
        if (userId == null) {
            return Result.fail(com.teammatch.common.ReasonCode.UNAUTHORIZED);
        }
        
        Long skillTagId = params.get("skillTagId");
        profileService.addSkill(userId, skillTagId);
        return Result.success(null);
    }

    /**
     * F1. 全量替换用户技能（新接口，推荐使用）
     * 路径: PUT /api/profile/skills
     * 
     * 【实习要点3】全量替换模式：前端提交最终的技能列表，后端先删除旧的再插入新的
     */
    @PutMapping("/skills")
    public Result<Void> updateUserSkills(@Valid @RequestBody UpdateUserSkillsRequest request,
                                         @RequestHeader("Authorization") String token) {
        // 使用 AuthUtil 安全获取用户 ID
        Long userId = authUtil.getCurrentUserId(token);
        if (userId == null) {
            return Result.fail(com.teammatch.common.ReasonCode.UNAUTHORIZED);
        }
        
        try {
            profileService.updateUserSkills(userId, request.getSkillTagIds());
            return Result.success(null);
        } catch (NotFoundException e) {
            // 技能标签不存在
            return Result.fail(e.getReasonCode());
        } catch (Exception e) {
            // 未知异常
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }

    /**
     * F2. 获取所有可用的技能标签列表
     * 路径: GET /api/profile/skills/tags
     * 
     * 【实习要点3】预置字典模式：技能标签由系统预置，用户只能从中选择
     */
    @GetMapping("/skills/tags")
    public Result<List<SkillTag>> listSkillTags() {
        List<SkillTag> tags = profileService.listActiveSkillTags();
        return Result.success(tags);
    }

    /**
     * G. 添加新的技能标签到数据库（管理员功能）
     * 路径: POST /api/profile/skills/tag/add
     */
    @PostMapping("/skills/tag/add")
    public Result<Void> addSkillTag(@RequestBody AddSkillTagRequest request,
                                    @RequestHeader("Authorization") String token) {
        // 管理员权限校验
        if (!authService.isAdmin(token)) {
            return Result.fail(com.teammatch.common.ReasonCode.ADMIN_REQUIRED);
        }
        
        SkillTag skillTag = new SkillTag();
        skillTag.setName(request.getName());
        skillTag.setCategory(request.getCategory());
        skillTag.setStatus("active"); // 强制设为 active
        try {
            profileService.addSkillTag(skillTag);
            return Result.success(null);
        } catch (DuplicateDataException e) {
            // 技能标签已存在
            return Result.fail(e.getReasonCode());
        } catch (Exception e) {
            // 未知异常
            return Result.fail(com.teammatch.common.ReasonCode.UNKNOWN_ERROR);
        }
    }
}
