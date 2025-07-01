# IoT消息处理功能指南

## 功能概述

本系统实现了华为IoT设备消息的自动解析和存储功能，支持三种类型的设备属性上报消息：

1. **检测数据 (detection)**: 金属微丝的基础属性检测数据
2. **问题数据 (question)**: 设备发送的问题和查询
3. **状态数据 (status)**: 设备的运行状态信息

## 数据模型

### 1. 线材实体 (WireMaterial)

存储金属微丝检测数据，对应 `detection` 类型消息：

| 字段 | 类型 | 说明 |
|------|------|------|
| batchNumber | String | 批次卷序 - 主键 (Batch) |
| deviceId | String | 设备ID |
| diameter | BigDecimal | 金属丝直径 (DIR_s) |
| resistance | BigDecimal | 电导率 (RES_s) |
| extensibility | BigDecimal | 延展率 (EXT_s) |
| weight | BigDecimal | 重量 (WEI_s) |
| sourceOriginRaw | String | 原始生产信息（十六进制GBK编码） |
| manufacturer | String | 生产商（解析后） |
| responsiblePerson | String | 负责人（解析后） |
| processType | String | 工艺类型（解析后） |
| productionMachine | String | 生产机器（解析后） |
| contactEmail | String | 联系方式/邮箱（解析后） |
| eventTime | LocalDateTime | 事件发生时间 |
| createTime | LocalDateTime | 创建时间 |

### 2. 设备实体 (Device)

存储设备状态信息，对应 `status` 类型消息。每个设备只有一条记录，状态消息用于更新现有设备的状态：

| 字段 | 类型 | 说明 |
|------|------|------|
| deviceId | String | 设备ID - 主键 |
| status | String | 设备状态 (STATUS)，只允许 "ON" 或 "OFF" |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间（即事件发生时间） |

**处理逻辑**：
- 设备必须预先存在于系统中（通过设备管理接口或数据库直接创建）
- 状态消息仅用于更新现有设备的状态字段
- 状态值必须为 "ON" 或 "OFF"，其他值将被拒绝
- 如果设备不存在或状态值无效，直接丢弃消息并记录错误
- 每次更新自动刷新updateTime字段

**设备创建方式**：
设备记录需要通过以下方式预先创建：
1. 管理员通过设备管理API创建设备记录
2. 系统初始化时批量导入设备信息
3. 直接在数据库中插入设备记录

**示例流程**：
```
前提：设备 device_001 已存在于系统中

设备 device_001 发送状态消息: STATUS=ON
→ 更新记录: {deviceId: "device_001", status: "ON", updateTime: now}

设备 device_001 发送状态消息: STATUS=OFF  
→ 更新记录: {deviceId: "device_001", status: "OFF", updateTime: now}

设备 device_001 发送无效状态消息: STATUS=INVALID
→ 丢弃消息，记录错误："无效的设备状态: INVALID，只允许 ON 或 OFF"

不存在的设备 device_999 发送状态消息: STATUS=ON
→ 丢弃消息，记录错误："设备不存在: device_999"
```

### 3. 问题实体 (Question)

存储设备问题数据，对应 `question` 类型消息：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键ID |
| deviceId | String | 设备ID |
| questionContent | String | 用户问题内容 (AI字段) |
| responseStatus | Integer | 响应状态 (0: 未处理, 1: 已处理) |
| aiResponseContent | String | AI响应内容 |
| eventTime | LocalDateTime | 事件发生时间 |
| createTime | LocalDateTime | 创建时间 |
| responseTime | LocalDateTime | 响应时间 |

## 消息处理流程

### 1. 消息接收
- 通过华为IoT AMQP连接接收设备消息
- 消息格式为JSON，包含设备信息和属性数据

### 2. 消息解析
- 提取消息中的 `TYPE` 字段确定消息类型
- 根据类型调用相应的解析方法
- 提取设备ID和事件时间等通用信息

### 3. 数据存储
- **检测数据**：每次创建新的线材记录（以批次号为主键）
- **设备状态**：仅更新现有设备记录，设备不存在则丢弃消息（以设备ID为主键）
- **问题数据**：每次创建新的问题记录
- 使用事务确保数据一致性
- 处理失败不影响后续消息处理

### 4. 错误处理
- 单个消息解析失败不中断整个流程
- 统一的错误日志记录（在IoTMessageServiceImpl层）
- 统计处理成功率和失败率
- 设备不存在时丢弃消息，避免创建无效设备记录
- 无效状态值时丢弃消息，确保数据完整性

## 数据库表结构

### 创建表的SQL脚本

```sql
-- 线材数据表
CREATE TABLE wire_materials (
    batch_number VARCHAR(255) PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    diameter DECIMAL(10,2),
    resistance DECIMAL(10,2),
    extensibility DECIMAL(10,2),
    weight DECIMAL(10,2),
    source_origin_raw TEXT,
    manufacturer VARCHAR(255),
    responsible_person VARCHAR(255),
    process_type VARCHAR(255),
    production_machine VARCHAR(255),
    contact_email VARCHAR(255),
    event_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_id (device_id),
    INDEX idx_event_time (event_time),
    INDEX idx_manufacturer (manufacturer),
    INDEX idx_responsible_person (responsible_person),
    INDEX idx_process_type (process_type)
);

-- 设备状态表
CREATE TABLE devices (
    device_id VARCHAR(255) PRIMARY KEY COMMENT '设备ID - 主键',
    status VARCHAR(50) NOT NULL COMMENT '设备状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_update_time (update_time)
);

-- 问题数据表
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(255) NOT NULL COMMENT '设备ID',
    question_content TEXT COMMENT '用户问题内容',
    response_status INT NOT NULL DEFAULT 0 COMMENT '响应状态 (0: 未处理, 1: 已处理)',
    ai_response_content TEXT COMMENT 'AI响应内容',
    event_time DATETIME NOT NULL COMMENT '事件发生时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    response_time DATETIME COMMENT '响应时间',
    INDEX idx_device_id (device_id),
    INDEX idx_response_status (response_status),
    INDEX idx_event_time (event_time),
    INDEX idx_device_event_time (device_id, event_time),
    INDEX idx_device_status (device_id, response_status)
);
```

