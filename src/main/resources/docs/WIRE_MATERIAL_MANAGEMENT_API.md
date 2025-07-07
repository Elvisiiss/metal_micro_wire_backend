# 线材管理API文档

## 概述

线材管理模块提供对金属微丝检测数据的管理功能，包括查询、编辑和删除线材记录。线材数据通过IoT设备自动采集并保存，管理员可以对这些数据进行后续管理。

## 权限说明

线材管理API有不同的权限要求：
- **公开接口**：根据批次号查询线材信息，无需任何认证
- **已认证用户**：分页查询线材列表，需要登录但不需要管理员权限
- **管理员用户**：更新和删除功能，需要普通用户中角色ID为1的管理员权限
- **Root用户**：Root用户专注于用户管理，不具有线材管理权限

## 数据模型

### 线材实体 (WireMaterial)

**主要字段**：
- **batchNumber**: 批次卷序（主键，唯一标识）
- **deviceId**: 设备ID（IoT设备标识）
- **diameter**: 金属丝直径（mm）
- **resistance**: 电导率
- **extensibility**: 延展率（%）
- **weight**: 重量（g）
- **sourceOriginRaw**: 原始生产信息（十六进制GBK编码）
- **manufacturer**: 生产商
- **responsiblePerson**: 负责人
- **processType**: 工艺类型
- **productionMachine**: 生产机器
- **contactEmail**: 联系方式（邮箱）
- **scenarioCode**: 应用场景编号（从批次号解析）
- **deviceCode**: 设备代码（从批次号解析）
- **eventTime**: 事件发生时间
- **createTime**: 创建时间

## API接口

### API权限总览

| 方法 | 端点 | 权限要求 | 描述 |
|------|------|----------|------|
| GET | `/api/wire-material/list` | 已认证用户 | 分页查询线材列表 |
| GET | `/api/wire-material/{batchNumber}` | 无需认证 | 根据批次号查询线材信息 |
| PUT | `/api/wire-material/{batchNumber}` | 管理员 | 更新线材信息 |
| DELETE | `/api/wire-material/{batchNumber}` | 管理员 | 删除线材记录 |

**权限说明**：
- **无需认证**：任何人都可以访问，无需登录
- **已认证用户**：需要登录，任何角色（roleId=0或1）都可以访问
- **管理员**：需要登录且角色为管理员（roleId=1），Root用户无此权限

### 1. 分页查询线材列表

**GET** `/api/wire-material/list`

**权限**: 已认证用户（任何登录用户都可以查询）

**查询参数**:
- `page`: 页码，从0开始 (默认: 0)
- `size`: 每页大小 (默认: 10，最大: 100)
- `batchNumberKeyword`: 批次号关键词搜索 (可选)
- `deviceIdKeyword`: 设备ID关键词搜索 (可选)
- `manufacturerKeyword`: 生产商关键词搜索 (可选)
- `responsiblePersonKeyword`: 负责人关键词搜索 (可选)
- `processTypeKeyword`: 工艺类型关键词搜索 (可选)
- `productionMachineKeyword`: 生产机器关键词搜索 (可选)
- `scenarioCode`: 应用场景编号筛选 (可选)
- `deviceCode`: 设备代码筛选 (可选)
- `sortBy`: 排序字段 (默认: createTime)
- `sortDirection`: 排序方向 (默认: desc)

**响应示例**:
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": {
    "wireMaterials": [
      {
        "batchNumber": "Cu0120250629010010001",
        "deviceId": "device001",
        "diameter": 0.25,
        "resistance": 60.5,
        "extensibility": 18.2,
        "weight": 1.5,
        "sourceOriginRaw": "hex_encoded_string",
        "manufacturer": "某某制造公司",
        "responsiblePerson": "张三",
        "processType": "拉丝工艺",
        "productionMachine": "拉丝机001",
        "contactEmail": "contact@example.com",
        "scenarioCode": "01",
        "deviceCode": "10",
        "eventTime": "2025-01-15T10:30:00",
        "createTime": "2025-01-15T10:35:00"
      }
    ],
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalElements": 42,
    "first": true,
    "last": false
  }
}
```

### 2. 根据批次号查询线材信息

**GET** `/api/wire-material/info/{batchNumber}`

**权限**: 无需认证（公开接口）

**路径参数**:
- `batchNumber`: 批次号（必填）

**响应示例**:
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": {
    "batchNumber": "Cu0120250629010010001",
    "deviceId": "device001",
    "diameter": 0.25,
    "resistance": 60.5,
    "extensibility": 18.2,
    "weight": 1.5,
    "sourceOriginRaw": "hex_encoded_string",
    "manufacturer": "某某制造公司",
    "responsiblePerson": "张三",
    "processType": "拉丝工艺",
    "productionMachine": "拉丝机001",
    "contactEmail": "contact@example.com",
    "scenarioCode": "01",
    "deviceCode": "10",
    "eventTime": "2025-01-15T10:30:00",
    "createTime": "2025-01-15T10:35:00"
  }
}
```

