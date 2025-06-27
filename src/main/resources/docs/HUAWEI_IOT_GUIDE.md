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

### 3.1 启动监听
```http
POST /api/iot/start
```

### 3.2 停止监听
```http
POST /api/iot/stop
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