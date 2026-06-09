package com.teammatch.service;

import com.teammatch.dto.LeaderboardEntryVO;
import com.teammatch.dto.TechProfileVO;
import com.teammatch.entity.TechProfile;

import java.util.List;

/**
 * M3 技术画像服务接口
 * 负责从 GitHub API 获取数据、生成/更新技术画像、提供排行榜查询
 */
public interface TechProfileService {

    /**
     * 根据 GitHub 用户名从 GitHub API 获取数据并生成技术画像
     * 若已存在则更新，不存在则新建
     *
     * @param githubUsername GitHub 用户名
     * @return 保存后的技术画像
     */
    TechProfile generateOrUpdateProfile(String githubUsername);

    /**
     * 认领技术画像（将 claimed_by_user_id 设为当前用户）
     *
     * @param githubUsername GitHub 用户名
     * @param userId 认领用户 ID
     * @return 认领后的技术画像
     */
    TechProfile claimProfile(String githubUsername, Long userId);

    /**
     * 根据用户 ID 获取该用户认领的技术画像
     *
     * @param userId 用户 ID
     * @return 技术画像 VO，未认领返回 null
     */
    TechProfileVO getProfileByUserId(Long userId);

    /**
     * 获取排行榜（按 tech_score 降序）
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 排行榜条目列表
     */
    List<LeaderboardEntryVO> getLeaderboard(int page, int size);

    /**
     * 获取排行榜总数
     */
    long getLeaderboardCount();

    /**
     * 事务提交后触发异步同步 GitHub 数据
     * 由调用方在事务外调用，确保 @Async 能读到已提交的数据
     */
    void triggerAsyncSync(String githubUsername);

    /**
     * 根据 GitHub 用户名查找技术画像
     */
    com.teammatch.entity.TechProfile findByGithubUsername(String githubUsername);

    /**
     * 更新技术画像
     */
    void updateTechProfile(com.teammatch.entity.TechProfile profile);

    /**
     * 删除技术画像
     */
    void deleteTechProfile(com.teammatch.entity.TechProfile profile);
}