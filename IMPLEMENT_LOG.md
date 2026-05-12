# IMPLEMENT_LOG

## 2026-05-12 阶段进度总览

| 模块 | 负责 | 进度 | 关键产出 |
|---|---|---|---|
| M3 认证/档案/技能 | 后端一 | ~95% | Auth + Profile Controller/Service，权限基础，技能标签 CRUD |
| M4 项目/组队/退出 | 后端二 | ~90% | Project/TeamRequest/ExitVote Controller + Service，并发关闭协议，35+17 测试 |
| M5 互评/信誉/申诉 | 后端三 | ~80% | 7/7 Service 完成（M5-1~M5-7），143 测试，**Controller 待补** |
| M6 治理/部署 | 治理 | ~30% | Board 板块管理完成，Report/Penalty/申诉审批/Jenkins 未开始 |
| M1 前端一 | 前端 | ~60-70% | 认证/档案/冷启动/项目浏览（7 页面） |
| M2 前端二+后台 | 前端 | ~50-60% | 组队/退出/互评/信誉展示/管理端（6+5 页面） |

**后端总体 ~74%，全项目总体 ~65-70%。** Phase 2 收尾阶段。

**M5 待办：**
- Controller 接口（互评资格/提交/列表/信誉分/评价复核管理端）
- 与 M2 对齐互评页面字段、信誉分展示
- 与 M6 对齐评价复核管理端入口、申诉流程边界
- M5-7 Gerrit Change: https://gerrit.lilingkun.com/c/shixun12_backend/+/2792

---

## 2026-05-12 M5-7 申诉恢复 Service

- 新增 `AppealRestoreService` + `AppealRestoreServiceImpl`：对已批准的 `evaluation` 类申诉执行信誉恢复，追加 `appeal_restore` 流水并回写 `user.credit_score`，全链路 `@Transactional`。
- 新增 DTO：`AppealRestoreCommand`（入参，仅 appealId）、`AppealRestoreResult`（出参，含 skipped/alreadyRestored/restoreValue）。
- 新增实体：`Appeal`（最小化字段：id / userId / targetType / targetId / status），M6 可扩展完整字段映射。
- 新增 Mapper：`AppealMapper`（继承 BaseMapper，selectById 自动可用）。
- 新增 Mapper 方法：
  - `CreditChangeMapper.findBySourceTypeAndSourceIdAndChangeType`：按精确四维键查原评价流水（不预判 effective，由业务代码判断）。
  - `CreditChangeMapper.findAppealRestoreExists`：幂等检查，查是否已有 `appeal_restore` 流水。
  - `EvaluationMapper.selectByIdForUpdate`：SELECT FOR UPDATE 锁定 evaluation 行（V2.1 8.7 D-11/D-13）。
- 新增 ReasonCode：`M5009`(APPEAL_NOT_FOUND)、`M5010`(APPEAL_NOT_APPROVED)、`M5011`(INVALID_APPEAL_TARGET_TYPE)、`EVALUATION_ALREADY_INVALIDATED`（字符串常量名）。
- 删除 ReasonCode：`M5012`(APPEAL_RESTORE_ALREADY_EXECUTED) —— 幂等分支改为 `success(alreadyRestored=true)`，该常量不会被任何业务路径返回。
- 关键契约（V2.1 6.7 冻结口径）：
  - 仅处理 `appeal.target_type='evaluation'`，其他类型抛 `INVALID_APPEAL_TARGET_TYPE`。`function_limit` 申诉恢复由 M6 单独处理，不在本服务范围。
  - 入口 null 防护：`command` 或 `appealId` 为 null 返回 `PARAM_ERROR`。
  - userId 一致性校验：`appeal.userId` 必须等于 `evaluation.targetId`（使用 `Objects.equals`），不一致返回 `PARAM_ERROR` + `log.warn`。
  - voided 评价已判无效，直接拒绝返回 `EVALUATION_ALREADY_INVALIDATED`。
  - 原流水 `effective=0`（pending_review / kept_no_credit）→ `success(skipped=true)`，不视为失败。
  - 幂等：同一 `(source_type, source_id, change_type, user_id)` 已有 `appeal_restore` 流水 → `success(alreadyRestored=true)`，不视为失败。
  - 恢复流水 `change_value = -original.changeValue`，`effective=true`，`source_type='evaluation'`。
  - 恢复值以已锁定的 `evaluation.getId()` 作为 `sourceId`（而非 `appeal.getTargetId()`）。
  - 代码中所有字符串字面量（`"evaluation"` / `"approved"` / `"appeal_restore"`）已提取为 `static final` 常量。
