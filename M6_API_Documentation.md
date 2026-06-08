# M6 模块（治理/举报/处罚）API 文档

> 版本：master 联调版 | 接口总数：**27** | 维护：M6

---

## 1. 概述

M6 负责 TeamMatch 平台**治理闭环**：板块分类、举报、处罚、申诉、评价复核（管理端）。

| 对接方 | 使用的接口 | 场景 |
|--------|-----------|------|
| **M2 Web 管理后台** | `/api/admin/*`（19 个） | 5 个后台页面：板块 / 举报 / 处罚 / 申诉 / 评价复核 |
| **M1 小程序** | `GET /api/boards` | 创建项目时选择板块 |
| **M1/M2 小程序用户** | `/api/reports`、`/api/appeals`、`/api/penalties` | 举报、申诉、查看自己的处罚 |

**业务边界（P0 冻结）**：
- 举报对象仅 `user` / `project`，**不能**举报 evaluation
- **pending_review 评价不开放用户申诉**；待复核由管理员走评价复核（#24–#25）
- 仅 **normal**（已计分）评价可对 `evaluation` 发起申诉；**M2/M1 小程序「我收到的互评」页**对 `pending_review` **不展示申诉按钮**（可展示「待复核」标签）
- 退出投票结果不可申诉

---

## 2. 接口速查表

| # | 方法 | 路径 | 权限 |
|---|------|------|------|
| 1 | GET | `/boards` | 公开 |
| 2 | POST | `/admin/boards` | 管理员 |
| 3 | PUT | `/admin/boards/{id}` | 管理员 |
| 4 | DELETE | `/admin/boards/{id}` | 管理员 |
| 5 | GET | `/admin/boards/{id}` | 管理员 |
| 6 | GET | `/admin/boards` | 管理员 |
| 7 | POST | `/reports` | 登录用户 |
| 8 | GET | `/reports/my` | 登录用户 |
| 9 | GET | `/admin/reports` | 管理员 |
| 10 | GET | `/admin/reports/{id}` | 管理员 |
| 11 | PUT | `/admin/reports/{id}/handle` | 管理员 |
| 12 | GET | `/penalties/my` | 登录用户 |
| 13 | GET | `/penalties/my/active` | 登录用户 |
| 14 | POST | `/admin/penalties` | 管理员 |
| 15 | GET | `/admin/penalties` | 管理员 |
| 16 | GET | `/admin/penalties/{id}` | 管理员 |
| 17 | PUT | `/admin/penalties/{id}/revoke` | 管理员 |
| 18 | POST | `/appeals` | 登录用户 |
| 19 | GET | `/appeals/appealable/evaluations` | 登录用户 |
| 20 | GET | `/appeals/appealable/penalties` | 登录用户 |
| 21 | GET | `/appeals/my` | 登录用户 |
| 22 | GET | `/appeals/{id}` | 登录用户（仅本人） |
| 23 | GET | `/admin/appeals` | 管理员 |
| 24 | GET | `/admin/appeals/{id}` | 管理员 |
| 25 | PUT | `/admin/appeals/{id}/handle` | 管理员 |
| 26 | GET | `/admin/evaluations/pending` | 管理员 |
| 27 | POST | `/admin/evaluations/{id}/review` | 管理员 |

> 以上路径均相对于 Base URL `http://localhost:8080/api`，完整 URL 需加前缀 `/api`。

---

## 3. 鉴权与登录（M3 提供）

M6 接口本身不提供登录，Token 通过 M3 认证接口获取。

### 3.1 管理员登录（测管理端必用）

**接口**：`POST /api/auth/login/password`（M3）

**请求 Body**：
```json
{
  "username": "admin",
  "password": "your_password"
}
```

**响应**（成功）：
```json
{
  "code": "00000",
  "message": "成功",
  "data": {
    "id": 1,
    "nickname": "管理员",
    "token": "<REDACTED>"
  }
}
```

后续管理端请求携带：`Authorization: Bearer {data.token}`

### 3.2 普通用户登录（测举报/申诉必用）

**接口**：`POST /api/auth/login`（M3，微信 Mock 登录）

**请求 Body**：
```json
{
  "code": "mock_wx_code_user_a"
}
```

