-- ============================================================
-- T-149: M4 模块演示数据
-- 场景：项目A（id=1）、队长B（id=10）、成员C（id=20）、成员D（id=30）
--       完整覆盖：创建→开始→结束→组队邀请/申请→退出投票全流程
-- ============================================================

-- 清空顺序：先子表再主表
DELETE FROM exit_vote_record WHERE exit_vote_id IN (SELECT id FROM exit_vote WHERE project_id IN (1,2));
DELETE FROM exit_vote WHERE project_id IN (1,2);
DELETE FROM team_member WHERE project_id IN (1,2);
DELETE FROM team_request WHERE project_id IN (1,2);
DELETE FROM project_skill WHERE project_id IN (1,2);
DELETE FROM project WHERE id IN (1,2);

-- ----------------------------
-- 项目 A：已结束，互评窗口内
-- ----------------------------
INSERT INTO `project` (id, creator_id, board_id, title, description, max_members, status,
                        deadline, eval_deadline, ended_at, created_at, updated_at)
VALUES (1, 10, 1, '演示项目A——已结束', 'M4 演示用，项目已结束，处于互评窗口内', 5,
        'ended',
        '2026-04-01 18:00:00',
        DATE_ADD(NOW(), INTERVAL 5 DAY),   -- eval_deadline = 5天后，窗口内
        NOW(),
        '2026-03-01 10:00:00', NOW());

-- 项目 A 技能需求
INSERT INTO `project_skill` (project_id, skill_tag_id, created_at) VALUES
(1, 101, '2026-03-01 10:00:00'),   -- Java
(1, 102, '2026-03-01 10:00:00');   -- Spring Boot

-- ----------------------------
-- 项目 B：招募中
-- ----------------------------
INSERT INTO `project` (id, creator_id, board_id, title, description, max_members, status,
                        deadline, eval_deadline, ended_at, created_at, updated_at)
VALUES (2, 10, 1, '演示项目B——招募中', 'M4 演示用，正在招募成员', 4,
        'recruiting',
        DATE_ADD(NOW(), INTERVAL 30 DAY),
        NULL, NULL,
        NOW(), NOW());

INSERT INTO `project_skill` (project_id, skill_tag_id, created_at) VALUES
(2, 103, NOW());   -- Vue

-- ----------------------------
-- 项目 A 成员（队长B + 成员C + 成员D）
-- ----------------------------
INSERT INTO `team_member` (project_id, user_id, role, status, exit_mode, joined_at, left_at, created_at, updated_at)
VALUES
(1, 10, 'leader', 'active',  NULL,        '2026-03-01 10:00:00', NULL, NOW(), NOW()),
(1, 20, 'member', 'active',  NULL,        '2026-03-02 09:00:00', NULL, NOW(), NOW()),
(1, 30, 'member', 'exited',  'self_exit', '2026-03-03 11:00:00', '2026-03-20 15:00:00', NOW(), NOW());

-- 项目 B 成员（只有队长）
INSERT INTO `team_member` (project_id, user_id, role, status, exit_mode, joined_at, left_at, created_at, updated_at)
VALUES (2, 10, 'leader', 'active', NULL, NOW(), NULL, NOW(), NOW());

-- ----------------------------
-- 项目 B 组队请求样例
-- ----------------------------
-- 用户40 申请加入项目B（pending）
INSERT INTO `team_request` (project_id, from_user_id, to_user_id, request_type, status, message, created_at, updated_at)
VALUES (2, 40, 10, 'apply', 'pending', '我擅长 Vue，希望加入', NOW(), NOW());

-- 队长邀请用户50（已接受）
INSERT INTO `team_request` (project_id, from_user_id, to_user_id, request_type, status, message, handled_at, created_at, updated_at)
VALUES (2, 10, 50, 'invite', 'accepted', '欢迎加入我们的项目', NOW(), NOW(), NOW());

-- ----------------------------
-- 退出投票样例（项目A，队长B对成员C发起）
-- ----------------------------
INSERT INTO `exit_vote` (id, project_id, initiator_id, target_user_id, reason, status,
                          agree_count, disagree_count, total_voters, closed_at, created_at, updated_at)
VALUES
-- 已通过的投票（成员C被踢出示例，但team_member里C仍active用于演示互评）
(1, 1, 10, 20, '长期不参与项目工作', 'open', 1, 0, 2, NULL, NOW(), NOW());

-- 投票记录：队长B已投赞成
INSERT INTO `exit_vote_record` (exit_vote_id, voter_id, choice, created_at)
VALUES (1, 10, 'agree', NOW());
