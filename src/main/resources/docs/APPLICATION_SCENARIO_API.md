# 应用场景管理API文档

## 概述

应用场景管理模块用于配置不同应用场景下线材的标准规范，包括电导率、延展率、重量、直径等参数的上下限值。

## 数据模型

### 应用场景实体 (ApplicationScenario)

- **scenarioCode**: 应用场景编号 (固定两位数字字符串，如"01", "02")
- **scenarioName**: 应用场景名称
- **wireType**: 线材类型 (Cu、Al、Ni、Ti、Zn)
- **conductivityMin/Max**: 电导率标准下限/上限
- **extensibilityMin/Max**: 延展率标准下限/上限 (%)
- **weightMin/Max**: 重量标准下限/上限 (g)
- **diameterMin/Max**: 直径标准下限/上限 (mm)
- **createTime**: 创建时间
- **updateTime**: 更新时间

## API接口

### 1. 分页查询应用场景列表

**GET** `/api/scenario/list`

**权限**: 已认证用户

**查询参数**:
- `page`: 页码，从0开始 (默认: 0)
- `size`: 每页大小 (默认: 10，最大: 100)
- `wireType`: 线材类型筛选 (可选，Cu/Al/Ni/Ti/Zn)
- `scenarioNameKeyword`: 应用场景名称关键词搜索 (可选)
- `sortBy`: 排序字段 (默认: createTime)
- `sortDirection`: 排序方向 (默认: desc)

**响应示例**:
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": {
    "scenarios": [
      {
        "scenarioCode": "01",
        "scenarioName": "高精密电子连接器",
        "wireType": "Cu",
        "conductivityMin": 58.0,
        "conductivityMax": 62.0,
        "extensibilityMin": 15.0,
        "extensibilityMax": 25.0,
        "weightMin": 0.5,
        "weightMax": 2.0,
        "diameterMin": 0.1,
        "diameterMax": 0.5,
        "createTime": "2025-01-15T10:30:00",
        "updateTime": "2025-01-15T10:30:00"
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

### 2. 根据应用场景编号查询

**GET** `/api/scenario/{scenarioCode}`

**权限**: 已认证用户

**路径参数**:
- `scenarioCode`: 应用场景编号 (两位数字)

### 3. 根据线材类型获取应用场景

**GET** `/api/scenario/wire-type/{wireType}`

**权限**: 已认证用户

**路径参数**:
- `wireType`: 线材类型 (Cu/Al/Ni/Ti/Zn)

**说明**: 返回指定线材类型的所有应用场景，不分页

### 4. 创建应用场景

**POST** `/api/scenario`

**权限**: 仅管理员用户 (roleId=1)

**请求体示例**:
```json
{
  "scenarioCode": "01",
  "scenarioName": "高精密电子连接器",
  "wireType": "Cu",
  "conductivityMin": 58.0,
  "conductivityMax": 62.0,
  "extensibilityMin": 15.0,
  "extensibilityMax": 25.0,
  "weightMin": 0.5,
  "weightMax": 2.0,
  "diameterMin": 0.1,
  "diameterMax": 0.5
}
```

**验证规则**:
- scenarioCode: 必须是两位数字
- scenarioName: 不能为空，最大100字符
- wireType: 必须是Cu、Al、Ni、Ti、Zn之一
- 所有数值字段必须大于0
- 下限值不能大于上限值

### 5. 更新应用场景

**PUT** `/api/scenario/{scenarioCode}`

**权限**: 仅管理员用户 (roleId=1)

**路径参数**:
- `scenarioCode`: 应用场景编号

**请求体**: 与创建请求相同，但不包含scenarioCode字段

### 6. 删除应用场景

**DELETE** `/api/scenario/{scenarioCode}`

**权限**: 仅管理员用户 (roleId=1)

**路径参数**:
- `scenarioCode`: 应用场景编号

## 线材批次卷序格式解析

根据线材批次卷序格式："Cu0120250629010010001"

- **位置1-2**: 线材类型 (Cu/Al/Ni/Ti/Zn)
- **位置3-4**: 应用场景编号 (对应本模块的scenarioCode)
- **位置5-12**: 时间信息 (20250629)
- **位置13-14**: 检测机器号 (01)
- **位置15-17**: 批次号 (001)
- **位置18-21**: 卷序 (0001)

## 错误码说明

- `success`: 操作成功
- `Error`: 操作失败，具体错误信息在msg字段中

## 数据库表结构

应用场景数据将存储在 `application_scenarios` 表中，JPA会自动创建表结构。

## 使用建议

1. **初始化数据**: 系统启动后，建议先通过管理员账户创建常用的应用场景配置
2. **数据验证**: 创建和更新时会自动验证上下限值的合理性
3. **分页查询**: 使用线材类型筛选和名称关键词搜索可以快速定位所需场景
4. **权限控制**: 只有管理员可以创建、更新、删除应用场景，普通用户只能查询 