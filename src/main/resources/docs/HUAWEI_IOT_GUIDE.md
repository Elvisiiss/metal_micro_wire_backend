# 华为云IoT完整指南

## 概述

本指南涵盖华为云IoT平台的连接配置和消息处理的完整流程，采用**灵活优先**的设计理念，能够适应华为云IoT平台的数据格式变化。

## 1. 连接配置

### 1.1 配置位置
华为云IoT相关配置位于 `application.yml` 文件中：

```yaml
# 华为云IoT连接配置
huawei:
  iot:
    amqp:
      host: 1ef972c084.st1.iotda-app.cn-north-4.myhuaweicloud.com
      port: 5671
      access-key: JOPvCH6V
      access-code: Y94aIdqb3BND7fFm6RqZTDieXn7I2bWE
      default-queue: DefaultQueue
      queue-prefetch: 100
      auto-acknowledge: true
      reconnect-delay: 3000
      max-reconnect-delay: 30000

# IoT消息处理配置
iot:
  message:
    enable-detailed-logging: true
    max-message-size: 1000000
```

### 1.2 配置参数说明

#### AMQP连接配置
- **host**: AMQP服务器地址，格式为 `{instance-id}.st1.iotda-app.{region}.myhuaweicloud.com`
- **port**: AMQP服务器端口，通常为5671（SSL）或5672（非SSL）
- **access-key**: 华为云IoT平台创建的应用访问密钥
- **access-code**: 与访问密钥对应的访问码
- **default-queue**: 接收设备消息的默认队列名称
- **queue-prefetch**: 客户端一次性从队列中获取的消息数量
- **auto-acknowledge**: 是否自动确认消息
- **reconnect-delay**: 连接断开后重连的初始延迟时间（毫秒）
- **max-reconnect-delay**: 重连延迟的最大值（毫秒）

#### 消息处理配置
- **enable-detailed-logging**: 是否启用详细日志（开发时建议开启，生产时建议关闭）
- **max-message-size**: 最大消息大小限制（字符数）

### 1.3 获取配置参数

#### 华为云IoT平台配置步骤
1. 登录华为云控制台
2. 进入IoT设备接入服务
3. 选择对应的实例
4. 在"应用管理"中创建或查看应用
5. 获取应用的访问密钥和访问码
6. 在实例详情页面找到AMQP接入点地址

#### 队列配置
- 在IoT平台的"规则引擎"中配置数据转发规则
- 将设备数据转发到指定的AMQP队列

### 1.4 环境变量配置（推荐）

为了提高安全性，建议使用环境变量配置敏感信息：

```yaml
huawei:
  iot:
    amqp:
      host: ${HUAWEI_IOT_HOST:default-host}
      port: ${HUAWEI_IOT_PORT:5671}
      access-key: ${HUAWEI_IOT_ACCESS_KEY:}
      access-code: ${HUAWEI_IOT_ACCESS_CODE:}
      default-queue: ${HUAWEI_IOT_DEFAULT_QUEUE:DefaultQueue}
```

启动应用时设置环境变量：
```bash
export HUAWEI_IOT_HOST=your-instance.st1.iotda-app.cn-north-4.myhuaweicloud.com
export HUAWEI_IOT_ACCESS_KEY=your-access-key
export HUAWEI_IOT_ACCESS_CODE=your-access-code
```

## 2. 消息处理

### 2.1 设计理念

采用**灵活优先**的设计理念，具有以下核心优势：

1. **格式兼容性**: 不依赖固定的DTO结构，支持消息格式演进
2. **错误容忍性**: 解析失败不会中断处理流程，保留原始数据
3. **简单配置**: 最小化配置项，易于使用和维护

### 2.2 处理流程

1. **接收消息**: 获取原始JSON字符串
2. **格式验证**: 验证JSON格式是否正确
3. **信息提取**: 安全地提取消息字段
4. **类型识别**: 自动识别消息类型（属性上报/设备事件等）
5. **业务处理**: 根据消息类型执行相应的处理逻辑
6. **统计记录**: 更新处理统计信息

### 2.3 消息处理示例

