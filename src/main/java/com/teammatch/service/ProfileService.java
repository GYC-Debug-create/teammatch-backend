package com.teammatch.service;

import com.teammatch.entity.SkillTag;
import com.teammatch.entity.User;
import java.util.List;

/**
 * M3 档案服务接口
 */
public interface ProfileService {

    /**
     * 更新用户档案
     */
    void updateProfile(Long userId, User profile);

    /**
     * 绑定 GitHub 用户名
     */
    void bindGithub(Long userId, String githubUsername);

    /**
     * 为用户添加技能标签
     */
    void addSkill(Long userId, Long skillTagId);

    /**
     * 获取用户的技能标签 ID 列表
     */
    List<Long> getUserSkills(Long userId);

    /**
     * 添加新的技能标签到数据库
     */
    void addSkillTag(SkillTag skillTag);
}
