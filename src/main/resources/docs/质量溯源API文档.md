# 金属微丝质量溯源系统 API 文档

## 概述

质量溯源系统提供多维度的质量问题分析和追踪功能，支持生产商、负责人、工艺类型、生产机器四个维度的质量统计和问题识别。

**重要更新**：系统已完成质量监控定时任务重构，实现了统计分析与邮件通知的完全解耦，支持时间窗口增量检测。

## 基础信息

- **基础URL**: `/api/traceability`
- **认证方式**: Bearer Token（在请求头中添加 `Authorization: Bearer <token>`）
- **数据格式**: JSON

## 通用响应格式

```json
{
  "code": "success",
  "msg": "操作成功",
  "data": {}
}
```

## 架构设计说明

### 1. 统计分析与通知解耦

系统将质量统计分析和邮件通知功能完全分离：

- **纯统计分析接口**：仅进行数据分析，不发送邮件，用于报表展示和数据查询
- **自动检测+通知接口**：结合统计分析和邮件发送，用于定时任务和问题通知
- **独立邮件发送接口**：完全独立的邮件功能，支持自定义内容

### 2. 时间窗口增量检测

- 摒弃全量历史数据扫描，采用时间窗口增量检测
- 默认检测前24小时数据，避免重复通知已解决问题
- 支持自定义时间范围，提供灵活的检测策略

### 3. 智能通知策略

- **有质量问题**：向相关责任人发送问题通知邮件
- **无质量问题**：仅向管理员发送系统运行确认邮件
- 支持完全自定义的邮件发送功能

**字段说明**：
- `code`: 响应状态码，成功时为"success"，失败时为"Error"
- `msg`: 响应消息，描述操作结果
- `data`: 响应数据，具体的业务数据内容

## API 接口列表

## 新增接口（重构后）

### 1. 全量历史数据质量统计分析

**接口描述**: 分析所有历史数据的质量问题，仅用于报表展示，不发送邮件通知

**请求方式**: `POST /api/traceability/analyze/all`

**请求参数**: 无

**响应示例**:
```json
{
  "code": "success",
  "msg": "操作成功",
  "data": [
    {
      "issueId": "MANUFACTURER_华为技术有限公司_1704067200000",
      "dimension": "生产商",
      "dimensionValue": "华为技术有限公司",
      "severity": "MEDIUM",
      "failRate": 8.0,
      "failCount": 8,
      "totalCount": 100,
      "description": "生产商华为技术有限公司的不合格率为8.00%，超过阈值5.00%",
      "recommendation": "建议检查生产工艺和质量控制流程",
      "contactEmail": "quality@huawei.com",
      "discoveredTime": "2025-01-01T12:00:00",
      "notified": false
    }
  ]
}
```

### 2. 基于时间窗口的质量统计分析

**接口描述**: 分析指定时间范围内的质量问题，仅用于数据分析，不发送邮件通知

**请求方式**: `POST /api/traceability/analyze/time-window`

**请求参数**:
```json
{
  "startTime": "2024-12-01T00:00:00",
  "endTime": "2024-12-31T23:59:59"
}
```

**参数说明**:
- `startTime`: 分析开始时间，必填
- `endTime`: 分析结束时间，必填

**响应示例**: 同上

### 3. 自动检测并通知质量问题（默认时间窗口）

**接口描述**: 使用默认时间窗口（24小时）检测质量问题并发送通知

**请求方式**: `POST /api/traceability/auto-detect`

**请求参数**: 无

**响应示例**:
```json
{
  "code": "success",
  "msg": "操作成功",
  "data": "增量检测完成，时间窗口：2024-12-30 00:00:00 至 2024-12-31 00:00:00，发现2个质量问题，邮件通知发送成功"
}
```

### 4. 自动检测并通知质量问题（指定时间窗口）

**接口描述**: 在指定时间范围内检测质量问题并发送通知

**请求方式**: `POST /api/traceability/auto-detect/time-window`

**请求参数**:
```json
{
  "startTime": "2024-12-01T00:00:00",
  "endTime": "2024-12-31T23:59:59"
}
```

**响应示例**: 同上

### 5. 发送自定义邮件通知

**接口描述**: 发送自定义内容的邮件通知，完全独立的邮件发送功能

**请求方式**: `POST /api/traceability/notifications/send-custom`

**请求参数**:
```json
{
  "recipients": ["user1@example.com", "user2@example.com"],
  "subject": "质量问题通知",
  "content": "发现质量问题，请及时处理...",
  "emailType": "QUALITY_ISSUE",
  "isHtml": false,
  "additionalData": "{\"issueCount\": 3}"
}
```

**参数说明**:
- `recipients`: 收件人邮箱列表，必填
- `subject`: 邮件主题，必填
- `content`: 邮件正文内容，必填
- `emailType`: 邮件类型，可选
- `isHtml`: 是否为HTML格式邮件，默认false
- `additionalData`: 附加数据（JSON格式），可选

**响应示例**:
```json
{
  "code": "success",
  "msg": "操作成功",
  "data": "自定义邮件发送完成，成功：2/2，收件人：user1@example.com, user2@example.com"
}
```

## 原有接口

### 6. 执行溯源分析

**接口描述**: 根据指定维度执行全面的质量溯源分析

**请求方式**: `POST /api/traceability/analysis`

**请求参数**:
```json
{
  "dimension": "MANUFACTURER",
  "dimensionValue": "华为技术有限公司",
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-12-31T23:59:59",
  "scenarioCode": "SC001",
  "onlyProblematic": false,
  "failRateThreshold": 5.0,
  "page": 1,
  "size": 20
}
```

**参数说明**:
- `dimension`: 查询维度，可选值：`MANUFACTURER`（生产商）、`RESPONSIBLE_PERSON`（负责人）、`PROCESS_TYPE`（工艺类型）、`PRODUCTION_MACHINE`（生产机器）
- `dimensionValue`: 维度值，可选，用于筛选特定对象
- `startTime`: 开始时间，格式：yyyy-MM-ddTHH:mm:ss，基于WireMaterial实体的eventTime字段进行过滤
- `endTime`: 结束时间，格式：yyyy-MM-ddTHH:mm:ss，基于WireMaterial实体的eventTime字段进行过滤
- `scenarioCode`: 应用场景编号，可选
- `onlyProblematic`: 是否只返回有问题的数据，默认false
- `failRateThreshold`: 不合格率阈值（百分比），默认5.0
- `page`: 页码，默认1
- `size`: 每页大小，默认20

