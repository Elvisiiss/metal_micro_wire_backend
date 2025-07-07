# AUTH API文档

## 基础信息
- **基础URL**: `/api/auth`
- **请求方法**: 所有认证接口使用POST方法
- **数据格式**: 请求和响应格式均为JSON
- **字符编码**: UTF-8
- **密码加密**: BCrypt算法
- **验证码存储**: Redis缓存
- **认证方式**: JWT Token + Redis验证

## 认证机制

### Token认证说明
- 登录成功后返回JWT Token
- Token同时存储在Redis中，设置过期时间
- 需要认证的接口必须携带有效Token

### Token使用方式
客户端可通过以下任一方式携带Token：

1. **Authorization头（推荐）**
```http
Authorization: Bearer <your_jwt_token>
```

2. **请求参数**
```http
GET /api/user/data?token=<your_jwt_token>
```

3. **自定义Header**
```http
token: <your_jwt_token>
```

### Token过期时间
- **普通登录**: 2小时（默认，可配置）
- **记住登录**: 168小时（7天，默认，可配置）

### 验证码配置
- **验证码有效期**: 5分钟（默认，可配置）
- **发送冷却时间**: 60秒（默认，可配置）

## 代理配置

### Vite开发环境代理配置
```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://10.168.82.63:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
```

## API接口列表

### 1. 用户注册

#### 1.1 发送注册验证码
- **接口**: `POST /api/auth/register/send-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "创建用户发送验证码",
  "e_mail": "user@example.com"
}
```
- **成功响应**:
```json
{
  "msg": "成功发送验证码",
  "e_mail": "user@example.com",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "邮箱已存在",
  "e_mail": "user@example.com", 
  "code": "Error"
}
```

#### 1.2 确认验证码并创建用户
- **接口**: `POST /api/auth/register/verify-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "创建用户确认验证码",
  "e_mail": "user@example.com",
  "user_name": "username",
  "passwd": "Password123!",
  "mail_code": "123456"
}
```
- **字段说明**:
  - `user_name`: 用户名，不能包含@符号
  - `passwd`: 密码，将使用BCrypt加密存储
  - `mail_code`: 邮箱验证码

- **成功响应**:
```json
{
  "msg": "成功创建用户",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "验证码错误",
  "code": "Error"
}
```

### 2. 用户登录

#### 2.1 账号密码登录
- **接口**: `POST /api/auth/login/password`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "密码登录",
  "account": "user@example.com",
  "passwd": "Password123!",
  "remember": true
}
```
- **字段说明**:
  - `account`: 用户名或邮箱地址
  - `passwd`: 用户密码
  - `remember`: 是否记住登录（可选，默认false）

- **成功响应**:
```json
{
  "msg": "成功登录",
  "code": "success",
  "e_mail": "user@example.com",
  "user_name": "username",
  "role_id": 0,
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsInVzZXJOYW1lIjoidXNlcm5hbWUiLCJyb2xlSWQiOjAsInN1YiI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDMxNjAwMDAsImV4cCI6MTcwMzE2NzIwMH0.signature"
}
```
- **失败响应**:
```json
{
  "msg": "账户或密码错误",
  "code": "Error"
}
```

#### 2.2 发送登录验证码
- **接口**: `POST /api/auth/login/send-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "登录发送验证码",
  "e_mail": "user@example.com"
}
```
- **成功响应**:
```json
{
  "msg": "成功发送验证码",
  "e_mail": "user@example.com",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "邮箱不存在",
  "e_mail": "user@example.com",
  "code": "Error"
}
```

#### 2.3 验证码登录
- **接口**: `POST /api/auth/login/verify-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "登录邮箱验证码",
  "e_mail": "user@example.com",
  "mail_code": "123456",
  "remember": true
}
```
- **成功响应**:
```json
{
  "msg": "成功登录",
  "code": "success",
  "e_mail": "user@example.com",
  "user_name": "username",
  "role_id": 0,
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsInVzZXJOYW1lIjoidXNlcm5hbWUiLCJyb2xlSWQiOjAsInN1YiI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3MDMxNjAwMDAsImV4cCI6MTcwMzE2NzIwMH0.signature"
}
```
- **失败响应**:
```json
{
  "msg": "登录验证码错误",
  "code": "Error"
}
```

### 3. 密码重置

#### 3.1 发送重置密码验证码
- **接口**: `POST /api/auth/reset/send-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "忘记密码发送验证码",
  "e_mail": "user@example.com"
}
```
- **成功响应**:
```json
{
  "msg": "成功发送验证码",
  "e_mail": "user@example.com",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "邮箱不存在",
  "e_mail": "user@example.com",
  "code": "Error"
}
```

#### 3.2 确认验证码并重置密码
- **接口**: `POST /api/auth/reset-password/verify-code`
- **认证**: ❌ 无需认证
- **请求示例**:
```json
{
  "msg": "提交新密码",
  "e_mail": "user@example.com",
  "new_passwd": "NewPassword123!",
  "mail_code": "123456"
}
```
- **成功响应**:
```json
{
  "msg": "成功重置密码",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "验证码错误",
  "code": "Error"
}
```

### 4. 用户管理（需要认证）

#### 4.1 用户登出
- **接口**: `GET /api/auth/logout`
- **认证**: ✅ 需要Token认证
- **请求头示例**:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
- **请求体**: 无需请求体
- **成功响应**:
```json
{
  "msg": "登出成功",
  "code": "success"
}
```
- **失败响应**:
```json
{
  "msg": "Token不能为空",
  "code": "Error"
}
```

### 5. 用户信息接口（需要认证）

#### 5.1 获取当前用户信息（已被修改，更改查看用户信息管理API文档）
- **接口**: `GET /api/user/profile`
- **认证**: ✅ 需要Token认证
- **请求头示例**:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
- **成功响应**:
```json
{
  "msg": "获取用户信息成功",
  "code": "success",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "userName": "username",
    "roleId": 0
  }
}
```

#### 5.2 更新用户信息（已被删除，更改查看用户信息管理API文档）
- **接口**: `POST /api/user/update`
- **认证**: ✅ 需要Token认证
- **请求头示例**:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```
- **成功响应**:
```json
{
  "msg": "用户信息更新成功",
  "code": "success"
}
```

