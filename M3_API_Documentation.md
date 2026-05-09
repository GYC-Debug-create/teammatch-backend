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
    "token": "<REDACTED>",
    "user": {
      "id": 1,
      "openid": "test_openid_123",
      "nickname": "User_test_",
      "email": null,
      "emailVerified": false,
      "creditScore": 100,
      "role": "user",
      "status": "active"
    }
  },
  "success": true
}
```

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
  "data": null,
  "success": true
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
  "data": "邮箱验证成功",
  "success": true
}
```

---

#### 4. 管理员创建用户密码
**接口**: `POST /api/auth/password/create`

**说明**: 为新用户设置密码，若用户不存在则自动创建。

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
  "data": null,
  "success": true
}
```

**业务规则**:
- 密码长度至少 6 位
- 若用户已设置密码，会提示使用修改接口
- 密码使用 BCrypt 加密存储

---

#### 5. 管理员修改用户密码
**接口**: `POST /api/auth/password/update`

**说明**: 修改已有用户的密码，可选择验证旧密码。

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
  "data": null,
  "success": true
}
```

**业务规则**:
- 若提供 `oldPassword`，会先校验旧密码是否正确
- 新密码长度至少 6 位

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
  "data": null,
  "success": true
}
```

**业务规则**:
- 用户名不能为空
- 若已绑定过用户名，提示使用修改接口
- 检查用户名是否被其他用户占用

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
  "data": null,
  "success": true
}
```

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
  "data": null,
  "success": true
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
  "data": null,
  "success": true
}
```

---

#### 3. 添加技能标签（用户）
**接口**: `POST /api/profile/skills/add`

**说明**: 为当前用户添加一个技能标签。技能标签必须先在 `skill_tag` 表中存在且状态为 `active`。

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
  "data": null,
  "success": true
}
```

**业务规则**:
- 技能标签 ID 必须在 `skill_tag` 表中存在
- 避免重复添加同一技能标签
- 数据会插入到 `user_skill` 关联表

---

#### 4. 添加技能标签（管理员）
**接口**: `POST /api/profile/skills/tag/add`

**说明**: 向数据库插入新的技能标签，供后续用户关联使用。

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
  "data": null,
  "success": true
}
```

**业务规则**:
- 名称不能为空
- 状态自动设为 `active`
- 插入到 `skill_tag` 表

---

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
   # 先添加技能标签到数据库
   POST /api/profile/skills/tag/add
   Body: { "name": "Java", "category": "编程语言" }
   
   # 为用户添加技能
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

| 错误码 | 说明 |
|--------|------|
| 00000 | 成功 |
| 99999 | 未知错误 |
| 401 | 未授权（Token 无效或过期） |
| 404 | 资源不存在 |

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

1. **Lombok 已禁用**：所有实体类和 DTO 均使用手动编写的 Getter/Setter
2. **密码加密**：所有密码使用 BCrypt 加密存储，不可逆
3. **JWT 密钥**：当前使用固定密钥，生产环境建议改为环境变量
4. **Redis 依赖**：邮箱验证码存储在 Redis 中，确保 Redis 服务已启动
5. **数据库编码**：JDBC URL 使用 `utf8` 编码，避免 utf8mb4 兼容性问题

---

## 联系方式

如有问题，请联系 M3 模块负责人。
