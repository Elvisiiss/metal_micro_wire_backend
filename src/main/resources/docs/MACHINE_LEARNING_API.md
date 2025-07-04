# 机器学习质量评估API文档

## 📋 系统更新概述

本次更新为金属微丝质量评估系统集成了机器学习模型，实现了规则引擎与AI模型的双重评估机制。

### 🔄 核心更新内容

#### **1. 数据模型扩展**
- **WireMaterial实体**：新增3个评估字段
  - `modelEvaluationResult` - 机器学习模型评估结果
  - `modelConfidence` - 模型评估置信度（0-1）
  - `finalEvaluationResult` - 综合评估的最终结果
- **WireMaterialResponse DTO**：更新响应结构，包含完整评估信息

#### **2. 服务层架构**
- **MachineLearningService** - ML模型API调用服务
- **QualityEvaluationService** - 综合质量评估服务
- **MachineLearningHealthCheckService** - 自动健康检查服务

#### **3. 智能评估机制**
```
IoT数据上报 → 规则引擎评估 → 机器学习模型评估 → 智能决策 → 最终结果
```

**智能决策逻辑**：
- ✅ **自动确定**：规则引擎与模型一致 且 置信度≥0.8
- ⚠️ **人工审核**：结果不一致、置信度低、模型失败
- 🔄 **高可用性**：模型服务不可用时自动回退到规则引擎

## 🚀 API接口详细说明

### 1. 机器学习模型预测接口

#### 1.1 单个预测
**接口地址**：`POST /api/quality/predict`  
**权限要求**：无需认证  
**功能说明**：使用机器学习模型对单个线材样本进行质量预测

**请求参数**：
```json
{
  "scenarioCode": "01",        // 应用场景代码（必填，2位数字字符串）
  "conductivity": 58.0,        // 电导率（必填，数值）
  "extensibility": 35.0,       // 延展率（必填，数值）
  "diameter": 5.0             // 直径（必填，数值）
}
```

**成功响应**：
```json
{
  "msg": "预测成功",
  "code": "success",
  "data": {
    "prediction": "合格",                    // 预测结果："合格"或"不合格"
    "probability": {
      "不合格": 0.15,                       // 不合格概率
      "合格": 0.85                          // 合格概率
    },
    "confidence": 0.85,                     // 置信度（0-1）
    "input": {                              // 输入数据回显
      "scenarioCode": "01",
      "conductivity": 58.0,
      "extensibility": 35.0,
      "diameter": 5.0
    },
    "timestamp": "2024-01-15T10:30:00"      // 预测时间戳
  }
}
```

**错误响应**：
```json
{
  "msg": "预测失败：模型服务不可用",
  "code": "Error",
  "data": null
}
```

**cURL示例**：
```bash
curl -X POST http://localhost:8080/api/quality/predict \
  -H "Content-Type: application/json" \
  -d '{
    "scenarioCode": "01",
    "conductivity": 58.0,
    "extensibility": 35.0,
    "diameter": 5.0
  }'
```

#### 1.2 批量预测
**接口地址**：`POST /api/quality/predict/batch`  
**权限要求**：无需认证  
**功能说明**：批量预测多个线材样本的质量

**请求参数**：
```json
{
  "samples": [
    {
      "scenarioCode": "01",
      "conductivity": 58.0,
      "extensibility": 35.0,
      "diameter": 5.0
    },
    {
      "scenarioCode": "11", 
      "conductivity": 0.60,
      "extensibility": 10.0,
      "diameter": 15.0
    }
  ]
}
```

**成功响应**：
```json
{
  "msg": "批量预测成功",
  "code": "success",
  "data": [
    {
      "prediction": "合格",
      "probability": {
        "不合格": 0.15,
        "合格": 0.85
      },
      "confidence": 0.85,
      "input": {
        "scenarioCode": "01",
        "conductivity": 58.0,
        "extensibility": 35.0,
        "diameter": 5.0
      },
      "timestamp": "2024-01-15T10:30:00"
    },
    {
      "prediction": "合格",
      "probability": {
        "不合格": 0.25,
        "合格": 0.75
      },
      "confidence": 0.75,
      "input": {
        "scenarioCode": "11",
        "conductivity": 0.60,
        "extensibility": 10.0,
        "diameter": 15.0
      },
      "timestamp": "2024-01-15T10:30:00"
    }
  ]
}
```

### 2. 系统健康检查接口

