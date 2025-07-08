# 聊天API文档

## 概述

本文档描述了金属微丝后端系统的聊天API接口，集成了DeepSeek大语言模型，支持智能对话功能。

## 认证要求

所有聊天API接口都需要JWT Token认证。Token可以通过以下方式携带：

1. **Authorization头（推荐）**：
   ```
   Authorization: Bearer your_jwt_token
   ```

2. **请求参数**：
   ```
   GET /api/chat/sessions?token=your_jwt_token
   ```

3. **自定义header**：
   ```
   token: your_jwt_token
   ```

## 用户权限

- **普通用户**（roleId=0或1）：可以正常使用所有聊天功能
- **Root用户**（roleId=999）：可以使用聊天功能，但会话与普通用户完全隔离

## API接口列表

### 1. 发送聊天消息
**POST** `/api/chat/message`

发送聊天消息到DeepSeek，支持新建会话或在现有会话中继续对话。

#### 请求头
```
Content-Type: application/json
Authorization: Bearer your_jwt_token
```

#### 请求体
```json
{
  "sessionId": "session_1703123456789_abcd1234",  // 可选，会话ID，为空则创建新会话
  "message": "你好，请介绍一下你自己",                // 必填，消息内容，最大4000字符
  "title": "新的对话"                              // 可选，会话标题，仅在创建新会话时使用，最大50字符
}
```

#### 字段说明
- `sessionId`: 会话ID（可选）
  - 如果为空或不提供，系统会创建新会话
  - 如果提供，则在指定会话中继续对话
- `message`: 消息内容（必填）
  - 长度限制：1-4000字符
  - 不能为空
- `title`: 会话标题（可选）
  - 仅在创建新会话时使用
  - 长度限制：1-50字符
  - 如果不提供，默认为"新的对话"

#### 成功响应
```json
{
  "success": true,
  "message": "发送成功",
  "data": {
    "messageId": "msg_1703123456789_efgh5678",
    "sessionId": "session_1703123456789_abcd1234",
    "userMessage": "你好，请介绍一下你自己",
    "assistantMessage": "你好！我是DeepSeek开发的AI助手，很高兴为您服务...",
    "createTime": "2024-01-01T12:00:00",
    "isNewSession": true,
    "sessionTitle": "新的对话"
  }
}
```

#### 错误响应
```json
{
  "success": false,
  "message": "发送失败：消息内容不能为空"
}
```

### 2. 创建新的聊天会话
**POST** `/api/chat/session`

创建一个新的聊天会话。

#### 请求头
```
Content-Type: application/json
Authorization: Bearer your_jwt_token
```

#### 请求体
```json
{
  "title": "技术讨论"  // 必填，会话标题，最大50字符
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "sessionId": "session_1703123456789_abcd1234",
    "title": "技术讨论",
    "userId": 1,
    "createTime": "2024-01-01T12:00:00",
    "updateTime": "2024-01-01T12:00:00",
    "messageCount": 0,
    "lastMessage": ""
  }
}
```

### 3. 获取用户的聊天会话列表
**GET** `/api/chat/sessions`

获取当前用户的所有聊天会话列表，按更新时间倒序排列。

#### 请求头
```
Authorization: Bearer your_jwt_token
```

