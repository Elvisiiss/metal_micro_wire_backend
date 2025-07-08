# DeepSeek工具调用解析错误修复文档

## 问题描述

Spring AI集成中的DeepSeek工具调用解析出现JsonParseException错误，错误信息为"Unexpected character ('<' (code 60)): expected a valid value"。

### 具体问题

1. 系统成功检测到标准OpenAI格式的工具调用并执行了get_current_time工具
2. 在工具调用结果响应中检测到DeepSeek特殊格式的工具调用，尝试递归处理
3. 使用正则表达式成功提取了4个工具调用：get_device_list、get_overall_statistics、get_quality_issues、get_manufacturer_ranking
4. 但在ChatServiceImpl.parseDeepSeekToolCalls方法的第736行解析JSON时失败

### 错误原因分析

DeepSeek返回的工具调用格式为：
```
<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>get_device_list
```json
{"status":"ON"}
```<｜tool▁call▁end｜>
<｜tool▁call▁begin｜>function<｜tool▁sep｜>get_overall_statistics
```json
{}
```<｜tool▁call▁end｜>
...
<｜tool▁calls▁end｜>
```

问题在于：
1. 当前的parseDeepSeekToolCalls方法期望的格式是：`function<｜tool▁sep｜>function_name<｜tool▁sep｜>arguments`
2. 但实际的DeepSeek格式是：`function<｜tool▁sep｜>function_name\n```json\n{...}\n```<｜tool▁call▁end｜>`
3. 在第819行，arguments包含了```json\n{...}\n```这样的格式，而不是纯JSON
4. 当这个包含markdown代码块的字符串被传递到第736行的objectMapper.readTree()时，就会出现JsonParseException

## 修复方案

### 1. 修改JSON验证逻辑

在parseDeepSeekToolCalls方法中，当使用正则表达式提取到内容后，先验证是否是有效的JSON格式：

```java
for (String pattern : patterns) {
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(content);
    if (m.find()) {
        String extractedContent = m.group(1).trim();
        log.info("使用模式 {} 成功提取工具调用内容：{}", pattern, extractedContent);
        
        // 验证提取的内容是否为有效JSON
        if (isValidJson(extractedContent)) {
            toolCallsJson = extractedContent;
            log.info("提取的内容是有效JSON，直接使用");
            break;
        } else {
            log.info("提取的内容不是有效JSON，将使用分隔符解析方法处理");
            // 不设置toolCallsJson，让后续的parseToolCallsWithSeparator处理
        }
    }
}
```

### 2. 增强分隔符解析方法

修改parseToolCallsWithSeparator方法，确保能够正确处理包含markdown代码块的函数名和参数：

