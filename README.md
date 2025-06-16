# Metal Micro Wire Backend

金属微丝后端项目

## 功能特性

- 用户注册/登录（邮箱验证码、用户名密码）
- **基于Redis的Token认证系统**
- 密码重置
- 邮件验证码发送
- JWT Token管理
- 接口权限控制

## Token认证系统

### 配置说明

在 `application.yml` 中配置JWT相关参数：

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

### Token生成和存储

1. **登录时生成Token**：用户成功登录后，系统会生成JWT Token
2. **存储到Redis**：Token同时存储在Redis中，设置过期时间
3. **返回给客户端**：Token在登录响应中返回给前端

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

### 认证流程

1. **登录获取Token**：
   ```http
   POST /api/auth/login/password
   {
     "msg": "登录请求",
     "status": 1,
     "account": "user@example.com",
     "passwd": "password123",
     "remember": true
   }
   ```

2. **访问需要认证的接口**：
   ```http
   GET /api/user/profile
   Authorization: Bearer your_jwt_token
   ```

3. **登出删除Token**：
   ```http
   POST /api/auth/logout
   Authorization: Bearer your_jwt_token
   ```

### 接口权限控制

- **无需认证的路径**：
  - `/api/auth/register/**` - 用户注册相关接口
  - `/api/auth/login/**` - 用户登录相关接口
  - `/api/auth/reset/**` - 密码重置相关接口
  - 静态资源、Swagger文档等

- **需要认证的路径**：
  - `/api/auth/user/data` - 获取用户数据（需要Token）
  - `/api/auth/logout` - 用户登出（需要Token）
  - `/api/user/**` - 所有用户相关接口

### Token验证机制

系统采用多重验证机制：

1. **JWT Token验证**：验证Token格式、签名、过期时间
2. **用户Token唯一性验证**：以用户ID为key存储Token，确保用户只有一个有效Token
3. **Token值完整性验证**：验证当前Token是否与Redis中存储的Token完全一致

**重要特性**：
- 用户重新登录后，之前的Token会自动失效
- 每个用户同时只能有一个有效的Token
- Token被替换后，旧Token立即无法使用

### 错误处理

当Token验证失败时，系统返回：

```json
{
  "code": "Error",
  "msg": "token无效或已过期"
}
```

HTTP状态码：401 Unauthorized

### 登录状态管理

- **普通登录**：Token过期时间为2小时（默认配置）
- **记住登录**：Token过期时间为168小时（7天，默认配置）
- **主动登出**：调用登出接口删除Redis中的Token

## API接口

### 认证相关接口

- `POST /api/auth/register/send-code` - 发送注册验证码
- `POST /api/auth/register/verify-code` - 用户注册
- `POST /api/auth/login/password` - 密码登录（返回Token）
- `POST /api/auth/login/send-code` - 发送登录验证码
- `POST /api/auth/login/verify-code` - 验证码登录（返回Token）
- `POST /api/auth/reset/send-code` - 发送重置密码验证码
- `POST /api/auth/reset/verify-code` - 重置密码
- `POST /api/auth/logout` - 用户登出（需要Token）

### 用户相关接口（需要Token认证）

- `GET /api/user/profile` - 获取当前用户信息
- `POST /api/user/update` - 更新用户信息

## 技术栈

- Spring Boot 3.4.5
- Spring Data JPA
- MySQL
- Redis
- JWT (JJWT)
- BCrypt
- Java Mail
- Lombok

## 系统优化特性

### 验证码机制优化
- **可配置冷却时间**：默认60秒发送冷却，可在配置文件调整
- **可配置有效期**：默认5分钟有效期，可在配置文件调整
- **智能提示**：显示剩余冷却时间
- **网络容错**：验证码过期后可重新发送

### Token安全机制
- **单点登录**：用户重新登录后旧Token自动失效
- **Token唯一性**：每个用户同时只能有一个有效Token
- **完整性验证**：确保Token值完全匹配

## 环境要求

- Java 17+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 运行步骤

1. **配置数据库和Redis**
   - 确保MySQL和Redis服务正在运行
   - 根据实际情况修改application.yml中的配置

2. **编译项目**
```bash
mvn clean compile
```

3. **运行项目**
```bash
mvn spring-boot:run
```

4. **验证服务**
   - 访问 http://localhost:8080/api/health 检查服务状态

## API接口

详细的API文档请查看 `src/main/resources/docs/AUTH_API.md`

主要接口：
- `/api/auth/register/send-code` - 发送注册验证码
- `/api/auth/register/verify-code` - 用户注册
- `/api/auth/login/password` - 账号密码登录
- `/api/auth/login/send-code` - 发送登录验证码
- `/api/auth/login/verify-code` - 验证码登录
- `/api/auth/reset-password/send-code` - 发送重置密码验证码
- `/api/auth/reset-password/verify-code` - 重置密码
- `/api/health` - 健康检查 