#### 属性上报消息处理
```java
private void handlePropertyReportMessage(String rawMessage, JsonNode messageNode) {
    // 安全地提取设备信息
    JsonNode notifyData = messageNode.get("notify_data");
    if (notifyData != null) {
        JsonNode header = notifyData.get("header");
        if (header != null) {
            String deviceId = getTextValue(header, "device_id");
            String productId = getTextValue(header, "product_id");
            log.info("设备ID: {}, 产品ID: {}", deviceId, productId);
        }
        
        JsonNode services = notifyData.path("body").path("services");
        if (services.isArray()) {
            for (JsonNode service : services) {
                String serviceId = getTextValue(service, "service_id");
                JsonNode properties = service.get("properties");
                log.info("服务ID: {}, 属性: {}", serviceId, properties);
                // TODO: 在这里添加具体的业务处理逻辑
            }
        }
    }
}
```

#### 典型消息格式
```json
{
  "resource": "device.property",
  "event": "report",
  "event_time": "20231201T120000Z",
  "notify_data": {
    "header": {
      "app_id": "your-app-id",
      "device_id": "device-001",
      "product_id": "product-001"
    },
    "body": {
      "services": [
        {
          "service_id": "temperature",
          "properties": {
            "value": 25.6,
            "unit": "°C"
          },
          "event_time": "20231201T120000Z"
        }
      ]
    }
  }
}
```

## 3. API接口

本系统提供了完整的IoT监听器管理API，支持启动、停止和状态查询功能。

### 3.1 启动监听器

#### 接口信息
- **请求方式**: `POST`
- **请求路径**: `/api/iot/start`
- **功能描述**: 启动IoT消息监听器，开始接收华为云IoT平台推送的消息

#### 请求示例
```http
POST /api/iot/start
Content-Type: application/json
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": "IoT消息监听已开启"
}
```

#### 响应说明
- **成功响应** (200): 监听器启动成功
- **失败响应** (500): 启动失败，返回具体错误信息

#### 特殊行为
- 如果已有监听器在运行，会先停止旧监听器再启动新的
- 启动成功后会自动记录启动时间和监听器ID
- 支持重复调用，不会产生资源泄漏

#### 使用场景
- 系统初始化时启动监听
- 重新连接华为云IoT平台
- 故障恢复后重启监听

### 3.2 停止监听器

#### 接口信息
- **请求方式**: `POST`
- **请求路径**: `/api/iot/stop`
- **功能描述**: 停止IoT消息监听器，断开与华为云IoT平台的连接

#### 请求示例
```http
POST /api/iot/stop
Content-Type: application/json
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": "IoT消息监听已关闭"
}
```

#### 响应说明
- **成功响应** (200): 监听器停止成功
- **失败响应** (500): 停止失败，返回具体错误信息

#### 特殊行为
- 如果当前没有运行的监听器，调用此接口不会报错
- 停止后会清理所有相关资源和统计信息
- 应用关闭时会自动调用此方法进行清理

#### 使用场景
- 系统维护前停止监听
- 临时断开IoT连接
- 故障排查时停止监听

### 3.3 查询监听器状态

#### 接口信息
- **请求方式**: `GET`
- **请求路径**: `/api/iot/status`
- **功能描述**: 获取当前IoT监听器的运行状态和消息处理统计信息

#### 请求示例
```http
GET /api/iot/status
```

#### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isListening": true,
    "listenerId": "DefaultQueue_1703123456789",
    "startTime": 1703123456789,
    "messageStats": {
      "totalCount": 150,
      "successCount": 145,
      "failedCount": 5,
      "successRate": 96.67
    }
  }
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `isListening` | boolean | 是否正在监听（true=监听中，false=已停止） |
| `listenerId` | string | 监听器唯一标识ID，停止时为null |
| `startTime` | long | 监听器启动时间戳（毫秒），停止时为null |
| `messageStats` | object | 消息处理统计信息 |
| `messageStats.totalCount` | long | 总共接收的消息数量 |
| `messageStats.successCount` | long | 成功处理的消息数量 |
| `messageStats.failedCount` | long | 处理失败的消息数量 |
| `messageStats.successRate` | double | 消息处理成功率（百分比） |

#### 状态示例

**监听器运行中**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isListening": true,
    "listenerId": "DefaultQueue_1703123456789",
    "startTime": 1703123456789,
    "messageStats": {
      "totalCount": 150,
      "successCount": 145,
      "failedCount": 5,
      "successRate": 96.67
    }
  }
}
```

**监听器已停止**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "isListening": false,
    "listenerId": null,
    "startTime": null,
    "messageStats": {
      "totalCount": 0,
      "successCount": 0,
      "failedCount": 0,
      "successRate": 0.0
    }
  }
}
```

#### 使用场景
- 前端页面实时显示监听器状态
- 系统健康检查和监控
- 故障诊断和性能分析
- 自动化运维脚本状态检查