#### 2.1 机器学习模型健康检查
**接口地址**：`GET /api/quality/health`  
**权限要求**：无需认证  
**功能说明**：检查机器学习模型服务的健康状态

**请求参数**：无

**成功响应**：
```json
{
  "msg": "机器学习模型服务正常",
  "code": "success",
  "data": true
}
```

**失败响应**：
```json
{
  "msg": "机器学习模型服务异常",
  "code": "Error",
  "data": false
}
```

### 3. 质量评估管理接口

#### 3.1 重新评估指定场景（新增）
**接口地址**：`POST /api/quality/scenario/{scenarioCode}/re-evaluate`  
**权限要求**：管理员（roleId=1）  
**功能说明**：重新评估指定应用场景下的所有线材数据（规则引擎+机器学习模型）

**路径参数**：
- `scenarioCode` - 应用场景代码（如：01, 02, 11等）

**请求头**：
```
Authorization: Bearer {admin_token}
```

**成功响应**：
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": "应用场景 01 下的线材数据重新评估完成，共处理 150 条数据"
}
```

**错误响应**：
```json
{
  "msg": "权限不足",
  "code": "Error",
  "data": null
}
```

#### 3.2 获取待审核线材列表（分页）
**接口地址**：`GET /api/quality/pending-review`  
**权限要求**：已认证用户  
**功能说明**：分页获取需要人工处理的线材列表，包括未评估(UNKNOWN)和待人工审核(PENDING_REVIEW)状态的线材

**请求头**：
```
Authorization: Bearer {token}
```

**查询参数**：
- `page`: 页码，从0开始 (默认: 0)
- `size`: 每页大小 (默认: 10，最大: 100)
- `sortBy`: 排序字段 (默认: createTime)
- `sortDirection`: 排序方向 (默认: desc)

**成功响应**：
```json
{
  "msg": "获取待审核线材成功",
  "code": "success",
  "data": {
    "wireMaterials": [
      {
        "batchNumber": "Cu0120250629010010001",
        "deviceId": "device001",
        "diameter": 5.0,
        "resistance": 58.0,
        "extensibility": 35.0,
        "weight": 1.5,
        "evaluationResult": "PASS",              // 规则引擎结果
        "evaluationMessage": "直径符合标准",
        "modelEvaluationResult": "FAIL",         // 模型评估结果
        "modelConfidence": 0.65,                 // 低置信度
        "finalEvaluationResult": "PENDING_REVIEW", // 需要审核
        "scenarioCode": "01",
        "eventTime": "2024-01-15T10:30:00",
        "createTime": "2024-01-15T10:35:00"
      },
      {
        "batchNumber": "Cu0120250629010010002",
        "deviceId": "device002",
        "diameter": 4.5,
        "resistance": 55.0,
        "extensibility": 30.0,
        "weight": 1.2,
        "evaluationResult": "UNKNOWN",           // 未进行规则引擎评估
        "evaluationMessage": null,
        "modelEvaluationResult": "UNKNOWN",      // 未进行模型评估
        "modelConfidence": null,
        "finalEvaluationResult": "UNKNOWN",      // 未评估状态
        "scenarioCode": "01",
        "eventTime": "2024-01-15T09:45:00",
        "createTime": "2024-01-15T09:50:00"
      }
    ],
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 1,
    "totalElements": 2,
    "first": true,
    "last": true
  }
}
```

**返回数据说明**：
- **PENDING_REVIEW**：经过初步评估但需要人工审核的线材（如规则引擎和模型结果不一致、置信度过低等）
- **UNKNOWN**：尚未进行任何评估的线材（如刚接收到IoT数据但还未处理）

#### 3.3 获取已完成评估线材列表（分页）
**接口地址**：`GET /api/quality/completed`  
**权限要求**：已认证用户  
**功能说明**：分页获取已完成评估的线材列表，包括自动确定为合格(PASS)和不合格(FAIL)状态的线材，支持应用场景筛选和置信度排序

**请求头**：
```
Authorization: Bearer {token}
```

**查询参数**：
- `page`: 页码，从0开始 (默认: 0)
- `size`: 每页大小 (默认: 10，最大: 100)
- `scenarioCode`: 应用场景编号筛选 (可选，如：01, 02, 11等)
- `sortBy`: 排序字段 (默认: createTime，支持: createTime, modelConfidence)
- `sortDirection`: 排序方向 (默认: desc，对于置信度排序：desc=高置信度优先，asc=低置信度优先)

**成功响应**：
```json
{
  "msg": "获取已完成评估线材成功",
  "code": "success",
  "data": {
    "wireMaterials": [
      {
        "batchNumber": "Cu0120250629010010003",
        "deviceId": "device003",
        "diameter": 5.2,
        "resistance": 59.0,
        "extensibility": 36.0,
        "weight": 1.6,
        "evaluationResult": "PASS",              // 规则引擎结果
        "evaluationMessage": "所有指标符合标准",
        "modelEvaluationResult": "PASS",         // 模型评估结果
        "modelConfidence": 0.92,                 // 高置信度
        "finalEvaluationResult": "PASS",         // 自动确定为合格
        "scenarioCode": "01",
        "eventTime": "2024-01-15T08:30:00",
        "createTime": "2024-01-15T08:35:00"
      },
      {
        "batchNumber": "Cu0120250629010010004",
        "deviceId": "device004",
        "diameter": 3.8,
        "resistance": 45.0,
        "extensibility": 20.0,
        "weight": 0.8,
        "evaluationResult": "FAIL",              // 规则引擎结果
        "evaluationMessage": "直径不符合标准",
        "modelEvaluationResult": "FAIL",         // 模型评估结果
        "modelConfidence": 0.88,                 // 高置信度
        "finalEvaluationResult": "FAIL",         // 自动确定为不合格
        "scenarioCode": "01",
        "eventTime": "2024-01-15T07:15:00",
        "createTime": "2024-01-15T07:20:00"
      }
    ],
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalElements": 48,
    "first": true,
    "last": false
  }
}
```

**返回数据说明**：
- **PASS**：规则引擎和模型评估一致且置信度≥0.8，自动确定为合格的线材
- **FAIL**：规则引擎和模型评估一致且置信度≥0.8，自动确定为不合格的线材

#### 3.4 人工审核确认（新增）
**接口地址**：`POST /api/quality/confirm-result`  
**权限要求**：管理员（roleId=1）  
**功能说明**：人工审核确认线材的最终评估结果，支持对待审核状态和已完成状态的线材进行重新审核

**请求头**：
```
Authorization: Bearer {admin_token}
Content-Type: application/x-www-form-urlencoded
```

**请求参数**：
```
batchNumber=Cu0120250629010010001&finalResult=PASS&reviewRemark=人工确认合格
```

**参数说明**：
- `batchNumber` - 批次号（必填）
- `finalResult` - 最终结果（必填，枚举值：PASS/FAIL/PENDING_REVIEW/UNKNOWN）
- `reviewRemark` - 审核备注（可选）

**成功响应**：
```json
{
  "msg": "人工审核确认成功",
  "code": "success",
  "data": null
}
```

### 4. 线材管理接口扩展

#### 4.1 手动评估线材质量（新增）
**接口地址**：`POST /api/wire-material/{batchNumber}/evaluate`  
**权限要求**：管理员（roleId=1）  
**功能说明**：手动触发对指定线材的综合质量评估

**路径参数**：
- `batchNumber` - 线材批次号

**请求头**：
```
Authorization: Bearer {admin_token}
```

**成功响应**：
```json
{
  "msg": "线材质量评估完成",
  "code": "success", 
  "data": {
    "batchNumber": "Cu0120250629010010001",
    "deviceId": "device001",
    "diameter": 5.0,
    "resistance": 58.0,
    "extensibility": 35.0,
    "weight": 1.5,
    "manufacturer": "某某制造公司",
    "responsiblePerson": "张三",
    "processType": "拉丝工艺",
    "productionMachine": "拉丝机001",
    "contactEmail": "contact@example.com",
    "scenarioCode": "01",
    "deviceCode": "10",
    "eventTime": "2025-01-15T10:30:00",
    "createTime": "2025-01-15T10:35:00",
    // === 新增评估字段 ===
    "evaluationResult": "PASS",              // 规则引擎评估结果
    "evaluationMessage": "规则引擎：直径符合标准",
    "modelEvaluationResult": "PASS",         // 模型评估结果
    "modelConfidence": 0.85,                 // 模型置信度
    "finalEvaluationResult": "PASS"          // 最终评估结果
  }
}
```

**错误响应**：
```json
{
  "msg": "线材记录不存在：Cu0120250629010010001",
  "code": "Error",
  "data": null
}
```

#### 4.2 线材查询接口更新
**现有接口**：`GET /api/wire-material/{batchNumber}` 和 `GET /api/wire-material/list`

**更新内容**：响应中新增评估相关字段
```json
{
  "msg": "操作成功",
  "code": "success",
  "data": {
    // 原有字段...
    "batchNumber": "Cu0120250629010010001",
    "deviceId": "device001",
    // ... 其他原有字段
    
    // === 新增评估字段 ===
    "evaluationResult": "PASS",              // 规则引擎评估结果
    "evaluationMessage": "直径符合标准",
    "modelEvaluationResult": "PASS",         // 模型评估结果  
    "modelConfidence": 0.85,                 // 模型置信度
    "finalEvaluationResult": "PASS"          // 最终评估结果
  }
}
```

## 📊 数据结构详细说明

### 评估结果字段

| 字段名 | 数据类型 | 说明 | 可能值 |
|--------|----------|------|--------|
| evaluationResult | EvaluationResult | 规则引擎评估结果 | PASS, FAIL, UNKNOWN |
| evaluationMessage | String | 评估详情信息 | 文本描述，如"直径符合标准" |
| modelEvaluationResult | EvaluationResult | 模型评估结果 | PASS, FAIL, UNKNOWN |
| modelConfidence | BigDecimal | 模型置信度 | 0.0000-1.0000 |
| finalEvaluationResult | FinalEvaluationResult | 最终评估结果 | PASS, FAIL, PENDING_REVIEW, UNKNOWN |

### 枚举类型定义

#### EvaluationResult（评估结果）
- **PASS**：合格
- **FAIL**：不合格
- **UNKNOWN**：未评估

#### FinalEvaluationResult（最终评估结果）
- **PASS**：合格
- **FAIL**：不合格
- **PENDING_REVIEW**：待人工审核
- **UNKNOWN**：未评估

### 智能决策规则矩阵

| 规则引擎结果 | 模型结果 | 模型置信度 | 最终结果 | 说明 |
|-------------|----------|-----------|----------|------|
| PASS | PASS | ≥0.8 | PASS | 自动确定合格 |
| FAIL | FAIL | ≥0.8 | FAIL | 自动确定不合格 |
| PASS | FAIL | 任意 | PENDING_REVIEW | 结果不一致，需审核 |
| FAIL | PASS | 任意 | PENDING_REVIEW | 结果不一致，需审核 |
| 任意 | 任意 | <0.8 | PENDING_REVIEW | 置信度低，需审核 |
| 任意 | UNKNOWN | 任意 | PENDING_REVIEW | 模型失败，需审核 |

## ⚙️ 配置参数详细说明

```yaml
ml:
  model:
    api:
      url: http://localhost:5000          # Python ML API服务地址
      timeout: 30000                      # HTTP请求超时时间（毫秒）
    confidence:
      threshold: 0.8                      # 置信度阈值（0-1），低于此值需人工审核
    enabled: true                         # 是否启用机器学习模型评估功能
    health-check:
      interval: 5                         # 定期健康检查间隔（分钟）
      timeout: 5000                       # 健康检查超时时间（毫秒）