### 3. 更新线材信息

**PUT** `/api/wire-material/{batchNumber}`

**权限**: 管理员（roleId=1，不包括Root用户）

**路径参数**:
- `batchNumber`: 批次号（必填）

**请求体**:
```json
{
  "diameter": 0.26,
  "resistance": 61.0,
  "extensibility": 19.0,
  "weight": 1.6,
  "manufacturer": "新制造公司",
  "responsiblePerson": "李四",
  "processType": "新拉丝工艺",
  "productionMachine": "拉丝机002",
  "contactEmail": "newcontact@example.com"
}
```

**字段说明**:
- 所有字段都是可选的，只更新提供的字段
- `diameter`: 金属丝直径，必须≥0
- `resistance`: 电导率，必须≥0
- `extensibility`: 延展率，必须≥0
- `weight`: 重量，必须≥0
- `manufacturer`: 生产商名称，最大100字符
- `responsiblePerson`: 负责人姓名，最大50字符
- `processType`: 工艺类型，最大50字符
- `productionMachine`: 生产机器名称，最大100字符
- `contactEmail`: 联系邮箱，必须是有效邮箱格式，最大100字符

**响应示例**:
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": {
    "batchNumber": "Cu0120250629010010001",
    "deviceId": "device001",
    "diameter": 0.26,
    "resistance": 61.0,
    "extensibility": 19.0,
    "weight": 1.6,
    "sourceOriginRaw": "hex_encoded_string",
    "manufacturer": "新制造公司",
    "responsiblePerson": "李四",
    "processType": "新拉丝工艺",
    "productionMachine": "拉丝机002",
    "contactEmail": "newcontact@example.com",
    "scenarioCode": "01",
    "deviceCode": "10",
    "eventTime": "2025-01-15T10:30:00",
    "createTime": "2025-01-15T10:35:00"
  }
}
```

### 4. 删除线材记录

**DELETE** `/api/wire-material/{batchNumber}`

**权限**: 管理员（roleId=1，不包括Root用户）

**路径参数**:
- `batchNumber`: 批次号（必填）

**响应示例**:
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": null
}
```

## 错误响应

### 权限不足
```json
{
  "msg": "权限不足，仅管理员可操作",
  "code": "Error",
  "data": null
}
```

### 认证Token缺失（仅对需要认证的接口）
```json
{
  "msg": "未提供认证token",
  "code": "Error",
  "data": null
}
```

### 记录不存在
```json
{
  "msg": "线材记录不存在：Cu0120250629010010001",
  "code": "Error",
  "data": null
}
```

### 参数验证失败
```json
{
  "msg": "参数验证失败：批次号不能为空",
  "code": "Error",
  "data": null
}
```

## 使用说明

### 搜索功能
线材管理支持多字段模糊搜索：
- **批次号搜索**：支持部分匹配，例如搜索"Cu01"可以找到所有以"Cu01"开头的批次
- **设备ID搜索**：支持设备标识符的模糊匹配
- **生产商搜索**：支持公司名称的模糊匹配
- **负责人搜索**：支持人员姓名的模糊匹配
- **工艺类型搜索**：支持工艺名称的模糊匹配
- **生产机器搜索**：支持机器名称的模糊匹配

### 精确筛选
- **应用场景编号**：按场景编号精确筛选（例如"01", "02"）
- **设备代码**：按设备代码精确筛选（例如"10", "20"）

### 排序功能
支持按以下字段排序：
- `createTime`: 创建时间（默认）
- `eventTime`: 事件发生时间
- `batchNumber`: 批次号
- `diameter`: 直径
- `resistance`: 电导率
- `extensibility`: 延展率
- `weight`: 重量

### 数据来源说明
- 线材数据由IoT设备自动采集并通过AMQP消息队列传输
- 系统自动解析消息并保存到数据库
- 管理员可以对保存的数据进行编辑和管理
- 批次号是唯一标识符，由设备生成，不可修改

### 权限验证
- **公开接口**：根据批次号查询线材信息无需任何认证，任何人都可以访问
- **列表查询**：需要用户登录，但任何角色的用户都可以查询
- **管理功能**：更新和删除操作需要管理员权限（roleId=1），普通用户（roleId=0）和Root用户都无法访问
- **Root用户限制**：Root用户专注于用户管理功能，不具有线材数据管理权限 