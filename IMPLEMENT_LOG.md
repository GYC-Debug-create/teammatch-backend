# IMPLEMENT_LOG

## 2026-05-12 M5-5 补充提交

- `Evaluation` 新增互评状态常量：`normal`、`pending_review`、`voided`、`kept_no_credit`。
- `EvaluationSubmitServiceImpl` 将互评提交链路中的 `normal` / `pending_review` 状态字符串替换为常量引用，并补充精简日志：提交入口、异常挂起触发、信誉分更新失败。
- 已知限制：V2.1 当前未定义全局信誉分下限，`user.credit_score` 理论上可被有效 `credit_change` 扣至负值。由于 `user.credit_score = 100 + SUM(change_value WHERE effective=1)` 是由 `credit_change` 推导出的缓存，直接在 SQL 中截断会破坏异常撤销、申诉恢复和审计时的流水一致性。本轮不引入 `GREATEST` 下限，待全组确认下限规则后统一设计落地。