```

**配置说明**：
- **url**：确保Python机器学习API服务在此地址运行
- **threshold**：建议值0.7-0.9，值越高则更多数据需要人工审核
- **enabled**：设为false可禁用ML功能，仅使用规则引擎
- **interval**：建议1-10分钟，太频繁会增加系统负载

## 🔄 自动化流程详解

### 1. 人工审核使用场景

#### 待审核数据（PENDING_REVIEW/UNKNOWN）
- 规则引擎与模型评估结果不一致
- 模型置信度低于阈值（默认0.8）
- 模型评估失败或服务不可用
- 新接收到的IoT数据尚未处理

#### 已完成数据重新审核（PASS/FAIL）
- 后续发现产品质量问题，需要追溯数据
- 客户反馈与评估结果不符
- 专家发现评估标准或模型存在问题
- 定期抽检验证自动化评估准确性
- 特殊批次或重要订单的二次确认

### 2. IoT数据自动评估流程
```
IoT设备上报 → 数据解析 → 规则引擎评估 → ML模型评估 → 智能决策 → 保存结果
     ↓              ↓            ↓            ↓           ↓         ↓
  原始数据      结构化数据    规则引擎结果   模型结果+置信度  最终结果   数据库存储
```

**流程说明**：
1. **数据解析**：从IoT消息中提取线材属性
2. **规则引擎评估**：基于应用场景标准进行范围检查
3. **ML模型评估**：调用Python API进行AI预测
4. **智能决策**：根据决策矩阵确定最终结果
5. **结果保存**：完整记录所有评估过程和结果

### 3. 启动时健康检查
应用启动后自动执行：
```bash
🔍 开始检查机器学习模型服务健康状态...
✅ 机器学习模型服务连接正常
📊 质量评估功能：规则引擎 + 机器学习模型 双重保障
```

### 4. 定期健康监控
- **检查频率**：每5分钟（可配置）
- **检查内容**：ML API服务可用性
- **失败处理**：记录日志，但不中断服务
- **恢复检测**：服务恢复时自动记录

## 🎯 人工审核双重保障

### 审核流程设计理念
质量评估系统采用"自动化为主，人工为辅"的设计理念，提供**双重审核机制**：

#### 第一重：智能筛选
- **自动通过**：规则引擎和模型一致且置信度高的数据
- **待审核**：存在争议或置信度低的数据

#### 第二重：人工监督
- **首次审核**：处理待审核状态的数据
- **重新审核**：对已完成数据进行二次确认
- **权威覆盖**：人工判断优先于自动化结果

### 审核权限与追溯
- **权限控制**：仅管理员可执行审核操作
- **完整记录**：所有审核过程和结果变更均被记录
- **可追溯性**：支持查看审核历史和变更原因

## 🛠️ 完整测试示例

### 1. 基础功能测试

```bash
# 1. 检查系统健康状态
curl -X GET http://localhost:8080/api/quality/health

