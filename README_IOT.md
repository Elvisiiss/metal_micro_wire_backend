# 华为云IoT AMQP工具类

## 简介
精简的华为云IoT设备消息接收工具类，基于AMQP协议。

## 核心文件
- `HuaweiIotAmqpUtil.java` - 核心工具类
- `IoTDeviceMessage.java` - 消息DTO
- `IoTMessageService.java` - 消息处理服务
- `IoTController.java` - API控制器

## 快速使用

### 1. 配置连接参数
在 `HuaweiIotAmqpUtil.java` 中配置：
```java
private static final String HOST = "你的IoT平台域名";
private static final String ACCESS_KEY = "你的访问密钥";
private static final String ACCESS_CODE = "你的访问密码";
```

### 2. 启动监听器
```java
@Autowired
private HuaweiIotAmqpUtil iotUtil;

// 启动监听器
iotUtil.startDefaultQueueListener(message -> {
    log.info("收到消息: {}", message);
    // 处理消息逻辑
});
```

### 3. 使用API接口
```bash
# 开启IoT消息监听
POST /api/iot/start

# 关闭IoT消息监听  
POST /api/iot/stop
```

### 4. 使用服务层
```java
@Autowired
private IoTMessageService iotService;

// 启动服务
iotService.startMessageListener();

// 停止服务  
iotService.stopMessageListener();
```

## API接口
- `POST /api/iot/start` - 开启IoT消息监听
- `POST /api/iot/stop` - 关闭IoT消息监听

## 主要方法
- `testConnection()` - 测试连接
- `startDefaultQueueListener()` - 启动监听器
- `stopListener()` - 停止监听器
- `stopAllListeners()` - 停止所有监听器

## 消息格式
华为云IoT推送的标准JSON格式消息，会自动解析为 `IoTDeviceMessage` 对象。 