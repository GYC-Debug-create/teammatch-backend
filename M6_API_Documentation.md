# M6 模块（治理/举报/处罚）API 文档

## 概述

M6 模块负责 TeamMatch 平台的治理功能：板块、举报、处罚、申诉（评价复核见 M5-9）。

---

## 技术栈

- **Spring Boot 3.2.5**
- **MyBatis-Plus 3.5.5**
- **JWT (io.jsonwebtoken 0.11.5)**
- **MySQL 8.0**

---

## 基础配置

### 服务地址
- **Base URL**: `http://localhost:8080/api`
- **Context Path**: `/api`（已在 application.yml 中配置）

### 公共请求头
```
Content-Type: application/json
Authorization: Bearer <JWT Token>  (需要认证的接口)
```

---

## 接口列表

### 一、板块管理 — 公开接口 (`/boards`)

#### 1. 获取启用的板块列表
**接口**: `GET /api/boards`

**说明**: 获取所有状态为 active 的板块列表，供项目创建时选择板块使用。

**权限**: 公开（无需认证）

**请求参数**: 无

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "编程开发",
      "status": "active",
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
}
```

---

### 二、板块管理 — 管理端 (`/admin/boards`)

#### 2. 创建板块
**接口**: `POST /api/admin/boards`

**权限**: 管理员

**请求 Body**:
```json
{
  "name": "新板块名称"
}
```

**响应**: 创建的板块对象

**错误**: `PARAM_ERROR`（名称重复、名称为空）

#### 3. 更新板块
**接口**: `PUT /api/admin/boards/{id}`

**权限**: 管理员

**请求 Body**:
```json
{
  "name": "更新后的名称",
  "status": "active"
}
```

**错误**: `NOT_FOUND`（板块不存在）、`PARAM_ERROR`（名称重复/为空、无效状态）

#### 4. 删除板块
**接口**: `DELETE /api/admin/boards/{id}`

**权限**: 管理员

**说明**: 仅允许无项目引用时删除。

**错误**: `NOT_FOUND`（板块不存在）、`STATUS_CONFLICT`（有项目引用，无法删除）

#### 5. 获取板块详情
**接口**: `GET /api/admin/boards/{id}`

**权限**: 管理员

**错误**: `NOT_FOUND`

#### 6. 获取所有板块列表（管理端）
**接口**: `GET /api/admin/boards`

**权限**: 管理员

**说明**: 包含所有状态的板块。

---

### 三、举报管理 — 用户端 (`/reports`)

#### 7. 提交举报
**接口**: `POST /api/reports`

**权限**: 已登录用户

**请求 Body**:
```json
{
  "targetType": "user",
  "targetId": 100,
  "reason": "该用户存在违规行为"
}
```

**说明**: targetType 支持 `user`（用户）、`project`（项目）。

#### 8. 获取我的举报列表
**接口**: `GET /api/reports/my`

**权限**: 已登录用户

**响应**: 举报记录列表，按时间倒序

---

### 四、举报管理 — 管理端 (`/admin/reports`)

#### 9. 获取举报列表
**接口**: `GET /api/admin/reports?status=pending`

**权限**: 管理员

**查询参数**: `status`（可选，pending/resolved/dismissed）

#### 10. 获取举报详情
**接口**: `GET /api/admin/reports/{id}`

**权限**: 管理员

#### 11. 处理举报
**接口**: `PUT /api/admin/reports/{id}/handle`

**权限**: 管理员

**请求 Body**:
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

**字段说明**:
| 字段 | 必填 | 说明 |
|------|------|------|
| status | 是 | 处理结果：resolved（已处理）/ dismissed（已驳回） |
| handleResult | 否 | 处理说明 |
| createPenalty | 否 | 是否同时创建处罚（默认 false） |
| penaltyType | 条件必填 | createPenalty=true 时必填：credit_deduct / function_limit |
| creditDeductValue | 条件必填 | penaltyType=credit_deduct 时必填且 > 0 |
| penaltyReason | 否 | 处罚原因（默认使用举报原因） |

**处理流程**: 当 `createPenalty=true` 且 `status=resolved` 时，在同一事务内创建处罚（`related_report_id` 关联本举报）。

**限制**: 仅 `target_type=user` 的举报可联动处罚；`project` 举报请单独走处罚接口。

---

### 五、处罚管理 — 用户端 (`/penalties`)

#### 12. 获取我的处罚记录列表
**接口**: `GET /api/penalties/my`

**权限**: 已登录用户

**响应**: 该用户的所有处罚记录，按时间倒序

#### 13. 获取我生效中的处罚记录
**接口**: `GET /api/penalties/my/active`

**权限**: 已登录用户

**说明**: 仅返回 status=active 的处罚（未被撤销的）。

---

### 六、处罚管理 — 管理端 (`/admin/penalties`)

#### 14. 创建处罚
**接口**: `POST /api/admin/penalties`

**权限**: 管理员

**请求 Body**:
```json
{
  "userId": 100,
  "type": "credit_deduct",
  "creditDeductValue": 10,
  "reason": "违规操作",
  "relatedReportId": 50
}
```

**处罚类型**:
| 类型 | 说明 | 副作用 |
|------|------|--------|
| credit_deduct | 扣信誉分 | 写入 credit_change 扣分流水，更新 user.credit_score |
| function_limit | 功能限制（封号） | 设置 user.status=banned |

**字段说明**:
| 字段 | 必填 | 说明 |
|------|------|------|
| userId | 是 | 被处罚用户ID |
| type | 是 | 处罚类型：credit_deduct / function_limit |
| creditDeductValue | type=credit_deduct 时必填 | 扣分值（大于0） |
| reason | 是 | 处罚原因 |
| relatedReportId | 否 | 关联的举报ID |

#### 15. 获取处罚列表
**接口**: `GET /api/admin/penalties?status=active&type=credit_deduct`

**权限**: 管理员

**查询参数**: `status`（可选，active/revoked）、`type`（可选，credit_deduct/function_limit）

#### 16. 获取处罚详情
**接口**: `GET /api/admin/penalties/{id}`

**权限**: 管理员

#### 17. 撤销处罚
**接口**: `PUT /api/admin/penalties/{id}/revoke`

**权限**: 管理员

**请求 Body**:
```json
{
  "reason": "申诉通过，撤销处罚"
}
```

**撤销副作用**:
| 处罚类型 | 副作用 |
|---------|--------|
| credit_deduct | 写入 penalty_restore 恢复流水，恢复 user.credit_score |
| function_limit | 检查是否还有其他 active 的 function_limit，无则解封用户 |

---

### 七、申诉管理 — 用户端 (`/appeals`)

#### 18. 提交申诉
**接口**: `POST /api/appeals`

**权限**: 已登录用户

**请求 Body**:
```json
{
  "targetType": "penalty",
  "targetId": 60,
  "reason": "处罚过重"
}
```

**说明**: targetType 支持 `evaluation`（评价）、`penalty`（处罚）
- 只能对自己的评价/处罚提交申诉
- 同一目标不能存在待处理的申诉
- 已撤销的处罚不能申诉

#### 19. 获取我的申诉列表
**接口**: `GET /api/appeals/my`

**权限**: 已登录用户

#### 20. 获取申诉详情
**接口**: `GET /api/appeals/{id}`

**权限**: 已登录用户（仅限自己的申诉）

---

### 八、申诉管理 — 管理端 (`/admin/appeals`)

#### 21. 获取申诉列表
**接口**: `GET /api/admin/appeals?status=pending&targetType=penalty`

**权限**: 管理员

**查询参数**: `status`（可选，pending/approved/rejected）、`targetType`（可选，evaluation/penalty）

#### 22. 获取申诉详情
**接口**: `GET /api/admin/appeals/{id}`

**权限**: 管理员

#### 23. 处理申诉
**接口**: `PUT /api/admin/appeals/{id}/handle`

**权限**: 管理员

**请求 Body**:
```json
{
  "status": "approved",
  "handleResult": "经审核，同意撤销处罚"
}
```

**字段说明**:
| 字段 | 必填 | 说明 |
|------|------|------|
| status | 是 | 处理结果：approved（批准）/ rejected（驳回） |
| handleResult | 否 | 处理说明 |

**处理副作用**:
| 申诉目标类型 | 批准后的操作 |
|-------------|------------|
| evaluation | 调用 M5 `AppealRestoreService` 写入 `appeal_restore` 并回写信誉分（原流水 effective=0 时跳过） |
| penalty | 自动调用 revokePenalty 撤销处罚 |

---

### 九、评价复核 — 管理端（M5-9，待队友合入）

以下接口由 M5 Gerrit **b05cea7** 提供，**当前仓库尚未包含**，合入后 M2 联调以此为准：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/evaluations/pending?projectId=` | 待复核列表（`PendingEvaluationVO`，含 `evaluatorId`） |
| POST | `/api/admin/evaluations/{id}/review` | 复核，`body`: `{ "action", "reviewNote" }` |

**action**: `approve` | `void` | `keep_no_credit`

---

## 错误码说明

| 错误码 | 原因 |
|--------|------|
| M1001 | 参数错误 |
| M1002 | 资源不存在 |
| M1003 | 状态冲突（如重复操作、已处理） |
| M1004 | 无权访问该资源 |
| M3000 | 未授权，需重新登录 |
| M3009 | 需要管理员权限 |
| M9999 | 未知错误 |

---

## 权限汇总

| 角色 | 可访问的接口 |
|------|------------|
| 公开（无需认证） | 获取启用的板块列表 |
| 已登录用户 | 提交举报/申诉、查看自己的举报/申诉/处罚记录 |
| 管理员 | 板块 CRUD、举报处理、处罚创建/撤销、申诉处理、所有查询；评价复核见 M5-9 |