# 2. 单个预测测试
curl -X POST http://localhost:8080/api/quality/predict \
  -H "Content-Type: application/json" \
  -d '{
    "scenarioCode": "01",
    "conductivity": 58.0,
    "extensibility": 35.0,
    "diameter": 5.0
  }'

# 3. 批量预测测试
curl -X POST http://localhost:8080/api/quality/predict/batch \
  -H "Content-Type: application/json" \
  -d '{
    "samples": [
      {"scenarioCode": "01", "conductivity": 58.0, "extensibility": 35.0, "diameter": 5.0},
      {"scenarioCode": "11", "conductivity": 0.60, "extensibility": 10.0, "diameter": 15.0}
    ]
  }'
```

### 2. 管理功能测试（需要管理员权限）

```bash
# 登录获取管理员token
ADMIN_TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | jq -r '.data.token')

# 1. 手动评估线材
curl -X POST http://localhost:8080/api/wire-material/Cu0120250629010010001/evaluate \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 2. 重新评估场景
curl -X POST http://localhost:8080/api/quality/scenario/01/re-evaluate \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. 获取待审核列表（分页）
curl -X GET "http://localhost:8080/api/quality/pending-review?page=0&size=20&sortBy=createTime&sortDirection=desc" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. 获取已完成评估列表（分页）
curl -X GET "http://localhost:8080/api/quality/completed?page=0&size=20&scenarioCode=01&sortBy=modelConfidence&sortDirection=desc" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5. 获取特定场景的低置信度数据（用于质量抽检）
curl -X GET "http://localhost:8080/api/quality/completed?scenarioCode=01&sortBy=modelConfidence&sortDirection=asc&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5. 人工审核确认（待审核数据）
curl -X POST http://localhost:8080/api/quality/confirm-result \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "batchNumber=Cu0120250629010010001&finalResult=PASS&reviewRemark=人工确认合格"

