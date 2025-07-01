# 设备管理API文档

## 概述
设备管理功能提供设备的分页查询、创建、删除和启停控制功能。

## 权限说明
- **查询功能**：已认证用户（普通用户和管理员）
- **管理功能**：仅管理员用户（roleId=1）

## API接口

### 1. 分页查询设备列表

**接口地址：** `GET /api/device/list`

**权限要求：** 已认证用户

**请求参数：**
```
page: 页码，从0开始，默认0
size: 每页大小，范围1-100，默认10
status: 设备状态筛选，可选值：ON、OFF，不传表示查询所有
sortBy: 排序字段，默认createTime
sortDirection: 排序方向，asc/desc，默认desc
```

**请求示例：**
```
GET /api/device/list?page=0&size=10&status=ON&sortBy=createTime&sortDirection=desc
```

**响应示例：**
```json
{
  "code": "Success",
  "msg": "操作成功",
  "data": {
    "devices": [
      {
        "deviceId": "device001",
        "status": "ON",
        "createTime": "2024-01-01T10:00:00",
        "updateTime": "2024-01-01T10:00:00"
      }
    ],
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 1,
    "first": true,
    "last": true
  }
}
```

### 2. 查询单个设备信息

**接口地址：** `GET /api/device/{deviceId}`

**权限要求：** 已认证用户

**路径参数：**
- deviceId: 设备ID

**响应示例：**
```json
{
  "code": "Success",
  "msg": "操作成功",
  "data": {
    "deviceId": "device001",
    "status": "ON",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

### 3. 创建设备

**接口地址：** `POST /api/device`

**权限要求：** 仅管理员用户（roleId=1）

**请求体：**
```json
{
  "deviceId": "device001",
  "status": "OFF"
}
```

**字段说明：**
- deviceId: 设备ID，必填，只能包含字母、数字、下划线和连字符
- status: 初始状态，可选，默认OFF

**响应示例：**
```json
{
  "code": "Success",
  "msg": "操作成功",
  "data": {
    "deviceId": "device001",
    "status": "OFF",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  }
}
```

### 4. 删除设备

**接口地址：** `DELETE /api/device/{deviceId}`

**权限要求：** 仅管理员用户（roleId=1）

**路径参数：**
- deviceId: 设备ID

**响应示例：**
```json
{
  "code": "Success",
  "msg": "操作成功",
  "data": null
}
```

### 5. 控制设备（启停）

**接口地址：** `POST /api/device/control`

**权限要求：** 仅管理员用户（roleId=1）

**说明：** 当前版本暂时只打印日志，不修改数据库中的设备状态

**请求体：**
```json
{
  "deviceId": "device001",
  "targetStatus": "ON"
}
```

**字段说明：**
- deviceId: 设备ID，必填
- targetStatus: 目标状态，可选值：ON、OFF

**响应示例：**
```json
{
  "code": "Success",
  "msg": "操作成功",
  "data": null
}
```

## 错误响应

### 权限不足
```json
{
  "code": "Error",
  "msg": "权限不足，仅管理员可创建设备",
  "data": null
}
```

### 设备不存在
```json
{
  "code": "Error",
  "msg": "设备不存在：device001",
  "data": null
}
```

### 设备ID已存在
```json
{
  "code": "Error",
  "msg": "设备ID已存在：device001",
  "data": null
}
```

## 设备状态枚举

- `ON`: 设备开启状态
- `OFF`: 设备关闭状态

## 注意事项

1. 所有接口都需要在请求头中携带有效的认证Token
2. 管理员功能需要用户的roleId为1
3. 设备控制功能当前只打印日志，不修改数据库状态
4. 设备ID创建后不可修改，删除设备会永久删除相关数据
5. 分页查询支持按设备状态筛选，提高查询效率 