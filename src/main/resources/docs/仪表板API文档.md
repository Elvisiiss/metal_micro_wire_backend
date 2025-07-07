# 仪表板API文档

## 概述

仪表板模块提供金属微丝质量检测系统的统计数据展示功能，包括年度检测数据统计、应用场景统计和系统总体统计等核心指标。

## API接口

### 1. 年度检测数据统计

**GET** `/api/OverView/year`

**权限**: 已认证用户

**功能描述**: 获取最近12个月的质量检测统计数据，按月份分组显示合格数量和不合格数量。

**请求示例**:
```http
GET /api/OverView/year
Authorization: Bearer <token>
```

**成功响应示例**:
```json
{
  "msg": "获取年度统计数据成功",
  "code": "success",
  "data": {
    "monthlyData": [
      {
        "year": 2024,
        "month": 1,
        "passCount": 850,
        "failCount": 150,
        "totalCount": 1000,
        "passRate": 85.00
      },
      {
        "year": 2024,
        "month": 2,
        "passCount": 920,
        "failCount": 80,
        "totalCount": 1000,
        "passRate": 92.00
      }
    ]
  }
}
```

**响应字段说明**:
- `monthlyData`: 月度统计数据数组
  - `year`: 年份
  - `month`: 月份
  - `passCount`: 合格数量
  - `failCount`: 不合格数量
  - `totalCount`: 总数量
  - `passRate`: 合格率（百分比）

### 2. 应用场景统计

**GET** `/api/OverView/scenario`

**权限**: 已认证用户

**功能描述**: 根据指定时间范围统计各应用场景的使用次数。

**请求参数**:
- `how` (必填): 时间范围，枚举值：
  - `this_month`: 当前月份
  - `last_month`: 上一个月
  - `this_year`: 当前年份
  - `last_year`: 上一年
  - `all`: 全部时间

**请求示例**:
```http
GET /api/OverView/scenario?how=this_month
Authorization: Bearer <token>
```

**成功响应示例**:
```json
{
  "msg": "获取应用场景统计数据成功",
  "code": "success",
  "data": {
    "scenarioData": [
      {
        "scenarioCode": "01",
        "scenarioName": "电子元器件",
        "wireType": "Cu",
        "scenarioCount": 1250
      },
      {
        "scenarioCode": "02",
        "scenarioName": "汽车线束",
        "wireType": "Al",
        "scenarioCount": 980
      }
    ]
  }
}
```

**响应字段说明**:
- `scenarioData`: 场景统计数据数组
  - `scenarioCode`: 应用场景编号
  - `scenarioName`: 应用场景名称
  - `wireType`: 线材类型
  - `scenarioCount`: 场景使用次数

### 3. 系统总体统计

**GET** `/api/OverView/count`

**权限**: 已认证用户

**功能描述**: 获取系统总体统计数据，包括总检测数量、本月数量、上月数量、总场景数、总设备数等。

**请求示例**:
```http
GET /api/OverView/count
Authorization: Bearer <token>
```

**成功响应示例**:
```json
{
  "msg": "获取系统总体统计数据成功",
  "code": "success",
  "data": {
    "totalDetectionCount": 15000,
    "currentMonthCount": 1200,
    "lastMonthCount": 1150,
    "totalScenarioCount": 25,
    "totalDeviceCount": 8,
    "totalPassCount": 13500,
    "totalFailCount": 1500,
    "totalPassRate": 90.00,
    "currentMonthPassCount": 1080,
    "currentMonthFailCount": 120,
    "currentMonthPassRate": 90.00
  }
}
```

**响应字段说明**:
- `totalDetectionCount`: 总检测数量
- `currentMonthCount`: 本月检测数量
- `lastMonthCount`: 上月检测数量
- `totalScenarioCount`: 总应用场景数
- `totalDeviceCount`: 总设备数
- `totalPassCount`: 总合格数量
- `totalFailCount`: 总不合格数量
- `totalPassRate`: 总合格率（百分比）
- `currentMonthPassCount`: 本月合格数量
- `currentMonthFailCount`: 本月不合格数量
- `currentMonthPassRate`: 本月合格率（百分比）

