# M3 模块（用户认证与档案管理）API 文档与使用说明

## 概述

M3 模块负责 TeamMatch 平台的用户认证和个人档案管理功能。本模块提供基于微信 Mock 登录、邮箱验证、密码管理、用户名绑定以及个人档案维护等核心功能。

---

## 技术栈

- **Spring Boot 3.2.5**
- **MyBatis-Plus 3.5.5**
- **JWT (io.jsonwebtoken 0.11.5)**
- **Redis (邮箱验证码存储)**
- **BCrypt (密码加密)**
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

### 一、认证模块 (`/auth`)

#### 1. Mock 微信登录
**接口**: `POST /api/auth/login`

**说明**: 模拟微信登录，使用 code 作为 openid 识别用户。若用户不存在则自动创建。

**请求 Body**:
```json
{
  "code": "test_openid_123"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": {
    "id": 1,
    "nickname": "User_test_",
    "avatarUrl": null,
    "emailVerified": false,
    "formalProfileCompleted": false,
    "creditScore": 100,
    "token": "<REDACTED>"
  }
}
```

**注意**:
- 返回的是 `LoginResponse` DTO，不包含敏感字段（passwordHash、role、status等）
- `formalProfileCompleted` 表示正式档案是否完成（邮箱验证 + 昵称 + 学校）

---

#### 2. 发送邮箱验证码
**接口**: `POST /api/auth/email/send`

**说明**: 向指定邮箱发送验证码（当前为控制台打印，实际项目需接入邮件服务）。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "email": "student@example.edu.cn"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

---

#### 3. 验证邮箱验证码
**接口**: `POST /api/auth/email/verify`

**说明**: 验证邮箱验证码并绑定邮箱到当前用户。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "email": "student@example.edu.cn",
  "code": "123456"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": "邮箱验证成功"
}
```

---

#### 4. 管理员创建用户密码
**接口**: `POST /api/auth/password/create`

**说明**: 为新用户设置密码，若用户不存在则自动创建。**需要管理员权限**。

**请求 Headers**:
```
Authorization: Bearer <管理员Token>
```

**请求 Body**:
```json
{
  "username": "zhangsan",
  "password": "<REDACTED>"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- **需要管理员权限**（role=admin），非管理员返回 `M3009` 错误
- 密码长度至少 6 位，否则返回 `M3007` 错误
- 若用户已设置密码，会提示使用修改接口
- 密码使用 BCrypt 加密存储

---

#### 5. 管理员修改用户密码
**接口**: `POST /api/auth/password/update`

**说明**: 修改已有用户的密码，可选择验证旧密码。**需要管理员权限**。

**请求 Headers**:
```
Authorization: Bearer <管理员Token>
```

**请求 Body**:
```json
{
  "username": "zhangsan",
  "oldPassword": "<REDACTED>",
  "newPassword": "<REDACTED>"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- **需要管理员权限**（role=admin），非管理员返回 `M3009` 错误
- 若提供 `oldPassword`，会先校验旧密码是否正确，错误返回 `M3008`
- 新密码长度至少 6 位，否则返回 `M3007` 错误

---

#### 6. 用户首次绑定用户名
**接口**: `POST /api/auth/username/bind`

**说明**: 微信登录用户首次绑定用户名（之前未设置过 username）。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "username": "zhangsan"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- 用户名不能为空
- 若已绑定过用户名，返回 `M3006` 错误，提示使用修改接口
- 检查用户名是否被其他用户占用，占用返回 `M3005` 错误

---

#### 7. 用户修改用户名
**接口**: `PUT /api/auth/username/update`

**说明**: 已绑定用户名的用户修改自己的用户名。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "username": "new_username"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

---

#### 8. 密码登录
**接口**: `POST /api/auth/login/password`

**说明**: 使用用户名和密码进行登录。

**请求 Body**:
```json
{
  "username": "zhangsan",
  "password": "<REDACTED>"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": {
    "id": 1,
    "nickname": "张三",
    "avatarUrl": null,
    "emailVerified": true,
    "formalProfileCompleted": true,
    "creditScore": 100,
    "token": "<REDACTED>"
  }
}
```

**业务规则**:
- 用户名不能为空，否则返回 `M3011` 错误
- 若用户未设置密码，返回 `M3014` 错误
- 密码错误返回 `M3013` 错误
- 登录成功后返回 `LoginResponse` DTO

---

#### 9. 用户修改密码
**接口**: `PUT /api/auth/password/change`

**说明**: 用户自主修改自己的密码，需要验证旧密码。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "oldPassword": "<REDACTED>",
  "newPassword": "<REDACTED>"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- 必须提供 Token，验证用户身份
- 旧密码不正确返回 `M3008` 错误
- 新密码长度至少 6 位，否则返回 `M3007` 错误
- 新密码与旧密码不能相同

---

### 二、个人档案模块 (`/profile`)

#### 1. 更新个人档案
**接口**: `PUT /api/profile/update`

**说明**: 更新用户的个人信息。当邮箱已验证且填写了昵称和学校时，`formalProfileCompleted` 自动变为 `true`。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "nickname": "张三",
  "avatarUrl": "https://example.com/avatar.jpg",
  "school": "清华大学",
  "major": "计算机科学与技术",
  "grade": "2023级",
  "bio": "热爱编程，寻找队友"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

---

#### 2. 绑定 GitHub 账号
**接口**: `POST /api/profile/github/bind`

**说明**: 绑定用户的 GitHub 用户名。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "githubUsername": "your-github-name"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

---

#### 3. 更新 GitHub 账号
**接口**: `PUT /api/profile/github/update`

**说明**: 更新已绑定的 GitHub 用户名。仅当用户已经绑定过 GitHub 账号时可修改。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "githubUsername": "new-github-name"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- 若用户未绑定过 GitHub，返回 `M3012` 错误
- 必须先调用 `POST /api/profile/github/bind` 完成首次绑定

---

#### 4. 全量替换用户技能（推荐）
**接口**: `PUT /api/profile/skills`

**说明**: 全量替换用户的技能标签列表。先删除旧技能，再插入新技能。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "skillTagIds": [1, 2, 3]
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- 传入空数组 `[]` 表示清空所有技能
- 所有 skillTagId 必须在 `skill_tag` 表中存在且状态为 `active`
- 若技能标签不存在或已禁用，返回 `M3010` 错误

---

#### 5. 获取技能标签列表
**接口**: `GET /api/profile/skills/tags`

**说明**: 获取所有激活状态的技能标签列表，供前端展示可选技能。

**请求**: 无需 Token（公开接口）

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": [
    {
      "id": 1,
      "name": "Java",
      "category": "language",
      "status": "active"
    },
    {
      "id": 2,
      "name": "Spring Boot",
      "category": "framework",
      "status": "active"
    }
  ]
}
```