# 6. 人工重新审核（已完成数据）
curl -X POST http://localhost:8080/api/quality/confirm-result \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "batchNumber=Cu0120250629010010003&finalResult=FAIL&reviewRemark=发现表面缺陷，重新判定为不合格"
```

## 🎯 系统优势与特点

### 1. 智能边界处理
- **规则引擎**：基于硬性标准，边界处理一刀切
- **机器学习**：基于历史数据学习，边界处理更灵活
- **双重验证**：两种方法互相验证，提高准确性

### 2. 高可用性设计
- **服务降级**：ML服务不可用时自动回退到规则引擎
- **容错机制**：单个组件失败不影响整体流程
- **健康监控**：实时监控各服务状态

### 3. 人工智能辅助
- **智能决策**：自动判断是否需要人工介入
- **置信度评估**：提供模型预测的可信程度
- **全面审核流程**：支持待审核数据的人工确认和已完成数据的重新审核
- **审核权威性**：人工专家判断优先，可覆盖自动化评估结果

### 4. 完整性和可追溯
- **全程记录**：保存完整的评估过程和结果
- **多维评估**：规则引擎、模型、人工三重保障
- **数据完整**：所有评估信息永久保存

## 🆘 故障处理与运维

### 1. 常见故障及处理

#### ML模型服务不可用
**现象**：健康检查失败，预测接口返回错误
**处理**：
- 系统自动回退到规则引擎评估
- 最终评估结果标记为PENDING_REVIEW
- 检查Python ML API服务状态
- 查看网络连接和配置

**日志示例**：
```
⚠️ 机器学习模型服务连接失败
🔄 质量评估功能：仅使用规则引擎评估
```

#### 低置信度数据过多
**现象**：大量数据标记为PENDING_REVIEW
**处理**：
- 检查模型训练数据质量
- 调整置信度阈值配置
- 增加人工审核人员
- 分析低置信度数据特征

#### 结果不一致率过高
**现象**：规则引擎与模型结果经常不一致
**处理**：
- 检查应用场景标准设置
- 重新训练机器学习模型
- 分析不一致数据的共同特征
- 优化规则引擎参数

### 2. 监控指标

#### 系统性能指标
- **API响应时间**：单次预测 < 100ms，批量预测 < 500ms
- **服务可用性**：> 99.9%
- **健康检查成功率**：> 95%

#### 评估质量指标
- **自动确定率**：目标 > 80%（减少人工审核负担）
- **模型置信度分布**：高置信度(>0.8) > 70%
- **一致性比率**：规则引擎与模型一致率 > 85%

### 3. 日常维护

#### 定期检查项目
- [ ] 机器学习模型服务健康状态
- [ ] 待审核数据数量和趋势
- [ ] 评估结果统计分析
- [ ] 系统性能监控数据

#### 配置优化建议
- **置信度阈值**：根据实际情况调整0.7-0.9
- **健康检查间隔**：生产环境建议5-10分钟
- **超时时间**：根据网络环境调整30-60秒

---

## 📝 版本更新记录

**v2.0.0** - 机器学习集成版本
- ✅ 新增机器学习模型评估功能
- ✅ 实现双重评估机制（规则引擎 + ML模型）
- ✅ 添加智能决策和人工审核流程
- ✅ 新增7个API接口
- ✅ 扩展数据模型，新增3个评估字段
- ✅ 实现自动健康检查和监控
- ✅ 支持配置化的置信度阈值管理

**主要新增接口**：
1. POST /api/quality/predict - 单个预测
2. POST /api/quality/predict/batch - 批量预测
3. GET /api/quality/health - 健康检查
4. POST /api/quality/scenario/{scenarioCode}/re-evaluate - 重新评估
5. GET /api/quality/pending-review - 待审核列表（分页，包括未评估和待人工审核状态）
6. GET /api/quality/completed - 已完成评估列表（分页，支持场景筛选和置信度排序）
7. POST /api/quality/confirm-result - 人工审核确认（支持重新审核已完成数据）
8. POST /api/wire-material/{batchNumber}/evaluate - 手动评估

**兼容性**：完全向后兼容，现有API接口仅扩展响应字段，不影响现有功能

**v2.0.1 更新**：
- ✅ 优化待审核线材列表接口，现在同时返回未评估(UNKNOWN)和待人工审核(PENDING_REVIEW)状态的线材
- ✅ 增强人工处理流程覆盖范围，确保所有需要人工干预的线材都能被识别

**v2.0.2 更新**：
- ✅ 新增获取已完成评估线材列表接口(`GET /api/quality/completed`)
- ✅ 支持对已自动确定结果的线材进行人工重新审核
- ✅ 优化人工审核流程，区分"首次审核"和"重新审核"
- ✅ 增强审核记录的详细程度，记录原始结果和变更信息

**v2.0.3 更新**：
- ✅ 为待审核和已完成评估接口添加分页功能，支持大数据量场景
- ✅ 已完成评估接口支持应用场景代码筛选
- ✅ 已完成评估接口支持模型置信度排序（升序/降序）
- ✅ 优化数据查询性能，避免一次性加载过多数据
- ✅ 提供灵活的排序选项，支持按创建时间和置信度排序 