#### 成功响应
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "sessions": [
      {
        "sessionId": "session_1703123456789_abcd1234",
        "title": "技术讨论",
        "userId": 1,
        "createTime": "2024-01-01T12:00:00",
        "updateTime": "2024-01-01T12:30:00",
        "messageCount": 4,
        "lastMessage": "感谢您的问题，我已经为您详细解答了相关技术要点..."
      },
      {
        "sessionId": "session_1703123456789_efgh5678",
        "title": "新的对话",
        "userId": 1,
        "createTime": "2024-01-01T11:00:00",
        "updateTime": "2024-01-01T11:15:00",
        "messageCount": 2,
        "lastMessage": "你好！我是DeepSeek开发的AI助手，很高兴为您服务..."
      }
    ],
    "total": 2
  }
}
```

### 4. 获取聊天历史记录
**GET** `/api/chat/history/{sessionId}`

获取指定会话的完整聊天历史记录。

#### 请求头
```
Authorization: Bearer your_jwt_token
```

#### 路径参数
- `sessionId`: 会话ID

#### 成功响应
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "sessionId": "session_1703123456789_abcd1234",
    "title": "技术讨论",
    "createTime": "2024-01-01T12:00:00",
    "messageCount": 4,
    "messages": [
      {
        "messageId": "msg_1703123456789_msg001",
        "role": "user",
        "content": "你好，请介绍一下你自己",
        "timestamp": "2024-01-01T12:00:00"
      },
      {
        "messageId": "msg_1703123456789_msg002",
        "role": "assistant",
        "content": "你好！我是DeepSeek开发的AI助手，很高兴为您服务...",
        "timestamp": "2024-01-01T12:00:05"
      },
      {
        "messageId": "msg_1703123456789_msg003",
        "role": "user",
        "content": "你能帮我解决技术问题吗？",
        "timestamp": "2024-01-01T12:01:00"
      },
      {
        "messageId": "msg_1703123456789_msg004",
        "role": "assistant",
        "content": "当然可以！我很乐意帮助您解决技术问题...",
        "timestamp": "2024-01-01T12:01:03"
      }
    ]
  }
}
```

### 5. 删除聊天会话
**DELETE** `/api/chat/session/{sessionId}`

删除指定的聊天会话及其所有消息历史。

#### 请求头
```
Authorization: Bearer your_jwt_token
```

#### 路径参数
- `sessionId`: 会话ID

#### 成功响应
```json
{
  "success": true,
  "message": "删除成功"
}
```

### 6. 更新会话标题
**PUT** `/api/chat/session/{sessionId}/title`

更新指定会话的标题。

#### 请求头
```
Authorization: Bearer your_jwt_token
```

#### 路径参数
- `sessionId`: 会话ID

#### 请求参数
- `title`: 新的会话标题（必填，最大50字符）

#### 请求示例
```
PUT /api/chat/session/session_1703123456789_abcd1234/title?title=更新后的标题
```

#### 成功响应
```json
{
  "success": true,
  "message": "更新成功"
}
```

### 7. 清理过期会话
**DELETE** `/api/chat/sessions/cleanup`

手动清理当前用户的所有过期会话。

#### 请求头
```
Authorization: Bearer your_jwt_token
```

#### 成功响应
```json
{
  "success": true,
  "message": "清理成功"
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未授权，Token无效或过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 常见错误响应

### 1. Token无效
```json
{
  "success": false,
  "message": "Token无效或已过期"
}
```

### 2. 会话不存在
```json
{
  "success": false,
  "message": "获取失败：会话不存在"
}
```

### 3. 参数验证失败
```json
{
  "success": false,
  "message": "发送失败：消息内容不能为空"
}
```

### 4. 会话数量限制
```json
{
  "success": false,
  "message": "创建失败：会话数量已达到上限：10"
}
```

### 5. DeepSeek服务不可用
```json
{
  "success": false,
  "message": "发送失败：DeepSeek服务已禁用"
}
```

## 配置说明

### 会话限制
- 每个用户最多10个会话（可配置）
- 每个会话最多100条消息（可配置）
- 会话过期时间24小时（可配置）
- 会话标题最大长度50字符（可配置）

### 消息限制
- 单条消息最大4000字符
- 支持中文、英文、数字、标点符号等

### 系统配置
在 `application.yml` 中可以配置：
```yaml
deepseek:
  api:
    enabled: true  # 是否启用DeepSeek服务
  session:
    expire-hours: 24  # 会话过期时间（小时）
    max-sessions-per-user: 10  # 每用户最大会话数
    max-messages-per-session: 100  # 每会话最大消息数
    max-title-length: 50  # 会话标题最大长度
```

## 使用建议

1. **会话管理**：建议定期清理过期会话以节省存储空间
2. **错误处理**：客户端应该妥善处理各种错误情况
3. **消息长度**：避免发送过长的消息，建议单条消息控制在2000字符以内
4. **并发控制**：避免同时发送多条消息到同一会话
5. **用户体验**：建议在发送消息时显示加载状态

## 注意事项

1. 所有时间字段均为ISO 8601格式的UTC时间
2. 会话ID和消息ID由系统自动生成，不可修改
3. 删除会话后无法恢复，请谨慎操作
4. 过期会话会自动清理，无需手动处理
5. 普通用户和Root用户的会话完全隔离，互不可见 