package com.teammatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.entity.SkillTag;
import com.teammatch.entity.User;
import com.teammatch.entity.UserSkill;
import com.teammatch.mapper.SkillTagMapper;
import com.teammatch.mapper.UserMapper;
import com.teammatch.mapper.UserSkillMapper;
import com.teammatch.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M3 档案服务实现类
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SkillTagMapper skillTagMapper;

    @Autowired
    private UserSkillMapper userSkillMapper;

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

        checkFormalProfile(user);
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

    private void checkFormalProfile(User user) {
        boolean completed = Boolean.TRUE.equals(user.getEmailVerified()) &&
                user.getNickname() != null && !user.getNickname().isEmpty() &&
                user.getSchool() != null && !user.getSchool().isEmpty();
        user.setFormalProfileCompleted(completed);
    }

    @Override
    @Transactional
    public void addSkillTag(SkillTag skillTag) {
        if (skillTag.getName() == null || skillTag.getName().isEmpty()) {
            throw new RuntimeException("技能标签名称不能为空");
        }
        skillTag.setStatus("active");
        skillTagMapper.insert(skillTag);
    }
}
