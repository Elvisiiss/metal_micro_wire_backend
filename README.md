# Metal Micro Wire Backend

金属微丝后端项目 - 基于Spring Boot的企业级用户管理系统

## 功能特性

### 核心功能
- 🔐 **双重用户系统**：支持普通用户和Root超级管理员
- 👤 **完整用户管理**：注册、登录、权限控制、状态管理
- 🛡️ **Token安全机制**：JWT + Redis双重验证，解决ID冲突问题
- 📧 **邮件验证系统**：支持注册、登录、密码重置验证码
- 🔒 **密码安全**：BCrypt加密存储
- 🎯 **角色权限控制**：多级权限管理（普通用户/管理员/Root）

### Root管理功能
- ✅ **用户提权降权**：设置用户角色（普通用户/管理员）
- 🚫 **用户禁用启用**：控制用户登录状态
- 📊 **用户列表管理**：分页查询、关键词搜索
- 🔄 **批量操作**：支持批量禁用、启用、提权、降权
- 📋 **用户详情查看**：完整的用户信息展示

## 系统架构

### 用户类型
| 类型 | 角色ID | 权限说明 |
|------|-------|----------|
| **普通用户** | 0 | 基本权限，注册登录等 |
| **管理员** | 1 | 管理权限，扩展功能 |
| **Root用户** | 999 | 超级管理员，用户管理 |

### 用户状态
| 状态 | 值 | 说明 |
|------|---|------|
| **正常** | 0 | 可以正常登录使用 |
| **禁用** | 1 | 无法登录，被管理员禁用 |

## Token冲突解决方案

### 问题背景
原始设计中Root用户和普通用户都使用自增ID，可能导致Redis中Token键冲突：
- Root用户ID=1 和 普通用户ID=1 都使用`user_token:1`，造成Token覆盖

### 解决方案
引入**用户类型枚举**，修改Redis键命名规则：

```java
// 用户类型枚举
enum UserType {
    NORMAL("user"),  // 普通用户
    ROOT("root");    // Root用户
}

// Redis键格式
token:user:1    // 普通用户ID=1
token:root:1    // Root用户ID=1
```

### JWT Token增强
Token中包含用户类型信息：
```json
{
  "userId": 1,
  "email": "user@example.com", 
  "userName": "testuser",
  "roleId": 0,
  "userType": "NORMAL",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### 安全保障
- ✅ **完全隔离**：Root和普通用户Token互不影响
- ✅ **类型验证**：Token中包含用户类型，支持双重验证
- ✅ **权限控制**：Root接口只允许Root用户访问，普通用户访问会被拦截
- ✅ **双重拦截器**：基础认证拦截器 + Root权限拦截器
- ✅ **日志追踪**：详细记录不同类型用户的操作和权限验证

## API接口

### Root管理接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/auth/root/login` | Root用户登录 |
| GET | `/api/root/users` | 获取用户列表（分页、排序）|
| PUT | `/api/root/users/role` | 用户提权/降权 |
| PUT | `/api/root/users/status` | 用户禁用/启用 |
| GET | `/api/root/users/{userId}` | 获取用户详情 |
| PUT | `/api/root/users/batch` | 批量用户操作 |

### 普通用户接口

| 方法 | 端点 | 描述 |
|------|------|------|
| POST | `/api/auth/register/send-code` | 发送注册验证码 |
| POST | `/api/auth/register/verify-code` | 用户注册 |
| POST | `/api/auth/login/password` | 密码登录 |
| POST | `/api/auth/login/send-code` | 发送登录验证码 |
| POST | `/api/auth/login/verify-code` | 验证码登录 |
| POST | `/api/auth/reset-password/send-code` | 发送重置密码验证码 |
| POST | `/api/auth/reset-password/verify-code` | 重置密码 |
| GET | `/api/auth/logout` | 用户登出 |

## Token认证系统

### 配置说明

```yaml
# JWT Token配置
jwt:
  secret: your_jwt_secret_key
  # 普通登录过期时间（小时）
  expiration-normal: 2
  # 记住登录过期时间（小时）  
  expiration-remember: 168

# 验证码配置
verification:
  # 验证码有效期（分钟）
  code-expire-minutes: 5
  # 验证码发送冷却时间（秒）
  send-cooldown-seconds: 60
```

