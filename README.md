# IT Service Ticket System

一个面向企业内部 IT 服务场景的后端工单管理系统，用于支持员工提交故障工单、
IT 支持人员处理工单以及管理员进行权限管理。

## 目录

- [项目概述](#项目概述)
- [核心功能](#核心功能)
- [主要接口](#主要接口)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [项目结构](#项目结构)
- [快速启动](#快速启动)
- [内置测试账号](#内置测试账号)
- [后续计划](#后续计划)

## 项目概述

开发企业内部 IT 服务工单管理系统，实现员工提交工单、IT 人员处理工单、管理员统一管理用户及权限等业务流程，覆盖工单创建、分配、处理、评论、关闭的完整生命周期，且具备认证授权、权限控制、缓存及容器化部署等能力。

## 核心功能

- **JWT 无状态认证**：基于 Spring Security 过滤器链实现登录与 Token 校验，配合
  Redis 黑名单支持主动登出，被拉黑的 Token 在有效期内无法复用。
- **RBAC 三级权限**：EMPLOYEE、IT_SUPPORT、ADMIN 三个角色，在接口层（URL）
  和数据层（WHERE 条件）分别实现权限控制，防止越权访问。
- **工单状态流转**：工单遵循 NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED
  五个状态，流转时校验目标状态合法性及操作者身份，非法操作直接拦截。
- **缓存与降级**：通过 Spring Cache 对工单和用户查询添加缓存，Redis 不可用时
  自动回源数据库，不影响业务。
- **Docker 部署**：提供 Dockerfile 和 docker-compose.yml，一条命令启动所有依赖
  服务。

## 主要接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/authenticate` | 用户登录，响应体返回 Token |
| POST | `/tickets` | 创建工单 |
| GET | `/tickets?page=0&size=10&status=NEW` | 分页查询（支持条件过滤） |

统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`

### 核心行为

- **认证授权**：登录成功返回 JWT，后续请求通过 `Authorization: Bearer <token>` 传递；登出时将 Token 写入 Redis 黑名单，TTL 为 Token 剩余有效期。
- **数据权限**：EMPLOYEE / IT_SUPPORT / ADMIN 三级角色，在 URL、方法、数据三层分别进行权限校验。
- **动态查询**：支持分页与动态条件过滤（Specification），查询结果根据当前用户角色自动进行数据权限隔离。
- **业务规则**：工单遵循 `NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED` 五态流转，转换规则在 Service 层统一校验，结合当前状态和操作者角色判断合法性，非法流转返回 400。
- **缓存策略**：通过 Spring Cache 对查询结果进行缓存，Redis 不可用时自动回源 MySQL。


## 技术栈

| 类别 | 技术 |
|---|---|
| 框架 | Spring Boot 3.4, Spring Security 6, Spring Data JPA |
| 认证 | JWT (auth0 java-jwt) |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 构建 | Maven |
| 部署 | Docker, Docker Compose |
| 文档 | Springdoc OpenAPI (Swagger UI) |

## 系统架构

```
Client
  │
  ▼
Spring Security Filter Chain
  │
  ▼
Controller ────── Exception Handler
  │
  ▼
Service (FSM / RBAC / Cache)
  │
  ▼
Repository
(JPA Repository + Specification)
  │
  ├──────────┬──────────┐
  ▼          ▼          ▼
MySQL     Redis      Redis
(持久化)  (Cache)  (Blacklist)
```

## 项目结构

```
src/main/java/com/codelogium/ticketing/
├── common/            Result<T> 响应体
├── config/            Swagger, Redis 配置
├── dto/               请求/响应 DTO
├── entity/            JPA 实体与枚举
├── exception/         异常定义与全局处理
├── mapper/            DTO 与 Entity 转换
├── repository/        数据访问（含动态查询）
├── security/          认证过滤器、JWT、黑名单
├── service/           业务逻辑
├── util/              工具方法
└── web/               REST 接口
```

## 快速启动

**环境要求**：
Docker Engine 24+
Docker Compose v2
JDK 17（仅本地开发需要）

```bash
git clone https://github.com/nimingFang/it-support-ticket-system.git
cd it-support-ticket-system
docker compose up -d --build
```

**运行 React Demo Client**（可选，本地开发环境需要 Node.js）：

```bash
cd react-client
npm install
npm run dev
```

##访问：http://localhost:5173

**停止服务**：

```bash
docker compose down            # 保留数据
docker compose down -v         # 清除数据
```

## 内置测试账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 员工 | zhangsan | 123456 |
| IT 支持 | it_wang | 123456 |
| IT 支持 | it_li | 123456 |
| 管理员 | admin | admin123 |

## 后续计划

- 附件上传（MinIO）
- 操作日志 AOP 重构
- 工单 Excel 导出
- GitHub Actions CI
- 补充更多单元测试覆盖