**业务规则**:
- 只返回 `status='active'` 的技能标签
- 按 `category` 和 `name` 排序

---

#### 6. 添加技能标签（用户，旧接口）
**接口**: `POST /api/profile/skills/add`

**说明**: 为当前用户增量添加一个技能标签（保留兼容，推荐使用全量替换接口）。

**请求 Headers**:
```
Authorization: Bearer <Token>
```

**请求 Body**:
```json
{
  "skillTagId": 1
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- 技能标签 ID 必须在 `skill_tag` 表中存在
- 避免重复添加同一技能标签
- 数据会插入到 `user_skill` 关联表

---

#### 7. 添加技能标签（管理员）
**接口**: `POST /api/profile/skills/tag/add`

**说明**: 向数据库插入新的技能标签，供后续用户关联使用。**需要管理员权限**。

**请求 Headers**:
```
Authorization: Bearer <管理员Token>
```

**请求 Body**:
```json
{
  "name": "Java",
  "category": "编程语言"
}
```

**响应**:
```json
{
  "code": "00000",
  "message": "成功",
  "data": null
}
```

**业务规则**:
- **需要管理员权限**（role=admin），非管理员返回 `M3009` 错误
- 名称不能为空
- 状态自动设为 `active`
- 插入到 `skill_tag` 表
- 若同名技能标签已存在，返回 `M3015` 错误

---
#### 8. 获取用户完整档案
**接口**: `GET /api/profile/detail`

**说明**: 获取当前用户的档案。返回 `ProfileDetailVO`（VO），排除 `openid`、`passwordHash` 等敏感字段。

**请求**: 需 Token

**响应**:
```json
{
    "code": "00000",
    "message": "成功",
    "data": {
        "id": 1,
        "nickname": "系统管理员",
        "avatarUrl": null,
        "email": "admin@teammatch.edu.cn",
        "emailVerified": true,
        "school": "TeamMatch大学",
        "major": null,
        "grade": null,
        "bio": null,
        "githubUsername": null,
        "githubClaimed": false,
        "formalProfileCompleted": true,
        "creditScore": 100,
        "role": "admin",
        "status": "active",
        "username": "admin",
        "createdAt": "2026-05-25T20:13:15",
        "updatedAt": "2026-05-25T20:13:15"
    },
    "fail": false,
    "success": true
}
```

---
#### 9. 获取指定用户的档案详情（供项目详情页显示用户名称用）
**接口**: `GET /api/profile/detail/{userId}`

**说明**: 根据 userId 获取任意用户的档案信息（不暴露 openid、passwordHash 等敏感字段）。
用于项目详情页等场景，显示队长/成员的昵称和头像。

**请求**:
```
需要 Token（任意已登录用户均可调用）
路径参数: userId - 目标用户的 ID
```

**响应**:
```json
{
    "code": "00000",
    "message": "成功",
    "data": {
        "id": 2,
        "nickname": "目标用户",
        "avatarUrl": "http://avatar.url",
        "email": null,
        "emailVerified": false,
        "school": null,
        "major": null,
        "grade": null,
        "bio": null,
        "githubUsername": null,
        "githubClaimed": false,
        "formalProfileCompleted": false,
        "creditScore": 100,
        "role": "user",
        "status": "active",
        "username": null,
        "createdAt": null,
        "updatedAt": null
    },
    "fail": false,
    "success": true
}
```

**业务规则**:
- 需要有效的登录 Token，否则返回 `M3000` 错误
- 若指定 userId 不存在，返回 `M1002` 错误
- 返回 `ProfileDetailVO`，不包含 `openid`、`passwordHash` 等敏感字段

## 数据表结构

### user 表（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| openid | VARCHAR | 微信 openid（微信登录用户） |
| username | VARCHAR | 用户名（密码登录用户） |
| nickname | VARCHAR | 用户昵称 |
| avatar_url | VARCHAR | 头像 URL |
| email | VARCHAR | 学校邮箱 |
| email_verified | TINYINT | 邮箱是否已验证 |
| school | VARCHAR | 学校名称 |
| major | VARCHAR | 专业 |
| grade | VARCHAR | 年级 |
| bio | VARCHAR | 个人简介 |
| github_username | VARCHAR | GitHub 用户名 |
| github_claimed | TINYINT | GitHub 是否已认领 |
| formal_profile_completed | TINYINT | 正式档案是否完成 |
| credit_score | INT | 信誉分 |
| role | VARCHAR | 用户角色（user/admin） |
| status | VARCHAR | 用户状态（active/banned） |
| password_hash | VARCHAR | 密码哈希值 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### skill_tag 表（技能标签表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR | 技能名称 |
| category | VARCHAR | 分类 |
| status | VARCHAR | 状态（active/inactive） |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### user_skill 表（用户技能关联表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| skill_tag_id | BIGINT | 技能标签 ID |

---

## 使用流程示例

### 场景一：微信登录用户完整流程

1. **登录获取 Token**
   ```bash
   POST /api/auth/login
   Body: { "code": "test_openid_123" }
   ```

2. **绑定邮箱**
   ```bash
   # 发送验证码
   POST /api/auth/email/send
   Headers: Authorization: Bearer <Token>
   Body: { "email": "student@example.edu.cn" }
   
   # 验证验证码（控制台查看验证码）
   POST /api/auth/email/verify
   Headers: Authorization: Bearer <Token>
   Body: { "email": "student@example.edu.cn", "code": "123456" }
   ```

3. **绑定用户名**
   ```bash
   POST /api/auth/username/bind
   Headers: Authorization: Bearer <Token>
   Body: { "username": "zhangsan" }
   ```

4. **完善个人档案**
   ```bash
   PUT /api/profile/update
   Headers: Authorization: Bearer <Token>
   Body: {
     "nickname": "张三",
     "school": "清华大学",
     "major": "计算机",
     "grade": "2023级",
     "bio": "寻找队友"
   }
   ```

5. **添加技能标签**
   ```bash
   # 先获取所有可用技能标签
   GET /api/profile/skills/tags
   
   # 全量替换用户技能（推荐）
   PUT /api/profile/skills
   Headers: Authorization: Bearer <Token>
   Body: { "skillTagIds": [1, 2, 3] }
   
   # 或者使用旧接口增量添加（不推荐）
   POST /api/profile/skills/add
   Headers: Authorization: Bearer <Token>
   Body: { "skillTagId": 1 }
   ```

6. **绑定 GitHub**
   ```bash
   POST /api/profile/github/bind
   Headers: Authorization: Bearer <Token>
   Body: { "githubUsername": "your-github" }
   ```

---

### 场景二：管理员创建密码登录用户

1. **创建用户密码**
   ```bash
   POST /api/auth/password/create
   Body: { "username": "admin", "password": "<REDACTED>" }
   ```

2. **修改密码（可选）**
   ```bash
   POST /api/auth/password/update
   Body: { 
     "username": "admin", 
     "oldPassword": "<REDACTED>",
     "newPassword": "<REDACTED>" 
   }
   ```

---

## 错误码说明

### 通用错误码
| 错误码 | 说明 |
|--------|------|
| 00000 | 成功 |
| M1001 | 参数错误 |
| M1002 | 资源不存在 |
| M1003 | 状态冲突 |
| M9999 | 未知错误 |

### M3 模块专用错误码
| 错误码 | 说明 | 触发场景 |
|--------|------|----------|
| M3001 | 需要先完成正式档案 | 访问需要正式档案的接口 |
| M3002 | 该邮箱已被其他用户绑定 | 邮箱验证时邮箱已被占用 |
| M3003 | 验证码错误或已过期 | 邮箱验证码不正确 |
| M3004 | 发送验证码过于频繁 | 短时间内多次发送验证码 |
| M3005 | 该用户名已被占用 | 绑定/修改用户名时重复 |
| M3006 | 用户名已绑定，请使用修改接口 | 重复调用 bind 接口 |
| M3007 | 密码长度不能少于6位 | 密码太短 |
| M3008 | 旧密码不正确 | 修改密码时旧密码错误 |
| M3009 | 需要管理员权限 | 非管理员调用管理接口 |
| M3010 | 技能标签不存在或已禁用 | 使用无效的技能ID |
| M3011 | 用户名不能为空 | 密码登录时未提供用户名 |
| M3012 | GitHub 账号未绑定 | 尝试更新未绑定的 GitHub |
| M3013 | 用户名或密码错误 | 密码登录时凭证错误 |
| M3014 | 该用户未设置密码 | 未设置密码的用户尝试密码登录 |
| M3015 | 技能标签已存在 | 添加同名技能标签时重复 |
| M3015 | 技能标签已存在 | 添加同名技能标签时重复 |

---

## 常见问题

### 1. 接口返回 404
- 检查路径是否包含前导 `/`
- 确认 Apifox 开发环境是否配置了前置路径 `/api`
- 重启服务确保路由已加载

### 2. Token 无效
- 确认 Token 格式：`Bearer <token>`（Bearer 后有空格）
- Token 有效期为 7 天，过期需重新登录

### 3. 技能标签添加失败
- 确保 `skill_tag` 表中存在对应的 ID
- 技能标签状态必须为 `active`
- 使用管理员接口先插入技能标签

### 4. 邮箱验证失败
- 检查验证码是否正确（当前在控制台打印）
- 确认邮箱未被其他用户占用

---

## 开发与测试

### 运行测试
```bash
cd backend/shixun12_backend
mvn clean test
```

### 启动服务
```bash
cd backend/shixun12_backend
mvn spring-boot:run
```

### 配置文件
- `application.yml`: 主配置文件
- `application-local.yml`: 本地环境配置（数据库密码等）

---

## 注意事项

1. **Lombok 已启用**：实体类和 DTO 使用 `@Data` 注解自动生成 Getter/Setter
2. **密码加密**：所有密码使用 BCrypt 加密存储，不可逆
3. **JWT 密钥**：当前使用固定密钥，生产环境建议改为环境变量
4. **Redis 依赖**：邮箱验证码存储在 Redis 中，确保 Redis 服务已启动
5. **数据库编码**：JDBC URL 使用 `utf8` 编码，避免 utf8mb4 兼容性问题
6. **管理员权限**：密码管理接口需要 role=admin 的 Token，否则返回 M3009 错误
7. **登录响应**：登录接口返回 `LoginResponse` DTO，不包含敏感字段（passwordHash、role等）
8. **技能标签**：推荐使用全量替换接口 `PUT /api/profile/skills`，旧接口保留兼容

---

## 联系方式

如有问题，请联系 M3 模块负责人。