## 配置说明

### 华为IoT配置

在 `application.yml` 中配置华为IoT连接信息：

```yaml
huawei:
  iot:
    amqp:
      host: your-iot-host
      port: 5671
      access-key: your-access-key
      access-code: your-access-code
      project-id: your-project-id
    message:
      enable-detailed-logging: true
      max-message-size: 10240
```

### 数据库配置

配置MySQL数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## 消息示例

### detection类型消息 (检测数据)
```json
{
  "resource": "device.property",
  "event": "report",
  "event_time": "20250630T122121Z",
  "notify_data": {
    "header": {
      "device_id": "6857a366d582f2001833e1e6_8888"
    },
    "body": {
      "services": [{
        "service_id": "STM32F407VET6",
        "properties": {
          "TYPE": "detection",
          "DIR_s": "60.2",
          "RES_s": "20.1",
          "EXT_s": "56.4",
          "WEI_s": "23.8",
          "Batch": "Cu0120250629010010001",
          "SourceOrigin": "C9FAB2FAC9CC_D5C5C8FD_B9A4D2D5_C9FAB2FABBFAC6F7_333130363133333231364071712E636F6D"
        }
      }]
    }
  }
}
```

### status类型消息 (状态数据)
```json
{
  "resource": "device.property",
  "event": "report",
  "event_time": "20250630T122122Z",
  "notify_data": {
    "header": {
      "device_id": "6857a366d582f2001833e1e6_8888"
    },
    "body": {
      "services": [{
        "service_id": "STM32F407VET6",
        "properties": {
          "TYPE": "status",
          "STATUS": "ON"
        }
      }]
    }
  }
}
```

**STATUS字段说明**：
- 有效值：`"ON"`（开机）、`"OFF"`（关机）
- 大小写敏感，必须使用大写
- 其他值将被拒绝并记录错误

### question类型消息 (问题数据)
```json
{
  "resource": "device.property",
  "event": "report",
  "event_time": "20250630T122122Z",
  "notify_data": {
    "header": {
      "device_id": "6857a366d582f2001833e1e6_8888"
    },
    "body": {
      "services": [{
        "service_id": "STM32F407VET6",
        "properties": {
          "TYPE": "question",
          "AI": "C4E3BAC3"
        }
      }]
    }
  }
}
```

注意：AI字段实际包含的是用户提出的问题内容，系统将其解析为questionContent字段，而非AI的回复，同样需要进行GBK解码。

## 监控和统计

系统提供以下监控功能：

1. **消息处理统计**: 总数、成功数、失败数
2. **详细日志记录**: 可配置开关的详细处理日志
3. **错误容错**: 单个消息失败不影响整体流程
4. **定期统计报告**: 每100条消息输出一次统计信息

## 生产信息编码解析

### 编码格式说明

生产信息采用特殊的编码格式：

1. **格式**: `生产商_负责人_工艺类型_生产机器_联系方式`
2. **编码**: 每个部分都使用十六进制表示的GBK编码
3. **示例**: `C9FAB2FAC9CC_D5C5C8FD_B9A4D2D5_C9FAB2FABBFAC6F7_333130363133333231364071712E636F6D`
4. **解码结果**: `生产商_张三_工艺_生产机器_3106133216@qq.com`

### 编码工具类

使用 `EncodingUtil` 类进行编码转换：

```java
// 解析完整的生产信息
String[] sourceInfo = EncodingUtil.parseSourceOrigin(sourceOriginRaw);
// sourceInfo[0] = 生产商
// sourceInfo[1] = 负责人  
// sourceInfo[2] = 工艺类型
// sourceInfo[3] = 生产机器
// sourceInfo[4] = 联系方式

// 单独解码十六进制GBK字符串
String decoded = EncodingUtil.decodeGbkHexToUtf8("C9FAB2FAC9CC"); // 返回 "生产商"
```

## 扩展开发

### 添加新的消息类型

1. 在 `IoTMessageServiceImpl` 的 `handlePropertyReportMessage` 方法中添加新的case
2. 创建对应的实体类和Repository
3. 在 `IoTDataService` 中添加解析方法
4. 更新数据库表结构

### 自定义消息处理逻辑

可以通过实现 `IoTDataService` 接口来自定义消息处理逻辑，或者扩展现有的 `IoTDataServiceImpl` 类。

### 支持新的编码格式

如果生产信息的编码格式发生变化，可以扩展 `EncodingUtil` 类：

1. 添加新的解码方法
2. 更新 `parseSourceOrigin` 方法
3. 相应更新实体类字段

## 故障排查

### 常见问题

1. **消息解析失败**: 检查消息格式是否符合预期
2. **数据库连接错误**: 检查数据库配置和网络连接
3. **华为IoT连接失败**: 检查AMQP配置和网络连接
4. **数据类型转换错误**: 检查数值字段的格式
5. **设备不存在错误**: 确保设备已通过管理接口预先创建
6. **无效状态值错误**: 确保STATUS字段值为 "ON" 或 "OFF"

### 日志级别

建议在开发和测试阶段开启详细日志：

```yaml
huawei:
  iot:
    message:
      enable-detailed-logging: true

logging:
  level:
    com.mmw.metal_micro_wire_backend.service.impl: DEBUG
``` 