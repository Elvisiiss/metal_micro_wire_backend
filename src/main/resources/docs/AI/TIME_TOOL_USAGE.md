# 时间工具使用说明

## 概述

新增的时间工具 `get_current_time` 为AI助手提供了获取当前系统时间和日期信息的能力，支持多种时间格式输出，满足不同场景的时间查询需求。

## 工具功能

### 基本信息
- **工具名称**: `get_current_time`
- **功能描述**: 获取当前系统时间和日期信息
- **返回格式**: JSON格式的时间数据

### 支持的时间格式

| 格式类型 | 参数值 | 输出示例 | 说明 |
|---------|--------|----------|------|
| 完整日期时间 | `datetime` | `2024-01-15 14:30:25` | 默认格式，包含日期和时间 |
| 仅日期 | `date` | `2024-01-15` | 只显示日期部分 |
| 仅时间 | `time` | `14:30:25` | 只显示时间部分 |
| 时间戳 | `timestamp` | `1705298425000` | Unix时间戳（毫秒） |

## 参数说明

### 输入参数

| 参数名 | 类型 | 必需 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `format` | string | 否 | `datetime` | 时间格式类型 |
| `timezone` | string | 否 | `Asia/Shanghai` | 时区设置 |

### 输出数据结构

```json
{
  "currentTime": "2024-01-15 14:30:25",
  "timezone": "Asia/Shanghai",
  "timestamp": 1705298425000,
  "formattedTime": "2024-01-15 14:30:25",
  "description": "当前日期和时间",
  "year": 2024,
  "month": 1,
  "day": 15,
  "hour": 14,
  "minute": 30,
  "second": 25,
  "dayOfWeek": "MONDAY",
  "dayOfYear": 15
}
```

## 使用场景

### 1. 基本时间查询
**用户问题**: "现在几点了？"
**AI响应**: 调用时间工具获取当前时间，回答用户当前的具体时间。

### 2. 日期查询
**用户问题**: "今天是几号？"
**AI响应**: 调用时间工具获取当前日期，告知用户今天的日期。

### 3. 星期查询
**用户问题**: "今天是星期几？"
**AI响应**: 调用时间工具获取星期信息，告知用户今天是星期几。

### 4. 时间戳查询
**用户问题**: "当前的时间戳是多少？"
**AI响应**: 调用时间工具获取时间戳，提供给用户当前的Unix时间戳。

### 5. 业务场景应用
- **数据分析**: "分析今天的检测数据"
- **报告生成**: "生成本月的质量报告"
- **时间范围查询**: "查看最近一周的设备状态"

## 技术实现

### 工具定义
```java
private ChatToolCall.Tool createGetCurrentTimeTool() {
    ChatToolCall.Tool tool = new ChatToolCall.Tool();
    ChatToolCall.Function function = new ChatToolCall.Function();
    function.setName("get_current_time");
    function.setDescription("获取当前系统时间和日期信息");
    
    // 参数定义...
    return tool;
}
```

### 工具执行
```java
private String executeGetCurrentTime(JsonNode args) {
    // 获取参数
    String format = args.has("format") ? args.get("format").asText() : "datetime";
    String timezone = args.has("timezone") ? args.get("timezone").asText() : "Asia/Shanghai";
    
    // 获取当前时间并格式化
    LocalDateTime now = LocalDateTime.now();
    
    // 构建响应数据...
    return objectMapper.writeValueAsString(timeInfo);
}
```

## 测试用例

### Postman测试
项目提供了完整的Postman测试集合，包含以下时间工具测试用例：

1. **获取当前时间**: 测试基本的时间查询功能
2. **获取日期格式时间**: 测试日期格式输出
3. **获取时间戳**: 测试时间戳格式输出

### 单元测试
```java
@Test
void testExecuteGetCurrentTimeWithDefaultFormat() {
    // 测试默认格式的时间获取
    ChatToolCall.ToolCallRequest request = createTimeToolRequest("{}");
    ChatToolCall.ToolCallResult result = chatToolService.executeToolCall(request, 1L);
    
    assertTrue(result.isSuccess());
    // 验证返回数据...
}
```

## 注意事项

1. **时区处理**: 默认使用Asia/Shanghai时区，确保时间显示符合中国用户习惯
2. **格式一致性**: 所有时间格式都遵循ISO 8601标准
3. **性能考虑**: 时间获取操作非常轻量，不会影响系统性能
4. **错误处理**: 包含完善的异常处理机制，确保工具调用的稳定性

## 扩展建议

未来可以考虑扩展以下功能：
- 支持更多时区选择
- 添加时间计算功能（如时间差计算）
- 支持自定义时间格式
- 添加节假日判断功能
- 集成农历时间显示

## 相关文档

- [AI工具调用功能文档](CHAT_TOOL_FUNCTIONS.md)
- [聊天API文档](CHAT_API.md)
- [Postman测试集合](postman/Chat_API_Tool_Tests.json)