```java
if (parts.length >= 2) {
    // 解析函数名和参数
    String functionNamePart = parts[1].trim();
    String arguments = parts.length > 2 ? parts[2].trim() : "{}";

    // 从函数名部分提取纯函数名（可能包含换行和markdown代码块）
    String functionName = extractFunctionName(functionNamePart);
    
    // 如果函数名部分包含参数，提取参数
    if (functionNamePart.contains("```")) {
        arguments = extractJsonFromMarkdown(functionNamePart);
    } else {
        // 提取JSON内容，处理markdown代码块格式
        arguments = extractJsonFromMarkdown(arguments);
    }

    // 验证JSON格式
    if (!isValidJson(arguments)) {
        log.warn("工具调用 {} 的参数不是有效的JSON格式，使用默认空对象：{}", functionName, arguments);
        arguments = "{}";
    }

    Map<String, Object> toolCall = new HashMap<>();
    toolCall.put("name", functionName);
    toolCall.put("arguments", arguments);
    toolCalls.add(toolCall);

    log.info("成功解析分隔符格式工具调用：{} with args: {}", functionName, arguments);
}
```

### 3. 新增辅助方法

#### extractFunctionName方法
从包含函数名和可能的其他内容的字符串中提取纯函数名：

```java
private String extractFunctionName(String functionNamePart) {
    if (functionNamePart == null || functionNamePart.trim().isEmpty()) {
        return "";
    }

    String cleaned = functionNamePart.trim();
    
    // 如果包含换行符，取第一行
    if (cleaned.contains("\n")) {
        cleaned = cleaned.split("\n")[0].trim();
    }
    
    // 如果包含markdown代码块标记，取代码块之前的部分
    if (cleaned.contains("```")) {
        cleaned = cleaned.split("```")[0].trim();
    }
    
    // 移除可能的空格和特殊字符
    cleaned = cleaned.replaceAll("[\\s\\r\\n]+", "").trim();
    
    log.debug("从 '{}' 提取函数名：'{}'", functionNamePart, cleaned);
    return cleaned;
}
```

#### extractJsonFromMarkdown方法
从markdown代码块中提取JSON内容：

```java
private String extractJsonFromMarkdown(String text) {
    if (text == null || text.trim().isEmpty()) {
        return "{}";
    }

    // 移除markdown代码块标记
    text = text.trim();
    
    // 处理```json\n{...}\n```格式
    if (text.contains("```json")) {
        java.util.regex.Pattern jsonPattern = java.util.regex.Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");
        java.util.regex.Matcher jsonMatcher = jsonPattern.matcher(text);
        if (jsonMatcher.find()) {
            String jsonContent = jsonMatcher.group(1).trim();
            log.debug("从markdown代码块中提取JSON：{}", jsonContent);
            return jsonContent;
        }
    }
    
    // 处理```\n{...}\n```格式
    if (text.contains("```")) {
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("```\\s*([\\s\\S]*?)\\s*```");
        java.util.regex.Matcher codeMatcher = codePattern.matcher(text);
        if (codeMatcher.find()) {
            String codeContent = codeMatcher.group(1).trim();
            // 检查是否是JSON格式
            if (codeContent.startsWith("{") || codeContent.startsWith("[")) {
                log.debug("从代码块中提取JSON：{}", codeContent);
                return codeContent;
            }
        }
    }
    
    // 如果没有代码块标记，直接返回原文本
    if (text.startsWith("{") || text.startsWith("[")) {
        return text;
    }
    
    // 默认返回空对象
    return "{}";
}
```

#### tryFixJsonFormat方法
尝试修复常见的JSON格式问题：

```java
private String tryFixJsonFormat(String jsonString) {
    if (jsonString == null || jsonString.trim().isEmpty()) {
        return null;
    }

    String fixed = jsonString.trim();
    
    try {
        // 1. 移除可能的HTML标签或特殊字符
        if (fixed.contains("<") && fixed.contains(">")) {
            fixed = fixed.replaceAll("<[^>]*>", "");
            log.debug("移除HTML标签后：{}", fixed);
        }
        
        // 2. 处理可能的转义问题
        if (fixed.contains("\\\"")) {
            fixed = fixed.replace("\\\"", "\"");
            log.debug("处理转义字符后：{}", fixed);
        }
        
        // 3. 确保是数组格式
        if (!fixed.startsWith("[") && !fixed.startsWith("{")) {
            // 查找第一个 { 或 [
            int firstBrace = fixed.indexOf("{");
            int firstBracket = fixed.indexOf("[");
            
            if (firstBrace >= 0 && (firstBracket < 0 || firstBrace < firstBracket)) {
                fixed = fixed.substring(firstBrace);
            } else if (firstBracket >= 0) {
                fixed = fixed.substring(firstBracket);
            }
            log.debug("提取JSON部分后：{}", fixed);
        }
        
        // 4. 确保结尾正确
        if (fixed.startsWith("[") && !fixed.endsWith("]")) {
            int lastBracket = fixed.lastIndexOf("]");
            if (lastBracket > 0) {
                fixed = fixed.substring(0, lastBracket + 1);
            } else {
                fixed = fixed + "]";
            }
            log.debug("修复数组结尾后：{}", fixed);
        } else if (fixed.startsWith("{") && !fixed.endsWith("}")) {
            int lastBrace = fixed.lastIndexOf("}");
            if (lastBrace > 0) {
                fixed = fixed.substring(0, lastBrace + 1);
            } else {
                fixed = fixed + "}";
            }
            log.debug("修复对象结尾后：{}", fixed);
        }
        
        // 5. 验证修复后的JSON
        if (isValidJson(fixed)) {
            return fixed;
        }
        
    } catch (Exception e) {
        log.debug("JSON修复过程中发生错误：{}", e.getMessage());
    }
    
    return null;
}
```

### 4. 增强错误处理和日志记录

在JSON解析部分添加更详细的错误处理：

```java
try {
    // 解析JSON格式的工具调用
    JsonNode toolCallsArray = objectMapper.readTree(toolCallsJson);
    // ... 处理逻辑
} catch (JsonProcessingException e) {
    log.error("解析工具调用JSON时发生错误，JSON内容：{}", toolCallsJson, e);
    log.error("JSON解析错误详情：{}", e.getMessage());
    
    // 尝试修复常见的JSON格式问题
    String fixedJson = tryFixJsonFormat(toolCallsJson);
    if (fixedJson != null && !fixedJson.equals(toolCallsJson)) {
        log.info("尝试修复JSON格式，修复后的内容：{}", fixedJson);
        try {
            JsonNode fixedArray = objectMapper.readTree(fixedJson);
            if (fixedArray.isArray()) {
                log.info("JSON修复成功，重新解析");
                // 递归调用自己处理修复后的JSON
                return parseDeepSeekToolCalls(fixedJson);
            }
        } catch (Exception fixException) {
            log.error("修复后的JSON仍然无法解析：{}", fixException.getMessage());
        }
    }
}
```

## 测试验证

创建了DeepSeekToolCallParsingTest测试类，验证修复后的功能：

1. **testParseDeepSeekToolCallsWithMarkdownFormat**: 测试实际的DeepSeek返回格式
2. **testExtractJsonFromMarkdown**: 测试JSON提取功能
3. **testIsValidJson**: 测试JSON验证功能
4. **testTryFixJsonFormat**: 测试JSON修复功能
5. **testContainsToolCallMarkers**: 测试工具调用标记检测

所有测试均通过，确保修复有效。

## 修复效果

修复后的系统能够：

1. **正确处理DeepSeek特殊格式**：成功解析包含markdown代码块的工具调用
2. **避免JsonParseException**：通过预验证和分隔符解析避免JSON解析错误
3. **保持向后兼容**：仍然支持标准OpenAI格式的工具调用
4. **提供详细日志**：便于调试和问题排查
5. **优雅降级**：当解析失败时提供合理的默认值

## 总结

通过增强JSON验证逻辑、改进分隔符解析方法、添加辅助函数和完善错误处理，成功修复了DeepSeek工具调用解析中的JsonParseException错误。修复后的系统既支持标准OpenAI格式也支持DeepSeek特殊格式的工具调用，提高了系统的健壮性和兼容性。
