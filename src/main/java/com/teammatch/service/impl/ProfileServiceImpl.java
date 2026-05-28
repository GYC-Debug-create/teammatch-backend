package com.teammatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.common.ReasonCode;
import com.teammatch.entity.SkillTag;
import com.teammatch.entity.User;
import com.teammatch.entity.UserSkill;
import com.teammatch.exception.DuplicateDataException;
import com.teammatch.exception.NotFoundException;
import com.teammatch.exception.ValidationException;
import com.teammatch.mapper.SkillTagMapper;
import com.teammatch.mapper.UserMapper;
import com.teammatch.mapper.UserSkillMapper;
import com.teammatch.service.ProfileService;
import com.teammatch.service.TechProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M3 档案服务实现类
 */
@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillTagMapper skillTagMapper;

    @Autowired
    private UserSkillMapper userSkillMapper;

    @Autowired
    private TechProfileService techProfileService;

    @Override
    public User getProfileById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, User profile) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        if (profile.getNickname() != null) user.setNickname(profile.getNickname());
        if (profile.getAvatarUrl() != null) user.setAvatarUrl(profile.getAvatarUrl());
        if (profile.getSchool() != null) user.setSchool(profile.getSchool());
        if (profile.getMajor() != null) user.setMajor(profile.getMajor());
        if (profile.getGrade() != null) user.setGrade(profile.getGrade());
        if (profile.getBio() != null) user.setBio(profile.getBio());

        // 【实习要点4】更新档案后，重新计算正式档案状态
        // 【实习要点4】更新正式档案完成状态
        user.updateFormalProfileCompleted();
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void bindGithub(Long userId, String githubUsername) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        user.setGithubUsername(githubUsername);
        user.setGithubClaimed(true);
        userMapper.updateById(user);

        // 联动技术画像：搜索已有画像 → 有则认领 → 无则从 GitHub 生成并认领
        try {
            techProfileService.claimProfile(githubUsername, userId);
            log.info("技术画像认领成功: userId={}, githubUsername={}", userId, githubUsername);
        } catch (Exception e) {
            log.error("技术画像认领失败: userId={}, githubUsername={}, error={}", userId, githubUsername, e.getMessage());
            // 不阻断绑定流程，GitHub 绑定本身成功即可
        }
    }

    @Override
    @Transactional
    public void updateGithub(Long userId, String githubUsername) {
        User user = userMapper.selectById(userId);
        if (user == null) return;

        // 检查是否已经绑定过 GitHub
        if (user.getGithubClaimed() == null || !user.getGithubClaimed()) {
            throw new RuntimeException(ReasonCode.GITHUB_NOT_BOUND.getMessage());
        }

        user.setGithubUsername(githubUsername);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void addSkill(Long userId, Long skillTagId) {
        // 检查技能是否存在
        SkillTag tag = skillTagMapper.selectById(skillTagId);
        if (tag == null || !"active".equals(tag.getStatus())) return;

        // 检查是否已关联
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId).eq(UserSkill::getSkillTagId, skillTagId);
        if (userSkillMapper.selectCount(wrapper) > 0) return;

        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        userSkill.setSkillTagId(skillTagId);
        userSkillMapper.insert(userSkill);
    }

    @Override
    public List<Long> getUserSkills(Long userId) {
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSkill::getUserId, userId);
        List<UserSkill> userSkills = userSkillMapper.selectList(wrapper);
        return userSkills.stream().map(UserSkill::getSkillTagId).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void addSkillTag(SkillTag skillTag) {
        if (skillTag.getName() == null || skillTag.getName().isEmpty()) {
            throw new ValidationException(ReasonCode.SKILL_TAG_NAME_REQUIRED, "技能标签名称不能为空");
        }
        
        // 检查是否已存在同名同分类技能标签（DB 约束是 (category, name) 组合唯一）
        LambdaQueryWrapper<SkillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillTag::getName, skillTag.getName());
        if (skillTag.getCategory() != null) {
            wrapper.eq(SkillTag::getCategory, skillTag.getCategory());
        }
        SkillTag existing = skillTagMapper.selectOne(wrapper);
        if (existing != null) {
            throw new DuplicateDataException(ReasonCode.SKILL_TAG_ALREADY_EXISTS);
        }
        
        skillTag.setStatus("active");
        skillTagMapper.insert(skillTag);
    }

    @Override
    @Transactional
    public void updateUserSkills(Long userId, List<Long> skillTagIds) {
        // 【实习要点3】全量替换模式：先删除旧的技能，再插入新的
        
        // 1. 删除用户的所有旧技能
        LambdaQueryWrapper<UserSkill> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserSkill::getUserId, userId);
        userSkillMapper.delete(deleteWrapper);
        
        // 2. 如果传入空列表，表示清空所有技能，直接返回
        if (skillTagIds == null || skillTagIds.isEmpty()) {
            return;
        }
        
        // 3. 验证所有技能标签是否存在且为 active 状态
        for (Long skillTagId : skillTagIds) {
            SkillTag tag = skillTagMapper.selectById(skillTagId);
            if (tag == null || !"active".equals(tag.getStatus())) {
                throw new NotFoundException(ReasonCode.SKILL_TAG_NOT_FOUND);
            }
        }
        
        // 4. 批量插入新技能
        for (Long skillTagId : skillTagIds) {
            UserSkill userSkill = new UserSkill();
            userSkill.setUserId(userId);
            userSkill.setSkillTagId(skillTagId);
            userSkillMapper.insert(userSkill);
        }
    }

    @Override
    public List<SkillTag> listActiveSkillTags() {
        // 【实习要点3】预置字典模式：只返回激活状态的技能标签
        LambdaQueryWrapper<SkillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SkillTag::getStatus, "active");
        wrapper.orderByAsc(SkillTag::getCategory, SkillTag::getName);
        return skillTagMapper.selectList(wrapper);
    }
}