**响应**：同上，`data.token` 即为用户 Token。

### 3.3 鉴权规则

| 角色 | 校验方式 | 失败码 |
|------|----------|--------|
| 未登录 / Token 无效 | `AuthUtil.requireUserId` | M3000 |
| 已登录但非管理员 | `AuthUtil.requireAdmin` | M3009 |
| 访问他人资源 | 业务层校验 | M1004 |

---

## 4. 基础约定

### 4.1 服务地址

- **Base URL**：`http://localhost:8080/api`
- **Context Path**：`/api`（`application.yml` 已配置，Apifox 环境变量请设此前缀）

### 4.2 公共请求头

```
Content-Type: application/json
Authorization: Bearer <JWT Token>   # 需鉴权接口必填
```

### 4.3 统一响应格式

**成功**：
```json
{
  "code": "00000",
  "message": "成功",
  "data": { }
}
```

**失败**：
```json
{
  "code": "M1001",
  "message": "参数错误",
  "data": null
}
```

---

## 5. 接口详情

### 一、板块管理 — 公开（1 个）

#### 1. 获取启用的板块列表

`GET /api/boards`

| 项 | 说明 |
|----|------|
| 权限 | 公开，无需 Token |
| 用途 | M1 创建项目时选择板块；M4 创建项目时会校验 boardId 有效 |
| 过滤 | 仅返回 `status=active` 的板块 |

