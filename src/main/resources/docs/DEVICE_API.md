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
  "code": "success",
  "msg": "操作成功",
  "data": {
    "devices": [
      {
        "deviceId": "device001",
        "deviceCode": "01",
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
  "code": "success",
  "msg": "操作成功",
  "data": {
    "deviceId": "device001",
    "deviceCode": "01",
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
  "deviceCode": "01"
}
```

**字段说明：**
- deviceId: 设备ID，必填，只能包含字母、数字、下划线和连字符
- deviceCode: 设备代码，可选，必须是两位数字，用于匹配批次号中的机器号（13-14位）

**说明：**
- 设备初始状态固定为OFF，由后端自动设置，无需前端传递
- 设备代码用于与线材批次号中的机器号进行关联，实现设备与线材数据的自动匹配

**响应示例：**
```json
{
  "code": "success",
  "msg": "操作成功",
  "data": {
    "deviceId": "device001",
    "deviceCode": "01",
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
  "code": "success",
  "msg": "操作成功",
  "data": null
}
```

### 5. 控制设备（启停）

**接口地址：** `POST /api/device/control`

**权限要求：** 仅管理员用户（roleId=1）

**说明：** 
- 此接口向华为云IoT设备发送控制命令消息（消息名：CMD_ON_OFF，内容：ON/OFF）
- 由于硬件限制，无法获得设备的实时响应确认，只能确认消息已送达IoT平台
- 设备状态的实际更新通过AMQP消息异步完成，系统会自动监听并更新数据库中的设备状态
- 客户端应通过轮询设备状态接口或实现状态变化监听来获取最新状态

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

**成功响应示例：**
```json
{
  "code": "success",
  "msg": "控制消息已送达，请等待设备启动完成",
  "data": null
}
```

**失败响应示例：**
```json
{
  "code": "Error",
  "msg": "设备控制消息发送失败",
  "data": null
}
```

### 6. 测试设备注册状态

**接口地址：** `POST /api/device/test-connection`

**权限要求：** 仅管理员用户（roleId=1）

**说明：** 
- 此接口向指定设备发送测试消息（消息名：connect，内容：test）
- 用于验证设备是否在华为云IoT平台上注册，而非设备在线状态
- 华为云消息有缓存机制，消息能够送达即表示设备ID已在平台注册
- 不能用于判断设备是否在线，只能确认设备是否已注册

**请求体：**
```json
{
  "deviceId": "device001"
}
```

**字段说明：**
- deviceId: 设备ID，必填，只能包含字母、数字、下划线和连字符

**成功响应示例：**
```json
{
  "code": "success",
  "msg": "测试消息已发送，设备已在华为云IoT平台注册",
  "data": null
}
```

**失败响应示例：**
```json
{
  "code": "Error",
  "msg": "测试消息发送失败，设备可能未在华为云IoT平台注册",
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

### 设备代码已存在
```json
{
  "code": "Error",
  "msg": "设备代码已存在：01",
  "data": null
}
```

## 设备状态枚举

- `ON`: 设备开启状态
- `OFF`: 设备关闭状态

## 注意事项

1. 所有接口都需要在请求头中携带有效的认证Token
2. 管理员功能需要用户的roleId为1
3. **设备创建规则**：
   - 设备初始状态固定为OFF，由后端自动设置
   - 前端只需传递设备ID，无需传递状态参数
   - 设备ID必须唯一，不可重复
4. **设备控制为异步操作**：
   - 控制接口仅确认消息发送状态，不代表设备已完成状态切换
   - 设备实际状态通过AMQP消息异步更新，可能存在1-5秒的延迟
   - 建议在发送控制命令后，间隔2-3秒后查询设备状态确认是否切换成功
5. **测试设备注册功能**：
   - 测试消息（消息名：connect，内容：test）用于验证设备是否在华为云IoT平台注册
   - 该功能不会更改设备状态，仅用于注册状态检查
   - 华为云消息有缓存机制，消息能够送达即表示设备ID已注册
   - 无法判断设备实际在线状态，只能确认注册状态
6. **设备代码与线材关联**：
   - 设备代码用于与线材批次号中的机器号（13-14位）进行自动匹配
   - 批次号格式：Cu0120250629010010001，其中13-14位为检测机器号
   - 系统在处理线材数据时会自动解析批次号并建立设备关联
   - 设备代码必须是两位数字，建议按实际机器编号设置
7. 设备ID创建后不可修改，删除设备会永久删除相关数据
8. 分页查询支持按设备状态筛选，提高查询效率

## 设备与线材数据关联

设备通过设备代码与线材检测数据进行关联：

### 批次号解析规则
- 批次号格式：`Cu0120250629010010001`（21位）
- 第13-14位为检测机器号，对应设备的deviceCode字段
- 系统自动解析批次号并在线材数据中记录设备代码

### 关联效果
当IoT设备上报线材检测数据时：
1. 系统解析批次号中的机器号（13-14位）
2. 在线材记录中保存设备代码
3. 可通过设备代码关联查询该设备的所有线材数据

### 查询示例
```sql
-- 查询设备代码为"01"的所有线材数据
SELECT w.*, d.device_id, d.status 
FROM wire_materials w
LEFT JOIN devices d ON w.device_code = d.device_code
WHERE w.device_code = '01';
```
