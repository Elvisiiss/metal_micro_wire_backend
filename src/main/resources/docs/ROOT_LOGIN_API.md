# Root用户管理API文档

## 概述
Root登录功能为系统管理员提供了最高权限的访问能力。Root用户拥有角色ID为999的超级管理员权限，可以管理普通用户的权限和状态。

## API端点

### 1. Root用户登录
**POST** `/api/auth/root/login`

#### 请求体
```json
{
  "userName": "root用户名",
  "password": "root密码",
  "remember": false
}
```

#### 字段说明
- `userName`: Root用户名（必填）
- `password`: Root密码（必填）
- `remember`: 是否记住登录状态（可选，默认false）

#### 成功响应
```json
{
  "success": true,
  "message": "Root登录成功",
  "data": {
    "token": "JWT_TOKEN",
    "email": "root@system",
    "userName": "root用户名",
    "roleId": 999
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "Root用户名或密码错误"
}
```

### 2. 获取用户列表（分页）
**GET** `/api/root/users`

#### 请求参数
- `page`: 页码（可选，默认0）
- `size`: 每页大小（可选，默认10）
- `keyword`: 搜索关键词（可选，支持用户名和邮箱模糊搜索）
- `sortBy`: 排序字段（可选，默认createTime，支持：id、createTime）
- `sortDir`: 排序方向（可选，默认desc，支持：asc、desc）

#### 成功响应
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "userName": "testuser",
        "email": "test@example.com",
        "roleId": 0,
        "roleName": "普通用户",
        "status": 0,
        "statusName": "正常",
        "createTime": "2024-01-01T12:00:00",
        "updateTime": "2024-01-01T12:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0
  }
}
```

### 3. 用户提权（设置角色）
**PUT** `/api/root/users/role`

#### 请求体
```json
{
  "userId": 1,
  "roleId": 1
}
```

#### 字段说明
- `userId`: 用户ID（必填）
- `roleId`: 角色ID（必填，0: 普通用户, 1: 管理员）

#### 成功响应
```json
{
  "success": true,
  "message": "用户角色更新成功，当前角色：管理员"
}
```

### 4. 禁用/启用用户
**PUT** `/api/root/users/status`

#### 请求体
```json
{
  "userId": 1,
  "status": 1
}
```

#### 字段说明
- `userId`: 用户ID（必填）
- `status`: 用户状态（必填，0: 正常, 1: 禁用）

#### 成功响应
```json
{
  "success": true,
  "message": "用户已禁用"
}
```

### 5. 获取用户详情
**GET** `/api/root/users/{userId}`

#### 路径参数
- `userId`: 用户ID

#### 成功响应
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 1,
    "userName": "testuser",
    "email": "test@example.com",
    "roleId": 0,
    "roleName": "普通用户",
    "status": 0,
    "statusName": "正常",
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-01T12:00:00"
  }
}
```

### 6. 批量用户操作
**PUT** `/api/root/users/batch`

#### 请求参数
- `action`: 操作类型（disable: 禁用, enable: 启用, promote: 提权为管理员, demote: 降权为普通用户）
- `userIds`: 用户ID列表，用逗号分隔（如："1,2,3,4"）

#### 成功响应
```json
{
  "success": true,
  "message": "批量操作完成，成功处理 3 个用户"
}
```

## 用户角色和状态说明

### 角色类型
- **0**: 普通用户 - 基本权限
- **1**: 管理员 - 管理权限
- **999**: Root用户 - 超级管理员权限

### 用户状态
- **0**: 正常 - 用户可以正常登录和使用系统
- **1**: 禁用 - 用户无法登录，所有操作被阻止

## 安全特性

1. **权限控制**: 只有Root用户（角色ID=999）才能访问用户管理接口
2. **状态验证**: 被禁用的用户无法登录系统
3. **操作日志**: 所有用户管理操作都会记录到日志中
4. **数据验证**: 严格的输入验证和参数校验

## 数据库表结构

### users表
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

### user_root表
```sql
CREATE TABLE user_root (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(255) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)'
);
```

## 使用示例

### Root用户登录
```bash
curl -X POST http://localhost:8080/api/auth/root/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "admin",
    "password": "admin123456",
    "remember": false
  }'
```

### 获取用户列表
```bash
# 按创建时间降序排列（默认）
curl -X GET "http://localhost:8080/api/root/users?page=0&size=10&keyword=test" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN"

# 按ID升序排列
curl -X GET "http://localhost:8080/api/root/users?page=0&size=10&sortBy=id&sortDir=asc" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN"

# 按创建时间升序排列
curl -X GET "http://localhost:8080/api/root/users?page=0&size=10&sortBy=createTime&sortDir=asc" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN"
```

### 用户提权
```bash
curl -X PUT http://localhost:8080/api/root/users/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN" \
  -d '{
    "userId": 1,
    "roleId": 1
  }'
```

### 禁用用户
```bash
curl -X PUT http://localhost:8080/api/root/users/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN" \
  -d '{
    "userId": 1,
    "status": 1
  }'
```

### 批量操作
```bash
curl -X PUT "http://localhost:8080/api/root/users/batch?action=disable&userIds=1,2,3" \
  -H "Authorization: Bearer YOUR_ROOT_TOKEN"
```

## 注意事项

1. **Root权限管理**: Root用户拥有最高权限，应当谨慎管理和使用
2. **用户状态影响**: 被禁用的用户无法登录，包括密码登录和验证码登录
3. **角色权限区分**: 不同角色拥有不同的系统权限，请根据需要合理分配
4. **操作可逆性**: 用户的禁用/启用和角色提权/降权都是可逆操作
5. **安全建议**: 定期审查用户权限，及时禁用不活跃或风险用户
6. **批量操作**: 批量操作支持同时处理多个用户，提高管理效率 