### Token使用方式

客户端可以通过以下三种方式携带Token：

1. **Authorization头（推荐）**：
   ```
   Authorization: Bearer your_jwt_token
   ```

2. **请求参数**：
   ```
   GET /api/user/profile?token=your_jwt_token
   ```

3. **自定义header**：
   ```
   token: your_jwt_token
   ```

### 验证机制

系统采用多重验证机制：

1. **JWT Token验证**：验证Token格式、签名、过期时间
2. **用户类型验证**：验证用户类型和权限
3. **Token唯一性验证**：确保用户只有一个有效Token
4. **状态检查**：验证用户是否被禁用
5. **Root权限拦截**：`/api/root/**`接口只允许Root用户访问

**重要特性**：
- 被禁用用户无法登录（包括密码登录和验证码登录）
- 用户重新登录后，之前的Token会自动失效
- Root用户和普通用户Token完全隔离
- **权限严格控制**：普通用户无法访问Root管理接口，返回403 Forbidden

5. **权限验证测试**
   ```http
   # 普通用户尝试访问Root接口（应该返回403）
   GET {{BASE_URL}}/api/root/users
   Authorization: Bearer {{USER_TOKEN}}
   
   # 期望返回：
   # {
   #   "code": "Error",
   #   "msg": "权限不足，仅Root用户可访问此接口"
   # }
   ```

### Redis监控

测试过程中可使用以下命令监控Token存储：

```bash
# 查看所有Token键
redis-cli keys "token:*"

# 查看Root用户Token
redis-cli keys "token:root:*"

# 查看普通用户Token  
redis-cli keys "token:user:*"
```

## 数据库表结构

### users表（普通用户）
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(255) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(255) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    role_id INT NOT NULL DEFAULT 0 COMMENT '角色ID (0: 普通用户, 1: 管理员)',
    status INT NOT NULL DEFAULT 0 COMMENT '用户状态 (0: 正常, 1: 禁用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
```

### user_root表（Root用户）
```sql
CREATE TABLE user_root (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(255) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)'
);
```

## 技术栈

- **框架**: Spring Boot 3.4.5
- **数据访问**: Spring Data JPA
- **数据库**: MySQL 8.0+
- **缓存**: Redis 6.0+
- **安全**: JWT (JJWT) + BCrypt
- **邮件**: Java Mail
- **工具**: Lombok
- **文档**: Swagger/OpenAPI

## 快速开始

### 环境要求

- Java 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 运行步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd metal_micro_wire_backend
   ```

2. **配置数据库和Redis**
   - 确保MySQL和Redis服务正在运行
   - 复制 `application-example.yml` 为 `application.yml`
   - 根据实际情况修改数据库和Redis配置

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行项目**
   ```bash
   mvn spring-boot:run
   ```

5. **验证服务**
   - 访问 http://localhost:8080/api/health 检查服务状态

### 初始化Root用户

首次运行需要创建Root用户（建议通过数据库直接插入）


## 部署注意事项

### 生产环境配置

1. **安全配置**
   - 修改JWT密钥
   - 配置强密码策略
   - 限制Root用户创建权限

2. **Redis配置**
   - 部署前清理旧Token格式：
     ```bash
     redis-cli --scan --pattern "user_token:*" | xargs redis-cli del
     ```

3. **监控配置**
   - 关注Token类型日志
   - 监控用户登录状态
   - 定期审查用户权限

## 系统特色

### 🔒 安全特性
- **Token冲突解决**：完美解决Root用户和普通用户ID冲突
- **状态控制**：支持用户禁用，提高系统安全性
- **权限隔离**：不同类型用户权限完全隔离
- **日志追踪**：详细的操作日志记录

### 🚀 性能优化
- **Redis缓存**：Token存储优化，快速验证
- **分页查询**：大数据量用户列表高效处理
- **批量操作**：提高管理效率

### 🛠️ 开发友好
- **完整文档**：详细的API文档和测试用例
- **清晰架构**：分层架构，易于扩展维护
- **配置灵活**：支持多环境配置

---

> 💡 **提示**: 更多详细信息请查看 `src/main/resources/docs/` 目录下的API文档。


### 必要的代码
- **创建数据库**：create database metal_micro_wire dbcompatibility ='B';
