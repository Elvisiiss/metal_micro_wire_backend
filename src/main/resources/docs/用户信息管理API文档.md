# 用户信息管理API文档

## 概述

用户信息管理模块提供用户头像上传、头像删除、用户名修改等功能。所有接口都需要JWT认证，用户只能操作自己的信息。

## 基础信息

- **基础URL**: `/api/user`
- **认证方式**: JWT Bearer Token
- **响应格式**: JSON
- **统一响应格式**:
```json
{
  "msg": "描述信息",
  "code": "success|Error",
  "data": {...}
}
```

## 接口列表

### 1. 获取用户详细资料(更新)

获取当前登录用户的详细信息，包括头像URL等。

**接口地址**: `GET /api/user/profile`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "msg": "获取用户资料成功",
  "code": "success",
  "data": {
    "userId": 1,
    "userName": "testuser",
    "email": "test@example.com",
    "roleId": 0,
    "roleName": "普通用户",
    "status": 0,
    "statusName": "正常",
    "avatarUrl": "/api/files/avatars/avatar_1_20241207_143022_a1b2c3d4.jpg",
    "createTime": "2024-12-07T14:30:22",
    "updateTime": "2024-12-07T14:30:22"
  }
}
```

### 2. 上传用户头像（新增）

上传用户头像图片文件。

**接口地址**: `POST /api/user/avatar/upload`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 头像图片文件 |

**文件限制**:
- 文件大小：最大2MB
- 支持格式：jpg, jpeg, png, gif, webp
- 文件名：支持中文和特殊字符

**响应示例**:
```json
{
  "msg": "头像上传成功",
  "code": "success",
  "data": {
    "avatarUrl": "/api/files/avatars/avatar_1_20241207_143022_a1b2c3d4.jpg",
    "fileSize": 1048576,
    "originalFilename": "my_avatar.jpg",
    "uploadTimestamp": 1701936622000
  }
}
```

**错误响应示例**:
```json
{
  "msg": "文件大小超过限制，最大允许 2MB",
  "code": "Error"
}
```

### 3. 删除用户头像（新增）

删除当前用户的头像。

**接口地址**: `DELETE /api/user/avatar`

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "msg": "头像删除成功",
  "code": "success"
}
```

**错误响应示例**:
```json
{
  "msg": "用户暂无头像",
  "code": "Error"
}
```

### 4. 修改用户名（更新）

修改当前用户的用户名。

**接口地址**: `PUT /api/user/username`

**请求头**:
```
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "newUsername": "newusername"
}
```

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| newUsername | String | 是 | 新用户名 |

**用户名规则**:
- 长度：1-20个字符
- 字符：只能包含字母、数字、下划线和中文字符
- 限制：不能包含@符号
- 唯一性：不能与其他用户重复

**响应示例**:
```json
{
  "msg": "用户名修改成功",
  "code": "success"
}
```

**错误响应示例**:
```json
{
  "msg": "用户名已被使用",
  "code": "Error"
}
```

### 5. 访问头像文件（新增）

直接访问上传的头像文件（无需认证）。

**接口地址**: `GET /api/files/avatars/{filename}`

**请求示例**:
```
GET /api/files/avatars/avatar_1_20241207_143022_a1b2c3d4.jpg
```

**响应**: 直接返回图片文件

## 错误码说明

| 错误信息 | 说明 |
|----------|------|
| 用户不存在 | 用户ID无效或用户已被删除 |
| 文件不能为空 | 未选择文件或文件为空 |
| 文件大小超过限制 | 文件大小超过2MB |
| 不支持的文件类型 | 文件格式不在支持列表中 |
| 用户名不能为空 | 用户名字段为空 |
| 用户名不能包含@符号 | 用户名包含非法字符 |
| 用户名已被使用 | 用户名重复 |
| 新用户名与当前用户名相同 | 用户名未发生变化 |

## 前端集成示例

### Vue3 + Axios 示例

```javascript
// 1. 获取用户资料
async function getUserProfile() {
  try {
    const response = await axios.get('/api/user/profile', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    console.error('获取用户资料失败:', error);
  }
}

// 2. 上传头像
async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);
  
  try {
    const response = await axios.post('/api/user/avatar/upload', formData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  } catch (error) {
    console.error('头像上传失败:', error);
  }
}

// 3. 删除头像
async function deleteAvatar() {
  try {
    const response = await axios.delete('/api/user/avatar', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.data;
  } catch (error) {
    console.error('头像删除失败:', error);
  }
}

// 4. 修改用户名
async function updateUsername(newUsername) {
  try {
    const response = await axios.put('/api/user/username', {
      newUsername: newUsername
    }, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('用户名修改失败:', error);
  }
}
```

### 头像显示组件示例

```vue
<template>
  <div class="avatar-container">
    <img 
      v-if="avatarUrl" 
      :src="avatarUrl" 
      alt="用户头像"
      class="avatar-image"
      @error="handleImageError"
    />
    <div v-else class="avatar-placeholder">
      {{ userName?.charAt(0)?.toUpperCase() }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  avatarUrl: String,
  userName: String
})

const handleImageError = () => {
  console.warn('头像加载失败')
}
</script>

<style scoped>
.avatar-container {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  font-size: 24px;
  font-weight: bold;
  color: #666;
}
</style>
```

## 注意事项

1. **文件存储**: 头像文件存储在服务器本地 `uploads/avatars/` 目录下
2. **文件命名**: 自动生成唯一文件名，格式为 `avatar_{userId}_{timestamp}_{uuid}.{ext}`
3. **旧文件清理**: 上传新头像时会自动删除旧头像文件
4. **权限控制**: 用户只能操作自己的头像和用户名
5. **文件访问**: 头像文件可通过 `/api/files/avatars/{filename}` 直接访问，无需认证
6. **错误处理**: 建议前端对所有可能的错误情况进行处理
7. **文件预览**: 上传前建议添加文件预览功能
8. **进度显示**: 大文件上传时建议显示上传进度
