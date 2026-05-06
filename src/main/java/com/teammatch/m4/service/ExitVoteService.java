package com.teammatch.m4.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teammatch.m4.dto.ExitVoteCreateDTO;
import com.teammatch.m4.dto.ExitVoteSubmitDTO;
import com.teammatch.m4.entity.ExitVote;

/**
 * T-136~T-141: 退出投票服务接口
 */
public interface ExitVoteService extends IService<ExitVote> {

    /** T-136: 成员主动退出 */
    void selfExit(Long projectId, Long userId);

    /** T-137: 队长发起退出投票 */
    ExitVote initiateVote(Long projectId, ExitVoteCreateDTO dto);

    /** T-138: 投票详情 */
    ExitVote getVoteDetail(Long voteId);

    /** T-139: 提交投票（agree/disagree），防重复 */
    void submitVote(Long voteId, ExitVoteSubmitDTO dto);

    /** T-140: 关闭投票并执行结果（加事务+幂等） */
    ExitVote closeVote(Long voteId, Long operatorId);
}
