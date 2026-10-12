# TeamMatch Backend

TeamMatch 校园组队与互评平台后端服务

## 技术栈

- Java 17
- Spring Boot 3.2.5（Spring Web、Validation、Data Redis、Mail）
- MyBatis-Plus 3.5.5（MySQL Connector/J 8.0.33）
- MySQL 8.0
- Redis 7.x
- JJWT 0.11.5（JWT 鉴权）
- Spring Security Crypto（BCrypt 密码哈希）
- 阿里云 OSS Java SDK 3.17.4（文件存储）
- Lombok
- Maven（依赖与构建见 `pom.xml`）

## 项目结构

```
teammatch-backend/
├── src/main/java/com/teammatch/
│   ├── TeamMatchApplication.java    # 启动类
│   ├── common/                      # 通用工具、常量、异常
│   ├── config/                      # 配置类
│   ├── controller/                  # 控制器层
│   ├── service/                     # 业务逻辑层
│   ├── mapper/                      # 数据访问层
│   ├── entity/                      # 实体类
│   ├── dto/                         # 数据传输对象
│   ├── util/                        # 工具类（如 AuthUtil）
│   ├── m4/                          # M4 模块：项目管理、组队与退出
│   └── m6/                          # M6 模块：治理、举报与处罚
├── src/main/resources/
│   ├── application.yml              # 主配置文件（敏感项通过环境变量注入）
│   ├── mapper/                      # MyBatis XML 映射文件
│   └── sql/                         # SQL 脚本
├── src/test/                        # 测试代码
├── sql/                             # 数据库脚本（建表、迁移、示例数据）
└── docs/                            # 部署、测试与设计文档
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.x+

### 2. 初始化数据库

先创建数据库：

```sql
CREATE DATABASE teammatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

在 `teammatch` 库中执行 `sql/schema.sql` 初始化表结构。如需示例数据，可执行 `sql/rebuild_database.sql`。注意：该脚本会先执行 `DROP DATABASE IF EXISTS teammatch`，会清空已有数据，仅用于首次初始化或重置。

### 3. 配置

敏感配置（数据库与 Redis 密码、邮箱授权码、OSS 与第三方应用凭证等）不写入仓库，统一通过环境变量注入。变量名与默认值见 `src/main/resources/application.yml`，常用变量如下（凭证处为占位符，请替换为自己的配置）：

| 用途 | 变量名 | 默认值 / 占位值 |
|------|--------|-----------------|
| 数据库 | `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME` | 默认 `localhost`、`3306`、`teammatch`、`root` |
| 数据库密码 | `DB_PASSWORD` | `<your-db-password>` |
| Redis | `REDIS_HOST`、`REDIS_PORT`、`REDIS_DB` | 默认 `localhost`、`6379`、`0` |
| Redis 密码 | `REDIS_PASSWORD` | `<your-redis-password>`（未设置时留空） |
| 邮件（SMTP） | `MAIL_HOST`、`MAIL_PORT`、`MAIL_USERNAME`、`MAIL_PASSWORD` | 默认 `smtp.qq.com`、`587`；账号与授权码为 `<your-mail-account>`、`<your-smtp-auth-code>` |
| 阿里云 OSS | `OSS_ENDPOINT`、`OSS_BUCKET`、`OSS_BASE_URL`、`OSS_ACCESS_KEY_ID`、`OSS_ACCESS_KEY_SECRET` | 凭证为 `<your-oss-access-key-id>`、`<your-oss-access-key-secret>` |
| Gitee OAuth | `GITEE_CLIENT_ID`、`GITEE_CLIENT_SECRET`、`GITEE_REDIRECT_URI`、`GITEE_PUBLIC_BASE_URL` | 凭证为 `<your-gitee-client-id>`、`<your-gitee-client-secret>` |
| 微信小程序 | `WECHAT_APP_ID`、`WECHAT_SECRET` | 凭证为 `<your-wechat-app-id>`、`<your-wechat-secret>` |
| 服务端口 | `SERVER_PORT` | 默认 `8080` |

也可以在 `src/main/resources/application-local.yml` 中覆盖配置。该文件已被 `.gitignore` 忽略，且 `application.yml` 默认激活 `local` profile，适合放置本机配置。

### 4. 启动应用

```bash
# 使用 Maven
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/teammatch-backend-1.0.0-SNAPSHOT.jar
```

应用默认在 `http://localhost:8080/api` 启动。

### 5. 验证启动

访问健康检查接口：
```bash
curl http://localhost:8080/api/health
```

服务器部署步骤见 `docs/DEPLOY.md`。

## 开发规范

### 分工说明

- **M3**: 板块一（用户认证/档案）+ 板块二（冷启动）
- **M4**: 板块三（项目管理）+ 板块四（组队/退出）
- **M5**: 板块五（互评/信誉/申诉）
- **M6**: 板块六（治理/举报/处罚）

### 代码规范

1. 遵循阿里巴巴 Java 开发手册
2. 使用 Lombok 简化代码
3. 统一使用 MyBatis-Plus 进行数据访问
4. 关键业务逻辑必须添加事务注解 `@Transactional`
5. 并发保护使用 `SELECT ... FOR UPDATE`

### 测试要求

- 单元测试：信誉分计算、退出结果计算、项目级封顶
- 接口测试：状态流转、资格校验、错误码、幂等性
- 集成测试：完整业务闭环

测试的执行方式见 `docs/TESTING.md`。

## 接口文档

- Base URL: `http://localhost:8080/api`
- 管理端: `http://localhost:8080/api/admin`
- 鉴权方式: `Authorization: Bearer {token}`

详细接口文档参见根目录 `M3_API_Documentation.md`、`M4_API_Documentation.md`、`M6_API_Documentation.md`；系统设计见 `docs/design/system-design-v2.1.md`。

## 注意事项

⚠️ **开发前必读**

1. 先阅读 `docs/design/system-design-v2.1.md`
2. 了解全局冻结基线（第 8 章）
3. 涉及状态机、退出机制、互评规则、信誉流水的改动必须全组对齐
4. 不要绕开 `credit_change` 直接修改信誉分
5. P0 不做软删除，不加 `is_deleted` 字段
6. 提交前必须自测相关链路

## 提交规范

使用 Gerrit 提交代码：

```bash
git add .
git commit -m "feat: 功能描述"
git push origin HEAD:refs/for/master
```

提交说明必须写清影响范围，涉及接口、数据库、状态机、事务的改动需单独标注。

## 仓库说明

- 本仓库为团队共同开发项目的公开副本。
- 历史提交中的密钥已替换为 `<REDACTED>`，提交者邮箱已匿名化。
- 如需完整开发记录，请联系团队成员。