**响应 data 示例**：
```json
[
  {
    "id": 1,
    "name": "编程开发",
    "status": "active",
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

### 二、板块管理 — 管理端（5 个）

前缀：`/api/admin/boards`，均需管理员 Token。

#### 2. 创建板块

`POST /api/admin/boards`

**Body**：`{ "name": "新板块名称" }`

**错误**：M1001（名称为空/重复）

#### 3. 更新板块

`PUT /api/admin/boards/{id}`

**Body**：
```json
{
  "name": "更新后的名称",
  "status": "active"
}
```

`status` 可选值：`active` / `inactive`

**错误**：M1002（不存在）、M1001（名称重复/无效状态）

#### 4. 删除板块

`DELETE /api/admin/boards/{id}`

**说明**：仅当该板块下**无项目引用**时可删除。

**错误**：M1002（不存在）、M1003（有项目引用）

#### 5. 获取板块详情

`GET /api/admin/boards/{id}`

**错误**：M1002

#### 6. 获取所有板块列表

`GET /api/admin/boards`

**说明**：包含 `active` 和 `inactive` 全部状态。

---

### 三、举报管理 — 用户端（2 个）

前缀：`/api/reports`，均需用户 Token。

#### 7. 提交举报

`POST /api/reports`

**Body**：
```json
{
  "targetType": "user",
  "targetId": 100,
  "reason": "该用户存在违规行为"
}
```

| 字段 | 约束 |
|------|------|
| targetType | 必填，`user` 或 `project` |
| targetId | 必填 |
| reason | 必填，1–500 字 |

**错误**：M1001（参数校验失败）

#### 8. 获取我的举报列表

`GET /api/reports/my`

**响应**：当前用户的举报列表，按 `createdAt` 倒序。

**Report 对象主要字段**：`id`, `reporterId`, `targetType`, `targetId`, `reason`, `status`（pending/resolved/dismissed）, `handlerId`, `handleResult`, `handledAt`, `createdAt`

---

### 四、举报管理 — 管理端（3 个）

前缀：`/api/admin/reports`，均需管理员 Token。

#### 9. 获取举报列表

`GET /api/admin/reports?status=pending`

**查询参数**：`status`（可选，`pending` / `resolved` / `dismissed`）

#### 10. 获取举报详情

`GET /api/admin/reports/{id}`

**错误**：M1002

#### 11. 处理举报

`PUT /api/admin/reports/{id}/handle`

**Body（仅驳回，不处罚）**：
```json
{
  "status": "dismissed",
  "handleResult": "经核实，举报不成立"
}
```

**Body（处理并联动处罚）**：
```json
{
  "status": "resolved",
  "handleResult": "经核实，确认违规",
  "createPenalty": true,
  "penaltyType": "credit_deduct",
  "creditDeductValue": 10,
  "penaltyReason": "违规行为处罚"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| status | 是 | `resolved`（已处理）/ `dismissed`（已驳回） |
| handleResult | 否 | 处理说明，最长 500 字 |
| createPenalty | 否 | 是否同时创建处罚，默认 `false` |
| penaltyType | 条件 | `createPenalty=true` 时必填：`credit_deduct` / `function_limit` |
| creditDeductValue | 条件 | `penaltyType=credit_deduct` 时必填且 > 0 |
| penaltyReason | 否 | 处罚原因，缺省使用举报 reason |

**联动规则**：
- 仅当 `status=resolved` 且 `createPenalty=true` 时，**同一事务**内创建处罚并关联 `related_report_id`
- 仅 `targetType=user` 的举报可联动；`project` 举报需另行调用 `POST /admin/penalties`

**错误**：M1002、M1003（已处理）、M1001（联动参数不合法）

---

### 五、处罚管理 — 用户端（2 个）

前缀：`/api/penalties`，均需用户 Token。用户端**仅查询**，不可创建/撤销。

#### 12. 获取我的处罚记录

`GET /api/penalties/my`

**响应**：全部处罚（含已撤销），按时间倒序。

#### 13. 获取我生效中的处罚

`GET /api/penalties/my/active`

**响应**：仅 `status=active` 的记录。

**Penalty 对象主要字段**：`id`, `userId`, `type`, `creditDeductValue`, `reason`, `adminId`, `relatedReportId`, `status`（active/revoked）, `revokedAt`, `createdAt`

---

### 六、处罚管理 — 管理端（4 个）

前缀：`/api/admin/penalties`，均需管理员 Token。

> 注意：处罚**无更新接口**，仅支持创建、查询、撤销。

#### 14. 创建处罚

`POST /api/admin/penalties`

**Body**：
```json
{
  "userId": 100,
  "type": "credit_deduct",
  "creditDeductValue": 10,
  "reason": "违规操作",
  "relatedReportId": 50
}
```

| type | 副作用 |
|------|--------|
| `credit_deduct` | 写 `credit_change`（change_type=penalty），扣 `user.credit_score` |
| `function_limit` | 设 `user.status=banned`，不写 credit_change |

| 字段 | 必填 | 说明 |
|------|------|------|
| userId | 是 | 被处罚用户 |
| type | 是 | `credit_deduct` / `function_limit` |
| creditDeductValue | type=credit_deduct 时 | 正整数 |
| reason | 是 | 处罚原因 |
| relatedReportId | 否 | 关联举报 ID |

#### 15. 获取处罚列表

`GET /api/admin/penalties?status=active&type=credit_deduct`

**查询参数**：`status`（active/revoked）、`type`（credit_deduct/function_limit），均可选。

#### 16. 获取处罚详情

`GET /api/admin/penalties/{id}`

#### 17. 撤销处罚

`PUT /api/admin/penalties/{id}/revoke`

**Body**：`{ "reason": "申诉通过，撤销处罚" }`

| type | 撤销副作用 |
|------|-----------|
| `credit_deduct` | 写 `penalty_restore` 流水，恢复 credit_score |
| `function_limit` | 若无其他 active 的 function_limit，则 `user.status=active` |

**错误**：M1002、M1003（已撤销）

---

### 七、申诉管理 — 用户端（3 个）

前缀：`/api/appeals`，均需用户 Token。

#### 18. 提交申诉

`POST /api/appeals`

**Body（对处罚申诉）**：
```json
{
  "targetType": "penalty",
  "targetId": 60,
  "reason": "处罚过重，请求复核"
}
```

**Body（对互评申诉）**：
```json
{
  "targetType": "evaluation",
  "targetId": 42,
  "reason": "评价内容与事实不符"
}
```

| 规则 | 说明 |
|------|------|
| targetType | `evaluation`（必须是该评价的被评价人）/ `penalty`（必须是被处罚人） |
| evaluation 状态 | **仅 `status=normal` 可申诉**；`pending_review` 不开放（等管理员复核）；`voided`/`kept_no_credit` 不可申诉 |
| 唯一性 | 同一用户对同一目标**终身仅一条申诉**（含已驳回，与 `uk_appeal_target_user` 一致） |
| 已撤销处罚 | 不可对 `revoked` 处罚申诉 |

**错误**：M1001、M1003（重复申诉 / 已撤销处罚）

#### 19. 申诉页可申诉评价列表

`GET /api/appeals/appealable/evaluations`

| 规则 | 说明 |
|------|------|
| 数据范围 | 仅 `evaluation.status=normal` 且 `target_id=当前用户` |
| 排除项 | 已对同一评价提交过申诉（含 pending/approved/rejected） |
| 附加字段 | `projectTitle`（避免前端再调项目详情） |

**响应 data 示例**：
```json
[
  {
    "evaluationId": 42,
    "projectId": 100,
    "projectTitle": "后端实训",
    "communicationScore": 2,
    "taskScore": 2,
    "skillScore": 2,
    "responsibilityScore": 2,
    "averageScore": 2.0,
    "comment": "沟通不足",
    "createdAt": "2026-06-01T10:00:00"
  }
]
```

#### 20. 申诉页可申诉处罚列表

`GET /api/appeals/appealable/penalties`

| 规则 | 说明 |
|------|------|
| 数据范围 | 仅 `penalty.status=active` 且 `user_id=当前用户` |
| 排除项 | 已对同一处罚提交过申诉 |

**响应 data 示例**：
```json
[
  {
    "penaltyId": 60,
    "type": "credit_deduct",
    "creditDeductValue": 10,
    "reason": "违规操作",
    "status": "active",
    "createdAt": "2026-06-01T10:00:00"
  }
]
```

#### 21. 获取我的申诉列表

`GET /api/appeals/my`

#### 22. 获取申诉详情

`GET /api/appeals/{id}`

**说明**：仅可查看**本人**提交的申诉。

**错误**：M1002（不存在）、M1004（非本人）

---

### 八、申诉管理 — 管理端（3 个）

前缀：`/api/admin/appeals`，均需管理员 Token。

#### 23. 获取申诉列表

`GET /api/admin/appeals?status=pending&targetType=penalty`

**查询参数**：
- `status`：pending / approved / rejected
- `targetType`：evaluation / penalty

#### 24. 获取申诉详情

`GET /api/admin/appeals/{id}`

#### 25. 处理申诉

`PUT /api/admin/appeals/{id}/handle`

**Body**：
```json
{
  "status": "approved",
  "handleResult": "经审核，同意撤销处罚"
}
```

| status | 说明 |
|--------|------|
| `approved` | 批准，触发下方副作用 |
| `rejected` | 驳回，仅更新申诉状态 |

**批准副作用**（同一事务）：

| targetType | 操作 |
|------------|------|
| `evaluation` | 调用 M5 `AppealRestoreService`，写 `appeal_restore` 恢复信誉分；原流水 effective=0 时跳过 |
| `penalty` | 自动调用 `revokePenalty` 撤销处罚 |

**错误**：M1002、M1003（已处理）

---

### 九、评价复核 — 管理端（2 个）

前缀：`/api/admin/evaluations`，均需管理员 Token。

> HTTP 入口在 M6 包，业务逻辑委托 M5-6 `EvaluationReviewService`。

#### 26. 获取待复核评价列表

`GET /api/admin/evaluations/pending?projectId=100`

**查询参数**：`projectId`（可选）

**说明**：
- 仅返回 `status=pending_review`
- 管理端返回 `evaluatorId`（评价人 ID），**与用户端匿名展示不同**

**响应 data 示例**：
```json
[
  {
    "evaluationId": 1,
    "projectId": 100,
    "evaluatorId": 10,
    "targetId": 20,
    "communicationScore": 2,
    "taskScore": 2,
    "skillScore": 1,
    "responsibilityScore": 2,
    "averageScore": 1.75,
    "comment": "配合度低",
    "status": "pending_review",
    "createdAt": "2026-05-01T10:00:00"
  }
]
```

#### 27. 执行评价复核

`POST /api/admin/evaluations/{id}/review`

**Body**：
```json
{
  "action": "approve",
  "reviewNote": "复核通过，恢复计分"
}
```

| action | evaluation.status | credit_change.effective | credit_score |
|--------|-------------------|-------------------------|--------------|
| `approve` | normal | 0 → 1 | 加回扣分 |
| `void` | voided | 保持 0 | 不变 |
| `keep_no_credit` | kept_no_credit | 保持 0 | 不变 |

**注意**：`reviewerId` 从管理员 Token 注入，**不要**放在 Body 中。

**错误**：M5005、M5006、M5007、M5008、M1003

---

## 6. M2 管理后台页面映射

| 后台页面 | 接口 | 联调要点 |
|----------|------|----------|
| 板块管理 | #1–#6 | 公开列表 + 管理端增删改查；删除前确认无项目 |
| 举报处理 | #9–#11 | 列表按 status 筛选；处理表单需支持 `createPenalty` 联动 |
| 处罚管理 | #14–#17 | 创建 / 列表 / 详情 / 撤销（无编辑） |
| 申诉审批 | #21–#23 | 按 targetType 区分 evaluation / penalty |
| 评价复核 | #24–#25 | 三种 action；列表展示 evaluatorId；**与 pending 申诉无并发（用户不可对 pending_review 申诉）** |

**登录流程**：`POST /api/auth/login/password` → 取 `data.token` → 所有管理端请求带 `Authorization: Bearer {token}`

---

## 7. Apifox 联调建议

项目未集成 Swagger，推荐使用 **Apifox**（团队已配置 Apifox Helper）。

### 7.1 环境变量

| 变量 | 值 |
|------|-----|
| baseUrl | `http://localhost:8080/api` |
| adminToken | 管理员登录后填入 |
| userToken | 用户 Mock 登录后填入 |

### 7.2 推荐测试顺序

1. **拿 Token**
   - `POST {{baseUrl}}/auth/login/password` → 保存 adminToken
   - `POST {{baseUrl}}/auth/login` → 保存 userToken

2. **管理端冒烟**（adminToken）
   - `GET /admin/boards`
   - `GET /admin/reports?status=pending`
   - `GET /admin/penalties`
   - `GET /admin/appeals?status=pending`
   - `GET /admin/evaluations/pending`

3. **用户端冒烟**（userToken）
   - `GET /boards`（可不带头）
   - `GET /penalties/my`

4. **治理闭环**（最有价值）
   ```
   用户 POST /reports
     → 管理员 PUT /admin/reports/{id}/handle（createPenalty=true）
     → 用户 GET /penalties/my/active
     → 用户 POST /appeals（targetType=penalty）
     → 管理员 PUT /admin/appeals/{id}/handle（approved）
     → 用户 GET /penalties/my/active（应为空）
   ```

---

## 8. 已知缺口

| 项 | 说明 | 状态 |
|----|------|------|
| D-11/D-13 复核↔申诉级联 | P0 因 pending_review 不开放申诉，**无需实现**（已与 M5 对齐） |
| 管理端信誉流水审计 | 用户端已有 M5 `GET /m5/credit/changes` | 管理端 API 未做 |
| 信誉分下限 | 当前可扣至负值 | 待全组确认 |

---

## 9. 错误码

| 错误码 | 含义 | 常见场景 |
|--------|------|----------|
| 00000 | 成功 | — |
| M1001 | 参数错误 | 校验失败、联动参数缺失 |
| M1002 | 资源不存在 | id 无效 |
| M1003 | 状态冲突 | 重复处理、重复申诉、板块有项目引用 |
| M1004 | 无权访问 | 查看他人申诉 |
| M3000 | 未授权 | Token 缺失/无效/过期 |
| M3009 | 需要管理员权限 | 普通用户访问 /admin/* |
| M5005 | 评价不存在 | 复核目标无效 |
| M5006 | 信誉流水不存在 | 复核时找不到挂起流水 |
| M5007 | 无效复核 action | action 不是三种合法值 |
| M5008 | 复核备注过长 | reviewNote 超限 |
| M9999 | 未知错误 | 未捕获异常 |

---

## 10. 权限汇总

| 角色 | 可访问接口 |
|------|-----------|
| 公开 | #1 获取启用板块 |
| 登录用户 | #7–#8 举报；#12–#13 查处罚；#18–#20 申诉 |
| 管理员 | #2–#6 板块；#9–#11 举报；#14–#17 处罚；#21–#23 申诉；#24–#25 评价复核 |