**时间过滤说明**:
- 所有时间范围查询都基于WireMaterial实体的`eventTime`字段（事件发生时间）
- 时间过滤逻辑：`eventTime >= startTime AND eventTime <= endTime`
- 如果startTime为空，则不限制开始时间
- 如果endTime为空，则不限制结束时间
- 时间格式必须为ISO 8601格式：`yyyy-MM-ddTHH:mm:ss`

**响应示例**:
```json
{
  "code": "success",
  "msg": "溯源分析成功",
  "data": {
    "dimension": "生产商",
    "startTime": "2024-01-01T00:00:00",
    "endTime": "2024-12-31T23:59:59",
    "overallStatistics": {
      "totalDimensions": 10,
      "problematicDimensions": 3,
      "totalBatches": 1000,
      "totalPassBatches": 950,
      "totalFailBatches": 50,
      "overallPassRate": 95.0,
      "overallFailRate": 5.0
    },
    "detailStatistics": [
      {
        "dimensionName": "生产商",
        "dimensionValue": "华为技术有限公司",
        "totalCount": 100,
        "passCount": 92,
        "failCount": 8,
        "pendingReviewCount": 0,
        "unknownCount": 0,
        "passRate": 92.0,
        "failRate": 8.0,
        "contactEmail": "quality@huawei.com"
      }
    ],
    "qualityIssues": [
      {
        "issueId": "MANUFACTURER_华为技术有限公司_1704067200000",
        "dimension": "生产商",
        "dimensionValue": "华为技术有限公司",
        "severity": "MEDIUM",
        "failRate": 8.0,
        "failCount": 8,
        "totalCount": 100,
        "description": "生产商华为技术有限公司的不合格率为8.00%，超过阈值5.00%",
        "recommendation": "建议检查生产工艺和质量控制流程",
        "contactEmail": "quality@huawei.com",
        "discoveredTime": "2025-01-01T12:00:00",
        "notified": false
      }
    ]
  }
}
```

### 2. 获取质量统计数据

**接口描述**: 获取指定维度的质量统计数据

**请求方式**: `POST /api/traceability/statistics`

**请求参数**: 同溯源分析接口

**响应示例**:
```json
{
  "code": "success",
  "msg": "获取质量统计数据成功",
  "data": [
    {
      "dimensionName": "生产商",
      "dimensionValue": "华为技术有限公司",
      "totalCount": 100,
      "passCount": 92,
      "failCount": 8,
      "pendingReviewCount": 0,
      "unknownCount": 0,
      "passRate": 92.0,
      "failRate": 8.0,
      "contactEmail": "quality@huawei.com"
    }
  ]
}
```

### 3. 识别质量问题

**接口描述**: 根据统计数据识别质量问题

**请求方式**: `POST /api/traceability/issues`

**请求参数**: 同溯源分析接口

**响应示例**:
```json
{
  "code": "success",
  "msg": "识别质量问题成功",
  "data": [
    {
      "issueId": "MANUFACTURER_华为技术有限公司_1704067200000",
      "dimension": "生产商",
      "dimensionValue": "华为技术有限公司",
      "severity": "MEDIUM",
      "failRate": 8.0,
      "failCount": 8,
      "totalCount": 100,
      "description": "生产商华为技术有限公司的不合格率为8.00%，超过阈值5.00%",
      "recommendation": "建议检查生产工艺和质量控制流程",
      "contactEmail": "quality@huawei.com",
      "discoveredTime": "2025-01-01T12:00:00",
      "notified": false
    }
  ]
}
```

### 4. 获取问题批次详情

**接口描述**: 获取指定维度下的问题批次详细信息

**请求方式**: `GET /api/traceability/batches/problematic`

**请求参数**:
- `dimension`: 维度名称（必填）
- `dimensionValue`: 维度值（必填）
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）

**响应示例**:
```json
{
  "code": "success",
  "msg": "获取问题批次详情成功",
  "data": [
    {
      "batchId": "BATCH_001",
      "scenarioCode": "SC001",
      "manufacturer": "华为技术有限公司",
      "responsiblePerson": "张三",
      "processType": "拉丝工艺",
      "productionMachine": "拉丝机001",
      "diameter": 0.05,
      "resistance": 1.2,
      "extensibility": 15.5,
      "weight": 0.8,
      "evaluationResult": "FAIL",
      "modelEvaluationResult": "FAIL",
      "finalEvaluationResult": "FAIL",
      "evaluationMessage": "直径超出标准范围",
      "contactEmail": "quality@huawei.com",
      "createdTime": "2024-12-01T10:30:00"
    }
  ]
}
```

### 5. 发送质量问题通知

**接口描述**: 向相关负责人发送质量问题通知邮件，同时向管理员发送严重问题汇总通知

**请求方式**: `POST /api/traceability/notifications/send`

**邮件发送逻辑**:
1. **负责人通知**: 向每个质量问题相关的生产商/负责人联系邮箱发送具体问题通知
2. **管理员汇总**: 当存在严重(CRITICAL)或高风险(HIGH)问题时，向配置的管理员邮箱发送汇总通知
3. **管理员邮箱配置**: 通过`app.notification.admin-emails`配置项设置管理员邮箱列表

**请求参数**:
```json
[
  {
    "issueId": "MANUFACTURER_华为技术有限公司_1704067200000",
    "dimension": "生产商",
    "dimensionValue": "华为技术有限公司",
    "severity": "MEDIUM",
    "failRate": 8.0,
    "failCount": 8,
    "totalCount": 100,
    "description": "生产商华为技术有限公司的不合格率为8.00%，超过阈值5.00%",
    "recommendation": "建议检查生产工艺和质量控制流程",
    "contactEmail": "quality@huawei.com",
    "discoveredTime": "2025-01-01T12:00:00",
    "notified": false
  }
]
```