#### 5.3 获取用户角色id（token）
- **接口**: `GET /api/auth/token`
- **认证**: ❌ 无需认证
- **请求头示例**:
```http
token: eyJhbGciOiJIUzI1NiJ9...
```
- **成功响应**:
```
1
```

## 错误代码说明

### HTTP状态码
- `200`: 请求成功
- `400`: 请求参数错误
- `401`: 未授权（Token无效或缺失）
- `500`: 服务器内部错误

### 业务状态码
- `success`: 操作成功
- `Error`: 操作失败

### 常见错误信息
- `未提供认证token`: 请求头中缺少Token
- `token无效或已过期`: Token验证失败
- `账户或密码错误`: 登录凭据错误
- `验证码错误`: 邮箱验证码不正确
- `邮箱已存在`: 注册时邮箱已被使用
- `用户名已存在`: 注册时用户名已被使用
- `邮箱不存在`: 操作的邮箱不存在

## 前端集成示例

### Axios配置示例
```javascript
import axios from 'axios';

const API_BASE_URL = '/api/auth';

// 创建axios实例
const authApi = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 请求拦截器 - 自动添加Token
authApi.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器 - 处理Token过期
authApi.interceptors.response.use(
    response => {
        return response;
    },
    error => {
        if (error.response?.status === 401) {
            // Token过期，清除本地存储并跳转到登录页
            localStorage.removeItem('token');
            localStorage.removeItem('userInfo');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default authApi;
```

### API调用示例
```javascript
// 账号密码登录
async function loginWithPassword(account, password, remember = false) {
    try {
        const response = await authApi.post('/login/password', {
            msg: "密码登录",
            status: 0,
            account,
            passwd: password,
            remember
        });
        
        if (response.data.code === "success") {
            // 保存Token和用户信息
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('userInfo', JSON.stringify({
                email: response.data.e_mail,
                username: response.data.user_name,
                roleId: response.data.role_id
            }));
        }
        
        return response.data;
    } catch (error) {
        console.error('登录失败:', error.response?.data);
        throw error;
    }
}

// 获取用户信息
async function getUserProfile() {
    try {
        const response = await axios.get('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('获取用户信息失败:', error.response?.data);
        throw error;
    }
}

// 用户登出
async function logout() {
    try {
        await authApi.post('/logout');
        // 清除本地存储
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
        // 跳转到登录页
        window.location.href = '/login';
    } catch (error) {
        console.error('登出失败:', error.response?.data);
        throw error;
    }
}
```

### Token管理最佳实践
```javascript
class TokenManager {
    static setToken(token) {
        localStorage.setItem('token', token);
    }
    
    static getToken() {
        return localStorage.getItem('token');
    }
    
    static removeToken() {
        localStorage.removeItem('token');
        localStorage.removeItem('userInfo');
    }
    
    static isTokenValid() {
        const token = this.getToken();
        if (!token) return false;
        
        try {
            // 简单的JWT过期检查（仅检查格式，实际验证由后端完成）
            const payload = JSON.parse(atob(token.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch {
            return false;
        }
    }
    
    static autoRefreshCheck() {
        // 定期检查Token状态
        setInterval(() => {
            if (!this.isTokenValid()) {
                this.removeToken();
                window.location.href = '/login';
            }
        }, 5 * 60 * 1000); // 每5分钟检查一次
    }
}

// 应用启动时初始化
TokenManager.autoRefreshCheck();
```

## 接口认证配置说明

### 拦截器配置
当前系统使用以下拦截器配置：

**无需认证的路径**：
- `/api/auth/register/**` - 用户注册相关接口
- `/api/auth/login/**` - 用户登录相关接口  
- `/api/auth/reset/**` - 密码重置相关接口
- 静态资源和Swagger文档等

**需要认证的路径**：
- `/api/auth/user/data` - 获取用户数据（需要Token）
- `/api/auth/logout` - 用户登出（需要Token）
- `/api/user/**` - 所有用户相关接口

## 开发注意事项

### 1. 安全考虑
- Token应存储在安全的地方（localStorage或sessionStorage）
- 生产环境必须使用HTTPS
- 定期检查Token有效性
- 敏感操作建议重新验证身份
- 已移除跨域配置，生产环境需要合适的跨域策略
- 验证码时间配置化，便于不同环境调整

### 2. 性能优化
- Token验证使用Redis缓存，响应速度快
- 合理设置Token过期时间
- 避免频繁刷新Token

### 3. 错误处理
- 统一处理401状态码，自动跳转登录页
- 网络错误时提供友好的用户提示
- 记录错误日志便于调试

### 4. 用户体验
- 登录状态持久化
- 记住登录功能延长会话时间
- 登出时清理本地数据
- 验证码发送添加防重复机制