#### 监控建议
- **状态轮询**: 建议每30秒查询一次状态
- **告警阈值**: 成功率低于95%时需要关注
- **运行时间**: 监控异常重启情况
- **消息量**: 监控消息处理量是否正常

### 3.4 API调用流程

#### 典型使用流程
```
1. 启动监听: POST /api/iot/start
2. 状态检查: GET /api/iot/status
3. 业务运行: (自动处理消息)
4. 定期监控: GET /api/iot/status
5. 停止监听: POST /api/iot/stop
```

#### 错误处理流程
```
1. 调用状态接口检查: GET /api/iot/status
2. 如果监听器异常，先停止: POST /api/iot/stop
3. 重新启动监听器: POST /api/iot/start
4. 验证启动成功: GET /api/iot/status
```

### 3.5 前端集成示例

#### JavaScript示例
```javascript
// 检查监听器状态
async function checkListenerStatus() {
  try {
    const response = await fetch('/api/iot/status');
    const result = await response.json();
    
    if (result.code === 200) {
      const status = result.data;
      console.log('监听状态:', status.isListening);
      console.log('成功率:', status.messageStats.successRate + '%');
      
      // 更新前端UI
      updateStatusUI(status);
    }
  } catch (error) {
    console.error('获取状态失败:', error);
  }
}

// 启动监听器
async function startListener() {
  try {
    const response = await fetch('/api/iot/start', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('启动成功:', result.data);
      // 启动后检查状态
      setTimeout(checkListenerStatus, 1000);
    } else {
      console.error('启动失败:', result.message);
    }
  } catch (error) {
    console.error('启动异常:', error);
  }
}

// 停止监听器
async function stopListener() {
  try {
    const response = await fetch('/api/iot/stop', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('停止成功:', result.data);
      // 停止后检查状态
      setTimeout(checkListenerStatus, 500);
    } else {
      console.error('停止失败:', result.message);
    }
  } catch (error) {
    console.error('停止异常:', error);
  }
}

// 定期检查状态（每30秒）
setInterval(checkListenerStatus, 30000);
```

## 4. 扩展开发

### 4.1 添加新的消息类型
```java
// 1. 添加类型检测
private boolean isNewMessageType(JsonNode messageNode) {
    String resource = getTextValue(messageNode, "resource");
    return "new.message.type".equals(resource);
}

// 2. 添加处理逻辑
private void handleNewMessageType(String rawMessage, JsonNode messageNode) {
    // 实现具体处理逻辑
}

// 3. 在主流程中调用
if (isNewMessageType(messageNode)) {
    handleNewMessageType(rawMessage, messageNode);
}
```

### 4.2 集成数据存储
```java
private void handlePropertyReportMessage(String rawMessage, JsonNode messageNode) {
    // 提取数据
    // ...
    
    // 保存到数据库
    messageRepository.save(rawMessage, extractedData);
}
```

## 5. 监控统计

### 5.1 自动统计
系统自动统计以下信息：
- 消息总数
- 处理成功数
- 处理失败数
- 成功率

每处理100条消息会自动输出一次统计报告。

### 5.2 日志级别
- **INFO**: 基本处理信息和统计
- **DEBUG**: 详细消息内容（可配置）
- **WARN**: 异常情况和性能问题
- **ERROR**: 处理失败和系统错误

## 6. 最佳实践

### 6.1 环境配置
- **开发环境**: 开启详细日志便于调试
- **生产环境**: 关闭详细日志提高性能

### 6.2 安全注意事项
1. **敏感信息**: 访问密钥和访问码不要提交到版本控制系统
2. **环境变量**: 使用环境变量配置敏感信息
3. **区域选择**: 确保选择正确的华为云区域
4. **连接限制**: 注意华为云IoT平台的连接数限制

### 6.3 性能优化
1. **日志控制**: 生产环境关闭详细日志
2. **消息大小**: 合理设置消息大小限制
3. **重连策略**: 避免频繁重连造成压力
4. **错误处理**: 单个消息失败不影响后续处理

## 7. 故障排查

### 7.1 连接问题
- 检查AMQP服务器地址和端口
- 验证访问密钥和访问码
- 确认网络连接和防火墙设置

### 7.2 消息处理问题
- 查看详细日志了解具体错误
- 检查消息格式是否符合预期
- 验证队列配置是否正确

### 7.3 性能问题
- 监控消息处理统计
- 调整队列预取数量
- 优化消息处理逻辑 