**响应示例**:
```json
{
  "code": "success",
  "msg": "发送质量问题通知成功",
  "data": "质量问题通知发送完成，成功：1/1"
}
```

### 6. 获取生产商质量排名

**接口描述**: 获取生产商按不合格率排序的质量排名

**请求方式**: `GET /api/traceability/ranking/manufacturers`

**请求参数**:
- `startTime`: 开始时间（可选）
- `endTime`: 结束时间（可选）
- `scenarioCode`: 应用场景编号（可选）

**响应示例**:
```json
{
  "code": "success",
  "msg": "获取生产商质量排名成功",
  "data": [
    {
      "dimensionName": "生产商",
      "dimensionValue": "华为技术有限公司",
      "totalCount": 100,
      "passCount": 92,
      "failCount": 8,
      "pendingReviewCount": 0,
      "unknownCount": 0,
      "passRate": 92.0,
      "failRate": 8.0,
      "contactEmail": "quality@huawei.com"
    }
  ]
}
```

### 7. 获取负责人绩效排名

**接口描述**: 获取负责人按不合格率排序的绩效排名

**请求方式**: `GET /api/traceability/ranking/responsible-persons`

**请求参数**: 同生产商质量排名

### 8. 获取工艺类型质量分析

**接口描述**: 获取工艺类型的质量分析数据

**请求方式**: `GET /api/traceability/analysis/process-types`

**请求参数**: 同生产商质量排名

### 9. 获取生产机器质量分析

**接口描述**: 获取生产机器的质量分析数据

**请求方式**: `GET /api/traceability/analysis/production-machines`

**请求参数**: 同生产商质量排名

### 10. 自动检测并通知质量问题

**接口描述**: 手动触发自动检测质量问题并发送通知

**请求方式**: `POST /api/traceability/auto-detect`

**请求参数**: 无

**响应示例**:
```json
{
  "code": "success",
  "msg": "自动检测质量问题成功",
  "data": "自动检测完成，发现3个质量问题，质量问题通知发送完成，成功：3/3"
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 质量问题严重程度

| 级别 | 不合格率范围 | 说明 |
|------|-------------|------|
| CRITICAL | ≥10% | 严重问题，需要立即处理 |
| HIGH | 8%-10% | 高风险问题，需要优先处理 |
| MEDIUM | 6%-8% | 中等风险问题，需要关注 |
| LOW | 5%-6% | 低风险问题，建议改进 |

## Vue3 前端集成示例

### 1. 安装依赖

```bash
npm install axios echarts vue-echarts
```

### 2. API 服务封装

```javascript
// api/traceability.js
import axios from 'axios'

const API_BASE_URL = '/api/traceability'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

// 请求拦截器 - 添加认证token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理响应
api.interceptors.response.use(
  response => {
    const { data } = response
    if (data.success) {
      return data.data
    } else {
      throw new Error(data.message || '请求失败')
    }
  },
  error => {
    console.error('API请求错误:', error)
    throw error
  }
)

