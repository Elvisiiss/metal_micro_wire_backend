package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.OverallStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.ScenarioStatisticsResponse;
import com.mmw.metal_micro_wire_backend.dto.overview.YearlyStatisticsResponse;
import com.mmw.metal_micro_wire_backend.entity.ApplicationScenario;
import com.mmw.metal_micro_wire_backend.repository.ApplicationScenarioRepository;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.OverViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表板概览服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverViewServiceImpl implements OverViewService {
    
    private final WireMaterialRepository wireMaterialRepository;
    private final ApplicationScenarioRepository applicationScenarioRepository;
    
    @Override
    public BaseResponse<YearlyStatisticsResponse> getYearlyStatistics() {
        try {
            // 计算12个月前的日期
            LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            log.info("获取年度统计数据，起始时间：{}", twelveMonthsAgo);
            
            // 查询月度统计数据
            List<Object[]> monthlyData = wireMaterialRepository.getMonthlyStatistics(twelveMonthsAgo);
            
            List<YearlyStatisticsResponse.MonthlyStatistics> monthlyStatistics = new ArrayList<>();
            
            for (Object[] row : monthlyData) {
                Integer year = ((Number) row[0]).intValue();
                Integer month = ((Number) row[1]).intValue();
                Long passCount = ((Number) row[2]).longValue();
                Long failCount = ((Number) row[3]).longValue();
                Long totalCount = ((Number) row[4]).longValue();
                
                // 计算合格率
                Double passRate = totalCount > 0 ? 
                    BigDecimal.valueOf(passCount * 100.0 / totalCount)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue() : 0.0;
                
                YearlyStatisticsResponse.MonthlyStatistics monthlyStats = YearlyStatisticsResponse.MonthlyStatistics.builder()
                        .year(year)
                        .month(month)
                        .passCount(passCount)
                        .failCount(failCount)
                        .totalCount(totalCount)
                        .passRate(passRate)
                        .build();
                
                monthlyStatistics.add(monthlyStats);
            }
            
            YearlyStatisticsResponse response = YearlyStatisticsResponse.builder()
                    .monthlyData(monthlyStatistics)
                    .build();
            
            log.info("年度统计数据获取成功，共{}个月的数据", monthlyStatistics.size());
            return BaseResponse.success("获取年度统计数据成功", response);
            
        } catch (Exception e) {
            log.error("获取年度统计数据失败", e);
            return BaseResponse.error("获取年度统计数据失败：" + e.getMessage());
        }
    }
    
    @Override
    public BaseResponse<ScenarioStatisticsResponse> getScenarioStatistics(String how) {
        try {
            log.info("获取应用场景统计数据，时间范围：{}", how);
            
            // 根据how参数计算时间范围
            LocalDateTime startTime = null;
            LocalDateTime endTime = null;
            
            LocalDateTime now = LocalDateTime.now();
            
            switch (how) {
                case "this_month":
                    YearMonth currentMonth = YearMonth.from(now);
                    startTime = currentMonth.atDay(1).atStartOfDay();
                    endTime = currentMonth.atEndOfMonth().atTime(23, 59, 59);
                    break;
                case "last_month":
                    YearMonth lastMonth = YearMonth.from(now).minusMonths(1);
                    startTime = lastMonth.atDay(1).atStartOfDay();
                    endTime = lastMonth.atEndOfMonth().atTime(23, 59, 59);
                    break;
                case "this_year":
                    startTime = LocalDateTime.of(now.getYear(), 1, 1, 0, 0, 0);
                    endTime = LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59);
                    break;
                case "last_year":
                    int lastYear = now.getYear() - 1;
                    startTime = LocalDateTime.of(lastYear, 1, 1, 0, 0, 0);
                    endTime = LocalDateTime.of(lastYear, 12, 31, 23, 59, 59);
                    break;
                case "all":
                    // 不设置时间限制
                    break;
                default:
                    return BaseResponse.error("不支持的时间范围参数：" + how);
            }
            
            // 查询场景统计数据
            List<Object[]> scenarioData = wireMaterialRepository.getScenarioStatistics(startTime, endTime);
            
            // 获取所有应用场景信息用于补充场景名称和线材类型
            List<ApplicationScenario> allScenarios = applicationScenarioRepository.findAll();
            Map<String, ApplicationScenario> scenarioMap = allScenarios.stream()
                    .collect(Collectors.toMap(ApplicationScenario::getScenarioCode, scenario -> scenario));
            
            List<ScenarioStatisticsResponse.ScenarioStatistics> scenarioStatistics = new ArrayList<>();
            
            for (Object[] row : scenarioData) {
                String scenarioCode = (String) row[0];
                Long scenarioCount = ((Number) row[1]).longValue();
                
                ApplicationScenario scenario = scenarioMap.get(scenarioCode);
                String scenarioName = scenario != null ? scenario.getScenarioName() : "未知场景";
                String wireType = scenario != null ? scenario.getWireType() : "未知";
                
                ScenarioStatisticsResponse.ScenarioStatistics scenarioStats = ScenarioStatisticsResponse.ScenarioStatistics.builder()
                        .scenarioCode(scenarioCode)
                        .scenarioName(scenarioName)
                        .wireType(wireType)
                        .scenarioCount(scenarioCount)
                        .build();
                
                scenarioStatistics.add(scenarioStats);
            }
            
            ScenarioStatisticsResponse response = ScenarioStatisticsResponse.builder()
                    .scenarioData(scenarioStatistics)
                    .build();
            
            log.info("应用场景统计数据获取成功，共{}个场景", scenarioStatistics.size());
            return BaseResponse.success("获取应用场景统计数据成功", response);

        } catch (Exception e) {
            log.error("获取应用场景统计数据失败", e);
            return BaseResponse.error("获取应用场景统计数据失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<OverallStatisticsResponse> getOverallStatistics() {
        try {
            log.info("获取系统总体统计数据");

            LocalDateTime now = LocalDateTime.now();

            // 获取系统总体统计
            List<Object[]> overallData = wireMaterialRepository.getOverallSystemStatistics();
            Long totalDetectionCount = 0L;
            Long totalPassCount = 0L;
            Long totalFailCount = 0L;

            if (!overallData.isEmpty()) {
                Object[] row = overallData.get(0);
                totalDetectionCount = ((Number) row[0]).longValue();
                totalPassCount = ((Number) row[1]).longValue();
                totalFailCount = ((Number) row[2]).longValue();
            }

            // 计算总合格率
            Double totalPassRate = totalDetectionCount > 0 ?
                BigDecimal.valueOf(totalPassCount * 100.0 / totalDetectionCount)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue() : 0.0;

            // 获取本月统计
            List<Object[]> currentMonthData = wireMaterialRepository.getCurrentMonthStatistics(now.getYear(), now.getMonthValue());
            Long currentMonthCount = 0L;
            Long currentMonthPassCount = 0L;
            Long currentMonthFailCount = 0L;

            if (!currentMonthData.isEmpty()) {
                Object[] row = currentMonthData.get(0);
                currentMonthCount = ((Number) row[0]).longValue();
                currentMonthPassCount = ((Number) row[1]).longValue();
                currentMonthFailCount = ((Number) row[2]).longValue();
            }

            // 计算本月合格率
            Double currentMonthPassRate = currentMonthCount > 0 ?
                BigDecimal.valueOf(currentMonthPassCount * 100.0 / currentMonthCount)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue() : 0.0;

            // 获取上月统计
            LocalDateTime lastMonth = now.minusMonths(1);
            List<Object[]> lastMonthData = wireMaterialRepository.getLastMonthStatistics(lastMonth.getYear(), lastMonth.getMonthValue());
            Long lastMonthCount = 0L;

            if (!lastMonthData.isEmpty()) {
                Object[] row = lastMonthData.get(0);
                lastMonthCount = ((Number) row[0]).longValue();
            }

            // 获取总应用场景数
            Long totalScenarioCount = applicationScenarioRepository.count();

            // 获取总设备数
            Long totalDeviceCount = wireMaterialRepository.getDistinctDeviceCount();
            if (totalDeviceCount == null) {
                totalDeviceCount = 0L;
            }

            OverallStatisticsResponse response = OverallStatisticsResponse.builder()
                    .totalDetectionCount(totalDetectionCount)
                    .currentMonthCount(currentMonthCount)
                    .lastMonthCount(lastMonthCount)
                    .totalScenarioCount(totalScenarioCount)
                    .totalDeviceCount(totalDeviceCount)
                    .totalPassCount(totalPassCount)
                    .totalFailCount(totalFailCount)
                    .totalPassRate(totalPassRate)
                    .currentMonthPassCount(currentMonthPassCount)
                    .currentMonthFailCount(currentMonthFailCount)
                    .currentMonthPassRate(currentMonthPassRate)
                    .build();

            log.info("系统总体统计数据获取成功");
            return BaseResponse.success("获取系统总体统计数据成功", response);

        } catch (Exception e) {
            log.error("获取系统总体统计数据失败", e);
            return BaseResponse.error("获取系统总体统计数据失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<Integer> getTodayCount() {
        try {
            log.info("获取今日线材检测数据统计");

            LocalDateTime now = LocalDateTime.now();
            int todayCount = wireMaterialRepository.getTodayCount(now);

            log.info("今日线材检测数据统计获取成功，共{}条数据", todayCount);
            return BaseResponse.success("获取今日线材检测数据统计成功", todayCount);

        } catch (Exception e) {
            log.error("获取今日线材检测数据统计失败", e);
            return BaseResponse.error("获取今日线材检测数据统计失败：" + e.getMessage());
        }
        
    }
}