## 数据源说明

### 数据表
- **wire_materials**: 线材检测数据主表
- **application_scenarios**: 应用场景配置表

### 关键字段
- `final_evaluation_result`: 最终评估结果（PASS/FAIL/PENDING_REVIEW/UNKNOWN）
- `scenario_code`: 应用场景编号
- `event_time`: 事件发生时间
- `device_id`: 设备ID

### 统计逻辑
1. **年度统计**: 基于`event_time`字段按年月分组，统计`final_evaluation_result`为PASS和FAIL的数量
2. **场景统计**: 基于`scenario_code`字段分组统计，结合`application_scenarios`表获取场景名称和线材类型
3. **总体统计**: 全表统计和按月份过滤统计的组合

## 错误处理

**失败响应示例**:
```json
{
  "msg": "不支持的时间范围参数：invalid_param",
  "code": "Error"
}
```

**常见错误**:
- 时间范围参数不正确（必须是：this_month、last_month、this_year、last_year、all）
- 数据库查询异常
- 权限验证失败

## 前端集成示例

### Vue3 + Axios 示例

```javascript
// 获取年度统计数据
async function getYearlyStatistics() {
  try {
    const response = await axios.get('/api/OverView/year', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.data.code === 'success') {
      // 处理月度数据，可用于图表展示
      const chartData = response.data.data.monthlyData.map(item => ({
        month: `${item.year}-${item.month.toString().padStart(2, '0')}`,
        pass: item.passCount,
        fail: item.failCount,
        rate: item.passRate
      }));
      
      // 使用 ECharts 或其他图表库展示
      renderChart(chartData);
    }
  } catch (error) {
    console.error('获取年度统计失败:', error);
  }
}

// 获取场景统计数据
async function getScenarioStatistics(timeRange = 'this_month') {
  try {
    const response = await axios.get('/api/OverView/scenario', {
      params: { how: timeRange },
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.data.code === 'success') {
      // 处理场景数据，可用于饼图或柱状图
      const pieData = response.data.data.scenarioData.map(item => ({
        name: item.scenarioName,
        value: item.scenarioCount,
        code: item.scenarioCode,
        type: item.wireType
      }));
      
      renderPieChart(pieData);
    }
  } catch (error) {
    console.error('获取场景统计失败:', error);
  }
}

// 获取总体统计数据
async function getOverallStatistics() {
  try {
    const response = await axios.get('/api/OverView/count', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    if (response.data.code === 'success') {
      const stats = response.data.data;
      
      // 更新仪表板卡片
      updateDashboardCards({
        totalDetections: stats.totalDetectionCount,
        currentMonth: stats.currentMonthCount,
        lastMonth: stats.lastMonthCount,
        scenarios: stats.totalScenarioCount,
        devices: stats.totalDeviceCount,
        passRate: stats.totalPassRate
      });
    }
  } catch (error) {
    console.error('获取总体统计失败:', error);
  }
}
```

### 数据可视化建议

1. **年度统计**: 使用折线图或柱状图展示月度趋势
2. **场景统计**: 使用饼图或环形图展示场景分布
3. **总体统计**: 使用卡片或仪表盘展示关键指标

## 性能优化

1. **数据库索引**: 确保`event_time`、`scenario_code`、`final_evaluation_result`字段有适当索引
2. **缓存策略**: 可考虑对统计数据进行短期缓存
3. **分页处理**: 大数据量时考虑分页返回

## 注意事项

1. 所有接口都需要有效的JWT Token认证
2. 时间范围参数严格按照枚举值传递（this_month、last_month、this_year、last_year、all）
3. 前端需要处理数据为空的情况
4. 建议定期刷新统计数据以保持实时性