export const traceabilityAPI = {
  // 执行溯源分析
  performAnalysis(params) {
    return api.post('/analysis', params)
  },

  // 获取质量统计数据
  getStatistics(params) {
    return api.post('/statistics', params)
  },

  // 识别质量问题
  identifyIssues(params) {
    return api.post('/issues', params)
  },

  // 获取问题批次详情
  getProblematicBatches(params) {
    return api.get('/batches/problematic', { params })
  },

  // 发送质量问题通知
  sendNotifications(issues) {
    return api.post('/notifications/send', issues)
  },

  // 获取生产商质量排名
  getManufacturerRanking(params) {
    return api.get('/ranking/manufacturers', { params })
  },

  // 获取负责人绩效排名
  getResponsiblePersonRanking(params) {
    return api.get('/ranking/responsible-persons', { params })
  },

  // 获取工艺类型质量分析
  getProcessTypeAnalysis(params) {
    return api.get('/analysis/process-types', { params })
  },

  // 获取生产机器质量分析
  getProductionMachineAnalysis(params) {
    return api.get('/analysis/production-machines', { params })
  },

  // 自动检测质量问题
  autoDetectIssues() {
    return api.post('/auto-detect')
  }
}
```

### 3. 质量溯源分析组件

```vue
<template>
  <div class="traceability-analysis">
    <el-card class="search-card">
      <h3>质量溯源分析</h3>

      <!-- 查询条件 -->
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item label="分析维度">
          <el-select v-model="queryForm.dimension" placeholder="请选择分析维度">
            <el-option label="生产商" value="MANUFACTURER" />
            <el-option label="负责人" value="RESPONSIBLE_PERSON" />
            <el-option label="工艺类型" value="PROCESS_TYPE" />
            <el-option label="生产机器" value="PRODUCTION_MACHINE" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="应用场景">
          <el-input v-model="queryForm.scenarioCode" placeholder="请输入应用场景编号" />
        </el-form-item>

        <el-form-item label="不合格率阈值">
          <el-input-number
            v-model="queryForm.failRateThreshold"
            :min="0"
            :max="100"
            :precision="1"
            controls-position="right"
          />
          <span style="margin-left: 8px;">%</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="performAnalysis" :loading="loading">
            开始分析
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分析结果 -->
    <div v-if="analysisResult" class="result-section">
      <!-- 总体统计 -->
      <el-card class="statistics-card">
        <h4>总体统计</h4>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ analysisResult.overallStatistics.totalDimensions }}</div>
              <div class="stat-label">总{{ getDimensionLabel() }}数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value problem">{{ analysisResult.overallStatistics.problematicDimensions }}</div>
              <div class="stat-label">问题{{ getDimensionLabel() }}数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value">{{ analysisResult.overallStatistics.totalBatches }}</div>
              <div class="stat-label">总批次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-value success">{{ analysisResult.overallStatistics.overallPassRate.toFixed(1) }}%</div>
              <div class="stat-label">总体合格率</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 质量问题列表 -->
      <el-card v-if="analysisResult.qualityIssues.length > 0" class="issues-card">
        <h4>质量问题 ({{ analysisResult.qualityIssues.length }})</h4>
        <el-table :data="analysisResult.qualityIssues" style="width: 100%">
          <el-table-column prop="dimensionValue" label="对象" />
          <el-table-column prop="severity" label="严重程度">
            <template #default="{ row }">
              <el-tag :type="getSeverityType(row.severity)">
                {{ getSeverityLabel(row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failRate" label="不合格率">
            <template #default="{ row }">
              {{ row.failRate.toFixed(1) }}%
            </template>
          </el-table-column>
          <el-table-column prop="failCount" label="不合格/总数">
            <template #default="{ row }">
              {{ row.failCount }}/{{ row.totalCount }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="问题描述" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="viewBatchDetails(row)">
                查看批次
              </el-button>
              <el-button
                size="small"
                type="warning"
                @click="sendNotification([row])"
                :disabled="row.notified"
              >
                {{ row.notified ? '已通知' : '发送通知' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 详细统计图表 -->
      <el-card class="chart-card">
        <h4>质量统计图表</h4>
        <div ref="chartContainer" style="height: 400px;"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { traceabilityAPI } from '@/api/traceability'

// 响应式数据
const loading = ref(false)
const analysisResult = ref(null)
const dateRange = ref([])
const chartContainer = ref(null)
let chart = null

// 查询表单
const queryForm = reactive({
  dimension: 'MANUFACTURER',
  scenarioCode: '',
  failRateThreshold: 5.0,
  onlyProblematic: false,
  page: 1,
  size: 20
})

// 执行溯源分析
const performAnalysis = async () => {
  try {
    loading.value = true

    const params = {
      ...queryForm,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    }

    const result = await traceabilityAPI.performAnalysis(params)
    analysisResult.value = result

    // 渲染图表
    await nextTick()
    renderChart()

    ElMessage.success('溯源分析完成')
  } catch (error) {
    ElMessage.error('溯源分析失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 渲染图表
const renderChart = () => {
  if (!chartContainer.value || !analysisResult.value) return

  if (chart) {
    chart.dispose()
  }

  chart = echarts.init(chartContainer.value)

  const statistics = analysisResult.value.detailStatistics
  const xData = statistics.map(item => item.dimensionValue)
  const passRateData = statistics.map(item => item.passRate)
  const failRateData = statistics.map(item => item.failRate)

  const option = {
    title: {
      text: `${getDimensionLabel()}质量统计`,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['合格率', '不合格率'],
      top: 30
    },
    xAxis: {
      type: 'category',
      data: xData,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '百分比 (%)',
      min: 0,
      max: 100
    },
    series: [
      {
        name: '合格率',
        type: 'bar',
        data: passRateData,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '不合格率',
        type: 'bar',
        data: failRateData,
        itemStyle: {
          color: '#F56C6C'
        }
      }
    ]
  }

  chart.setOption(option)
}

// 获取维度标签
const getDimensionLabel = () => {
  const labels = {
    MANUFACTURER: '生产商',
    RESPONSIBLE_PERSON: '负责人',
    PROCESS_TYPE: '工艺类型',
    PRODUCTION_MACHINE: '生产机器'
  }
  return labels[queryForm.dimension] || '对象'
}

// 获取严重程度类型
const getSeverityType = (severity) => {
  const types = {
    CRITICAL: 'danger',
    HIGH: 'warning',
    MEDIUM: 'info',
    LOW: 'success'
  }
  return types[severity] || 'info'
}

// 获取严重程度标签
const getSeverityLabel = (severity) => {
  const labels = {
    CRITICAL: '严重',
    HIGH: '高风险',
    MEDIUM: '中等',
    LOW: '低风险'
  }
  return labels[severity] || '未知'
}

// 查看批次详情
const viewBatchDetails = async (issue) => {
  try {
    const params = {
      dimension: queryForm.dimension,
      dimensionValue: issue.dimensionValue,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    }

    const batches = await traceabilityAPI.getProblematicBatches(params)

    // 这里可以打开一个对话框显示批次详情
    console.log('问题批次详情:', batches)
    ElMessage.info('批次详情已在控制台输出')
  } catch (error) {
    ElMessage.error('获取批次详情失败: ' + error.message)
  }
}

// 发送通知
const sendNotification = async (issues) => {
  try {
    await ElMessageBox.confirm(
      `确定要发送 ${issues.length} 个质量问题的通知邮件吗？`,
      '确认发送',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = await traceabilityAPI.sendNotifications(issues)

    // 更新通知状态
    issues.forEach(issue => {
      issue.notified = true
      issue.notifiedTime = new Date().toISOString()
    })

    ElMessage.success(result)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发送通知失败: ' + error.message)
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(queryForm, {
    dimension: 'MANUFACTURER',
    scenarioCode: '',
    failRateThreshold: 5.0,
    onlyProblematic: false,
    page: 1,
    size: 20
  })
  dateRange.value = []
  analysisResult.value = null

  if (chart) {
    chart.dispose()
    chart = null
  }
}

// 组件卸载时清理图表
onMounted(() => {
  // 组件挂载后的初始化逻辑
})
</script>

<style scoped>
.traceability-analysis {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-top: 20px;
}

.result-section {
  margin-top: 20px;
}

.statistics-card,
.issues-card,
.chart-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.problem {
  color: #F56C6C;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}
</style>
```

### 4. 数据可视化建议

#### 4.1 质量趋势图表
- 使用折线图展示时间维度的质量变化趋势
- 支持多维度对比（生产商、工艺类型等）
- 可配置时间粒度（日、周、月）

#### 4.2 质量分布图表
- 使用饼图展示合格/不合格比例
- 使用柱状图对比不同维度的质量表现
- 使用热力图展示质量问题分布

#### 4.3 实时监控大屏
- 实时显示质量统计数据
- 质量问题告警提示
- 关键指标仪表盘

#### 4.4 质量报告导出
- 支持PDF格式质量报告导出
- 包含图表和详细数据
- 可定制报告模板

### 5. 系统配置说明

#### 5.1 配置文件位置

质量溯源系统的配置项已集成到项目的配置文件中，请参考 `src/main/resources/application-example.yml` 文件中的完整配置示例。

#### 5.2 主要配置项说明

**通知配置 (`app.notification`)**:
- `email-enabled`: 是否启用邮件通知功能
- `fail-rate-threshold`: 基础不合格率阈值（百分比）
- `critical-fail-rate-threshold`: 严重问题不合格率阈值（百分比）
- `high-risk-fail-rate-threshold`: 高风险问题不合格率阈值（百分比）
- `medium-risk-fail-rate-threshold`: 中等风险问题不合格率阈值（百分比）
- `admin-emails`: 管理员邮箱列表，用于接收汇总报告和严重问题通知
- `email-retry-count`: 邮件发送重试次数
- `email-retry-interval`: 邮件发送重试间隔（毫秒）

**质量监控配置 (`app.quality-monitor`)**:
- `enabled`: 是否启用质量监控定时任务
- `cron`: 定时任务执行频率（cron表达式）

**质量报告配置 (`app.quality-report`)**:
- `enabled`: 是否启用质量报告生成
- `cron`: 报告生成频率（cron表达式）

#### 5.3 邮件通知机制详解

**负责人通知**:
- 触发条件：质量问题不合格率超过配置阈值
- 发送对象：WireMaterial实体中的contactEmail字段指定的邮箱
- 通知内容：具体质量问题详情、建议措施

**管理员汇总通知**:
- 触发条件：存在严重(CRITICAL)或高风险(HIGH)质量问题
- 发送对象：配置文件中admin-emails指定的管理员邮箱
- 通知内容：问题汇总表格、统计数据、处理建议

**定时报告**:
- 触发条件：按配置的cron表达式定时执行
- 发送对象：管理员邮箱
- 报告内容：各维度质量统计数据、趋势分析

#### 5.4 配置使用说明

1. **配置文件复制**: 将 `application-example.yml` 复制为 `application.yml` 或 `application-prod.yml`
2. **邮箱配置**: 根据实际SMTP服务器修改邮件配置部分
3. **管理员邮箱**: 在 `admin-emails` 中配置实际的管理员邮箱地址
4. **阈值调整**: 根据生产实际情况调整各级质量问题判定阈值
5. **定时任务**: 根据监控需求调整cron表达式的执行频率

#### 5.5 使用建议

1. **权限控制**: 根据用户角色控制API访问权限
2. **数据缓存**: 对统计数据进行适当缓存，提高查询性能
3. **异步处理**: 大数据量分析建议使用异步处理
4. **错误处理**: 完善的错误处理和用户提示
5. **性能优化**: 合理使用分页和数据过滤
6. **邮件配置**: 确保SMTP服务器配置正确，管理员邮箱有效
7. **阈值调整**: 根据实际生产情况调整质量问题判定阈值

## 6. JWT鉴权机制问题分析与解决

### 6.1 问题概述

在系统开发过程中发现，使用相同JWT令牌时，质量溯源相关接口鉴权失败，而其他功能模块接口成功通过鉴权。经过详细分析，发现问题根源在于Token验证方式的不一致性。

### 6.2 问题根因分析

#### 6.2.1 Token验证方式差异

**问题接口（质量溯源 - 修复前）**:
```java
// 错误的实现方式
@PostMapping("/analysis")
public BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(
        @RequestHeader("Authorization") String token,  // 直接获取Authorization头
        @RequestBody TraceabilityQueryRequest request) {

    // 验证包含"Bearer "前缀的token
    if (!tokenService.validateToken(token)) {
        return BaseResponse.error("无效的访问令牌");
    }
}
```

**正确接口（其他功能模块）**:
```java
// 正确的实现方式
@GetMapping("/profile")
public ResponseEntity<BaseResponse<Map<String, Object>>> getUserProfile(HttpServletRequest request) {
    // 从拦截器设置的属性中获取已处理的用户信息
    Long userId = (Long) request.getAttribute("userId");
    String userName = (String) request.getAttribute("userName");
}
```

#### 6.2.2 AuthInterceptor的Token处理逻辑

```java
private String getTokenFromRequest(HttpServletRequest request) {
    // 从Authorization头获取
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        return authHeader.substring(7); // 关键：去掉"Bearer "前缀
    }

    // 其他获取方式...
    return request.getHeader("token");
}
```

#### 6.2.3 问题核心

1. **AuthInterceptor正确处理**: 拦截器已经正确解析Authorization头，去掉"Bearer "前缀，验证token，并将用户信息设置到request属性中
2. **质量溯源接口错误处理**: 直接从Authorization头获取token（包含"Bearer "前缀），然后用包含前缀的字符串进行验证
3. **JWT验证失败**: `tokenService.validateToken()`期望接收纯token，但收到了"Bearer eyJhbGciOiJIUzI1NiJ9..."格式的字符串

### 6.3 拦截器配置分析

#### WebConfig配置
```java
registry.addInterceptor(authInterceptor)
        .addPathPatterns("/api/**")  // 包含质量溯源接口
        .excludePathPatterns(
            "/api/auth/register/**",
            "/api/auth/login/**",
            "/api/auth/reset-password/**",
            "/api/auth/root/**",
            "/api/auth/token"
        )
        .order(0);
```

**分析结果**:
- 质量溯源接口路径`/api/traceability/**`在拦截器覆盖范围内
- 没有被排除在外，应该正常经过AuthInterceptor处理
- 拦截器已经验证了token并设置了用户信息到request属性

### 6.4 解决方案

#### 6.4.1 已实施的修复

将所有质量溯源接口的token验证方式修改为标准方式：

```java
// 修复前
@PostMapping("/analysis")
public BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(
        @RequestHeader("Authorization") String token,
        @RequestBody TraceabilityQueryRequest request) {

    if (!tokenService.validateToken(token)) {
        return BaseResponse.error("无效的访问令牌");
    }
}

// 修复后
@PostMapping("/analysis")
public BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(
        @RequestBody TraceabilityQueryRequest request,
        HttpServletRequest httpRequest) {

    // 从拦截器设置的属性中获取用户信息（拦截器已验证token）
    String userName = (String) httpRequest.getAttribute("userName");
    Long userId = (Long) httpRequest.getAttribute("userId");
}
```

#### 6.4.2 修复的接口列表

1. `POST /api/traceability/analysis` - 执行溯源分析
2. `POST /api/traceability/statistics` - 获取质量统计数据
3. `POST /api/traceability/issues` - 识别质量问题
4. `GET /api/traceability/batches/problematic` - 获取问题批次详情
5. `POST /api/traceability/notifications/send` - 发送质量问题通知
6. `GET /api/traceability/ranking/manufacturers` - 获取生产商质量排名
7. `GET /api/traceability/ranking/responsible-persons` - 获取负责人绩效排名
8. `GET /api/traceability/analysis/process-types` - 获取工艺类型质量分析
9. `GET /api/traceability/analysis/production-machines` - 获取生产机器质量分析
10. `POST /api/traceability/auto-detect` - 自动检测并通知质量问题

### 6.5 最佳实践建议

#### 6.5.1 统一的鉴权方式
- 所有需要鉴权的接口都应该依赖AuthInterceptor的处理结果
- 避免在Controller中重复验证token
- 直接使用request.getAttribute()获取用户信息

#### 6.5.2 代码规范
```java
// 推荐的Controller方法签名
@PostMapping("/example")
public ResponseEntity<BaseResponse<Object>> exampleMethod(
        @RequestBody RequestDto request,
        HttpServletRequest httpRequest) {

    // 获取用户信息
    Long userId = (Long) httpRequest.getAttribute("userId");
    String userName = (String) httpRequest.getAttribute("userName");
    Integer roleId = (Integer) httpRequest.getAttribute("roleId");
    TokenService.UserType userType = (TokenService.UserType) httpRequest.getAttribute("userType");
}
```

#### 6.5.3 权限控制
- 如需特定权限验证，在获取用户信息后进行角色检查
- 参考WireMaterialController中的hasManagerPermission()方法

### 6.6 关键差异对比

| 方面 | 质量溯源接口（修复前） | 其他功能模块接口 |
|------|----------------------|------------------|
| Token获取 | `@RequestHeader("Authorization")` | `request.getAttribute("token")` |
| Token格式 | "Bearer eyJxxx..." | "eyJxxx..." |
| 验证方式 | 重复验证 | 依赖拦截器验证 |
| 用户信息 | 从token解析 | 从request属性获取 |

### 6.7 验证方法

修复后，使用相同的JWT token测试：
1. 其他功能模块接口（如用户信息获取）
2. 质量溯源接口

两者应该都能正常通过鉴权并返回正确结果。

### 6.8 总结

这个问题的核心在于**token格式处理的不一致性**。修复后所有接口将使用统一的鉴权机制，确保系统的安全性和一致性。通过这次问题分析和修复，建立了标准的JWT鉴权处理模式，为后续开发提供了参考规范。

## 7. 溯源分析ClassCastException修复文档

### 7.1 问题描述

在执行溯源分析时，TraceabilityServiceImpl.java的第273行出现了ClassCastException错误：

```
class [Ljava.lang.Object; cannot be cast to class java.lang.Number
```

错误发生在`calculateOverallStatistics`方法中，具体位置是尝试将查询结果转换为Number类型时。

### 7.2 问题分析

#### 7.2.1 根本原因

1. **返回类型不匹配**：`getOverallStatistics`方法原本返回`Object[]`，但JPA原生查询实际返回的是`List<Object[]>`
2. **类型转换异常**：当查询结果为空或数据类型不符合预期时，强制类型转换会失败
3. **缺少空值处理**：没有对查询结果为空的情况进行处理

#### 7.2.2 错误场景

- 查询结果为空时
- 数据库返回的数据类型与期望不符时
- 查询结果包含null值时

### 7.3 修复方案

#### 7.3.1 修改Repository方法返回类型

**文件**：`src/main/java/com/mmw/metal_micro_wire_backend/repository/WireMaterialRepository.java`

**修改前**：
```java
Object[] getOverallStatistics(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("scenarioCode") String scenarioCode);
```

**修改后**：
```java
List<Object[]> getOverallStatistics(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime,
                                   @Param("scenarioCode") String scenarioCode);
```

#### 7.3.2 增强Service方法的错误处理

**文件**：`src/main/java/com/mmw/metal_micro_wire_backend/service/impl/TraceabilityServiceImpl.java`

**主要改进**：

1. **空结果处理**：
```java
if (overallDataList == null || overallDataList.isEmpty()) {
    log.warn("总体统计查询返回空结果，使用默认值");
    return TraceabilityAnalysisResponse.OverallStatistics.builder()
            .totalDimensions(0L)
            .problematicDimensions(0L)
            .totalBatches(0L)
            .totalPassBatches(0L)
            .totalFailBatches(0L)
            .overallPassRate(0.0)
            .overallFailRate(0.0)
            .build();
}
```

2. **安全类型转换**：
```java
// 安全的类型转换，处理可能的null值
Long totalBatches = convertToLong(overallData[0]);
Long totalPassBatches = convertToLong(overallData[1]);
Long totalFailBatches = convertToLong(overallData[2]);
```

3. **添加类型转换辅助方法**：
```java
/**
 * 安全地将Object转换为Long类型
 * 处理可能的null值和类型转换异常
 */
private Long convertToLong(Object value) {
    if (value == null) {
        return 0L;
    }

    try {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        } else {
            log.warn("无法转换的数据类型: {}, 值: {}, 使用默认值0", value.getClass().getName(), value);
            return 0L;
        }
    } catch (Exception e) {
        log.error("类型转换失败，值: {}, 错误: {}, 使用默认值0", value, e.getMessage());
        return 0L;
    }
}
```

### 7.4 测试验证

#### 7.4.1 单元测试

创建了`TraceabilityServiceTest`类，包含以下测试场景：

1. **正常数据测试**：验证正常查询结果的处理
2. **空结果测试**：验证查询结果为空时的处理
3. **null值测试**：验证包含null值的查询结果处理
4. **字符串数字测试**：验证字符串类型数字的转换

#### 7.4.2 集成测试

创建了`TraceabilityIntegrationTest`类，测试各个维度的溯源分析：

- 厂商维度（MANUFACTURER）
- 负责人维度（RESPONSIBLE_PERSON）
- 工艺类型维度（PROCESS_TYPE）
- 生产机器维度（PRODUCTION_MACHINE）

### 7.5 修复效果

#### 7.5.1 修复前

- 执行厂商维度溯源分析时抛出ClassCastException
- 系统无法正常处理空查询结果
- 缺少对异常数据类型的处理

#### 7.5.2 修复后

- 所有维度的溯源分析都能正常执行
- 优雅处理空查询结果，返回默认值
- 安全处理各种数据类型转换
- 增加详细的错误日志记录

### 7.6 最佳实践建议

1. **类型安全**：在处理JPA原生查询结果时，始终使用`List<Object[]>`而不是`Object[]`
2. **空值检查**：对所有查询结果进行空值检查
3. **安全转换**：使用辅助方法进行类型转换，避免直接强制转换
4. **错误日志**：记录详细的错误信息，便于问题排查
5. **默认值**：为异常情况提供合理的默认值

### 7.7 相关文件

- `src/main/java/com/mmw/metal_micro_wire_backend/repository/WireMaterialRepository.java`
- `src/main/java/com/mmw/metal_micro_wire_backend/service/impl/TraceabilityServiceImpl.java`
- `src/test/java/com/mmw/metal_micro_wire_backend/service/TraceabilityServiceTest.java`
- `src/test/java/com/mmw/metal_micro_wire_backend/integration/TraceabilityIntegrationTest.java`

### 7.8 版本信息

- 修复日期：2025-07-06
- 修复版本：0.0.1-SNAPSHOT
- 影响范围：溯源分析功能
- 兼容性：向后兼容，不影响现有API接口

## 8. openGauss参数类型推断问题修复说明

### 8.1 问题描述

在使用openGauss数据库（MySQL兼容模式）时，遇到了参数类型推断错误：

```
ERROR: could not determine data type of parameter $2
SQLState: 42P38
```

该错误发生在执行包含`IS NULL`检查的原生SQL查询时，特别是在`WireMaterialRepository.getFailedBatchesByDimension`方法中。

### 8.2 原始问题查询

```sql
SELECT * FROM wire_materials w
WHERE w.final_evaluation_result = 'FAIL'
AND CASE
    WHEN ?1 = 'MANUFACTURER' THEN w.manufacturer = ?2
    WHEN ?1 = 'RESPONSIBLE_PERSON' THEN w.responsible_person = ?2
    WHEN ?1 = 'PROCESS_TYPE' THEN w.process_type = ?2
    WHEN ?1 = 'PRODUCTION_MACHINE' THEN w.production_machine = ?2
    ELSE FALSE
END = TRUE
AND (?3 IS NULL OR w.event_time >= ?3)
AND (?4 IS NULL OR w.event_time <= ?4)
ORDER BY w.event_time DESC
```

### 8.3 根本原因

openGauss在处理以下情况时存在参数类型推断困难：
1. 复杂的CASE语句中的参数重复使用
2. `IS NULL`检查与参数绑定的组合
3. 即使使用显式类型转换（CAST）也无法完全解决

### 8.4 解决方案

采用**查询分离策略**，将复杂的动态查询拆分为多个简单的静态查询：

#### 8.4.1 按维度分离查询方法

为每个维度创建专门的查询方法：
- `getFailedBatchesByManufacturerWithTime` / `getFailedBatchesByManufacturerNoTime`
- `getFailedBatchesByResponsiblePersonWithTime` / `getFailedBatchesByResponsiblePersonNoTime`
- `getFailedBatchesByProcessTypeWithTime` / `getFailedBatchesByProcessTypeNoTime`
- `getFailedBatchesByProductionMachineWithTime` / `getFailedBatchesByProductionMachineNoTime`

#### 8.4.2 按时间过滤分离查询

每个维度提供两个版本：
- **有时间过滤版本**：包含明确的时间范围条件
- **无时间过滤版本**：不包含时间条件

#### 8.4.3 示例查询结构

```sql
-- 有时间过滤
SELECT * FROM wire_materials w
WHERE w.final_evaluation_result = 'FAIL'
AND w.manufacturer = ?1
AND w.event_time >= ?2
AND w.event_time <= ?3
ORDER BY w.event_time DESC

-- 无时间过滤
SELECT * FROM wire_materials w
WHERE w.final_evaluation_result = 'FAIL'
AND w.manufacturer = ?1
ORDER BY w.event_time DESC
```

#### 8.4.4 统一接口实现

通过default方法提供统一的接口：

```java
default List<WireMaterial> getFailedBatchesByDimension(String dimension, String dimensionValue,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
    boolean hasTimeFilter = startTime != null && endTime != null;

    switch (dimension) {
        case "MANUFACTURER":
            return hasTimeFilter ?
                getFailedBatchesByManufacturerWithTime(dimensionValue, startTime, endTime) :
                getFailedBatchesByManufacturerNoTime(dimensionValue);
        // ... 其他维度
    }
}
```

### 8.5 修复效果

✅ **解决了openGauss参数类型推断问题**
✅ **保持了原有API接口不变**
✅ **所有单元测试通过**
✅ **实际运行环境验证成功**

### 8.6 技术要点

1. **避免复杂的动态SQL**：openGauss对复杂查询的参数类型推断支持有限
2. **分离关注点**：将不同的查询逻辑分离到不同的方法中
3. **简化参数绑定**：每个查询方法只处理必要的参数
4. **保持接口兼容性**：通过default方法保持对外接口不变

### 8.7 适用场景

此解决方案适用于：
- openGauss数据库环境
- 包含复杂条件判断的动态查询
- 需要参数重复使用的场景
- 包含`IS NULL`检查的查询

### 8.8 注意事项

- 该方案增加了代码量，但提高了查询的稳定性
- 每个新维度需要添加对应的查询方法
- 建议在openGauss环境中优先使用简单的静态查询

### 8.9 相关文件

- `src/main/java/com/mmw/metal_micro_wire_backend/repository/WireMaterialRepository.java`
- `src/main/java/com/mmw/metal_micro_wire_backend/service/impl/TraceabilityServiceImpl.java`

### 8.10 版本信息

- 修复日期：2025-07-06
- 修复版本：0.0.1-SNAPSHOT
- 影响范围：问题批次查询功能
- 兼容性：向后兼容，不影响现有API接口

## 9. 技术修复总结

### 9.1 修复概览

本文档记录了质量溯源系统在开发过程中遇到的三个主要技术问题及其解决方案：

1. **JWT鉴权机制问题**：解决了token格式处理不一致导致的鉴权失败
2. **ClassCastException问题**：解决了JPA查询结果类型转换异常
3. **openGauss参数类型推断问题**：解决了复杂SQL查询在openGauss数据库中的兼容性问题

### 9.2 技术架构改进

通过这些修复，系统在以下方面得到了显著改进：

- **安全性**：统一的JWT鉴权机制确保了系统安全
- **稳定性**：安全的类型转换和错误处理提高了系统稳定性
- **兼容性**：解决了openGauss数据库的兼容性问题
- **可维护性**：清晰的代码结构和详细的错误日志便于维护

### 9.3 开发规范建议

基于这些修复经验，建议在后续开发中遵循以下规范：

1. **统一鉴权方式**：所有API接口使用统一的AuthInterceptor处理鉴权
2. **安全类型转换**：使用辅助方法进行类型转换，避免直接强制转换
3. **数据库兼容性**：在openGauss环境中优先使用简单的静态查询
4. **错误处理**：完善的异常处理和日志记录
5. **测试覆盖**：为关键功能编写完整的单元测试和集成测试

### 9.4 质量保证

所有修复都经过了严格的测试验证：
- 单元测试覆盖核心逻辑
- 集成测试验证端到端功能
- 实际运行环境验证
- API接口兼容性验证

这些修复确保了质量溯源系统的稳定性和可靠性，为生产环境的部署奠定了坚实基础。

## 10. 质量监控配置说明

### 10.1 配置文件

在 `application.yml` 中添加以下配置：

```yaml
app:
  quality-monitor:
    # 是否启用质量监控定时任务
    enabled: true
    # 定时任务执行频率（cron表达式）
    # 格式：秒 分 时 日 月 周
    # 示例：
    # "0 0 0 * * ?"     - 每天凌晨执行一次（推荐）
    # "0 0 * * * ?"     - 每小时执行一次
    # "0 */30 * * * ?"  - 每30分钟执行一次
    # "0 0 */2 * * ?"   - 每2小时执行一次
    cron: "0 0 0 * * ?"
    # 检测时间窗口（小时）- 检测前N小时的数据
    detection-window-hours: 24
    # 是否向管理员发送无问题确认邮件
    send-no-issue-notification-to-admin: true
    # 质量问题判定阈值（百分比）
    fail-rate-threshold: 5.0
```

### 10.2 配置参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `enabled` | boolean | true | 是否启用质量监控定时任务 |
| `cron` | string | "0 0 0 * * ?" | 定时任务执行频率（cron表达式） |
| `detection-window-hours` | int | 24 | 检测时间窗口（小时） |
| `send-no-issue-notification-to-admin` | boolean | true | 是否向管理员发送无问题确认邮件 |
| `fail-rate-threshold` | double | 5.0 | 质量问题判定阈值（百分比） |

### 10.3 时间窗口说明

- **增量检测**：系统采用时间窗口增量检测，避免重复通知已解决的问题
- **默认窗口**：检测前24小时的数据，可通过配置调整
- **灵活配置**：支持通过API接口指定自定义时间范围

### 10.4 通知策略

#### 10.4.1 有质量问题时
- 向相关责任人发送问题通知邮件
- 邮件包含问题详情、严重程度、建议措施等信息
- 支持多个收件人同时通知

#### 10.4.2 无质量问题时
- 仅向管理员发送系统运行确认邮件
- 确认系统正常工作，避免误报
- 可通过配置关闭此功能

### 10.5 Vue3前端集成示例

#### 10.5.1 全量统计分析

```javascript
// 获取全量历史数据质量统计
async function analyzeAllQualityIssues() {
  try {
    const response = await axios.post('/api/traceability/analyze/all', {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 'success') {
      // 处理统计结果
      const issues = response.data.data;
      console.log('发现质量问题：', issues.length);

      // 可以用于图表展示
      renderQualityChart(issues);
    }
  } catch (error) {
    console.error('统计分析失败：', error);
  }
}
```

#### 10.5.2 时间窗口分析

```javascript
// 基于时间窗口的质量统计分析
async function analyzeQualityByTimeWindow(startTime, endTime) {
  try {
    const response = await axios.post('/api/traceability/analyze/time-window', {
      startTime: startTime,
      endTime: endTime
    }, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    return response.data.data;
  } catch (error) {
    console.error('时间窗口分析失败：', error);
    throw error;
  }
}
```

#### 10.5.3 自动检测触发

```javascript
// 手动触发质量问题检测
async function triggerQualityDetection() {
  try {
    const response = await axios.post('/api/traceability/auto-detect', {}, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.data.code === 'success') {
      // 显示检测结果
      showNotification('检测完成', response.data.data, 'success');
    }
  } catch (error) {
    showNotification('检测失败', error.message, 'error');
  }
}
```

#### 10.5.4 自定义邮件发送

```javascript
// 发送自定义邮件通知
async function sendCustomNotification(recipients, subject, content) {
  try {
    const response = await axios.post('/api/traceability/notifications/send-custom', {
      recipients: recipients,
      subject: subject,
      content: content,
      isHtml: false
    }, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    return response.data;
  } catch (error) {
    console.error('邮件发送失败：', error);
    throw error;
  }
}
```

### 10.6 数据可视化建议

#### 10.6.1 质量趋势图表
- 使用ECharts或Chart.js展示质量问题趋势
- 按时间维度显示不合格率变化
- 支持多维度对比分析

#### 10.6.2 问题分布图
- 饼图显示各维度问题分布
- 柱状图对比不同责任人/机器的质量表现
- 热力图展示问题集中区域

#### 10.6.3 实时监控面板
- 实时显示当前质量状态
- 最近检测结果和通知状态
- 系统运行状态指示器