- 测试：19 单测全量通过，覆盖正常恢复（负分/正分/零分）、幂等跳过、effective=0 跳过、7 种异常分支（appeal 不存在/未批准/非 evaluation 类型/userId 不一致/evaluation 不存在/voided/原流水缺失）、2 种事务回滚（insert 失败/updateCreditScore 失败）。
- 已知限制：
  - 无 Controller 接口入口（M5-7 仅提供 Service，由 M6 审批流程中直接调用）。
  - `function_limit` 申诉恢复由 M6 单独实现，不在 M5-7 范围。
  - SELECT FOR UPDATE 锁竞争场景标注为集成测试/手工覆盖，不在 JUnit 单测中强制要求。

## 2026-05-12 M5-6 评价复核 Service

- 新增 `EvaluationReviewService` + `EvaluationReviewServiceImpl`：对 `pending_review` 评价执行 `approve` / `void` / `keep_no_credit` 三种复核操作，`@Transactional` 保证 evaluation.status + credit_change.effective + user.credit_score 原子更新。
- 新增 DTO：`EvaluationReviewCommand`（入参）、`EvaluationReviewResult`（返回值，含 oldStatus / newStatus / creditDelta / creditEffectiveChanged）。
- 新增 `ReviewAction` 枚举（`approve` / `void` / `keep_no_credit`），入口校验无效 action 返回 `INVALID_REVIEW_ACTION`。
- 新增 ReasonCode：`M5005`(EVALUATION_NOT_FOUND)、`M5006`(CREDIT_CHANGE_NOT_FOUND)、`M5007`(INVALID_REVIEW_ACTION)、`M5008`(REVIEW_NOTE_TOO_LONG)。
- 新增 Mapper 方法：`CreditChangeMapper.findSuspendedOne`（限定 source_type + source_id + change_type + user_id + effective=0 查询单条挂起流水）、`EvaluationMapper.updateReview`（更新 status + reviewer_id + review_note + reviewed_at + updated_at）。
- 关键契约：
  - `approve`：evaluation.status → normal，credit_change.effective 0→1，user.credit_score 增量加回（不绕开 credit_change）。
  - `void`：evaluation.status → voided，credit_change.effective 保持 0，不更新 credit_score。
  - `keep_no_credit`：evaluation.status → kept_no_credit，credit_change.effective 保持 0，不更新 credit_score。
  - 重复 approve 或其他状态提交 → `STATUS_CONFLICT`，不重复加分。
- 测试：26 单测全量通过，覆盖三种成功路径、5 种异常分支（不存在/状态冲突/流水缺失/已生效/备注过长）、3 种落库失败回滚。
- 已知限制：尚无 Controller 暴露复核 API（M5 专注底层能力），集成测试待后续补充。
- Gerrit Change: I7431dab4df3b7348cdf3b2e0e76740b9556c0ca3

## 2026-05-12 M5-5 补充提交

- `Evaluation` 新增互评状态常量：`normal`、`pending_review`、`voided`、`kept_no_credit`。
- `EvaluationSubmitServiceImpl` 将互评提交链路中的 `normal` / `pending_review` 状态字符串替换为常量引用，并补充精简日志：提交入口、异常挂起触发、信誉分更新失败。
- 已知限制：V2.1 当前未定义全局信誉分下限，`user.credit_score` 理论上可被有效 `credit_change` 扣至负值。由于 `user.credit_score = 100 + SUM(change_value WHERE effective=1)` 是由 `credit_change` 推导出的缓存，直接在 SQL 中截断会破坏异常撤销、申诉恢复和审计时的流水一致性。本轮不引入 `GREATEST` 下限，待全组确认下限规则后统一设计落地。
