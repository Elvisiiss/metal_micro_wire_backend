package com.mmw.metal_micro_wire_backend.service.impl;

import com.mmw.metal_micro_wire_backend.config.NotificationConfig;
import com.mmw.metal_micro_wire_backend.config.QualityMonitorConfig;
import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.*;
import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import com.mmw.metal_micro_wire_backend.repository.WireMaterialRepository;
import com.mmw.metal_micro_wire_backend.service.EmailService;
import com.mmw.metal_micro_wire_backend.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 质量问题溯源服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceabilityServiceImpl implements TraceabilityService {

    private final WireMaterialRepository wireMaterialRepository;
    private final EmailService emailService;
    private final NotificationConfig notificationConfig;
    private final QualityMonitorConfig qualityMonitorConfig;

    // 支持多种日期时间格式
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter STANDARD_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BigDecimal DEFAULT_FAIL_RATE_THRESHOLD = BigDecimal.valueOf(5.0); // 默认5%不合格率阈值

    @Override
    public BaseResponse<TraceabilityAnalysisResponse> performTraceabilityAnalysis(TraceabilityQueryRequest request) {
        try {
            log.info("开始执行溯源分析，维度：{}，值：{}", request.getDimension(), request.getDimensionValue());

            // 解析时间参数
            LocalDateTime startTime = request.getStartTime();
            LocalDateTime endTime = request.getEndTime();

            // 获取统计数据
            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 识别质量问题
            List<QualityIssueResponse> qualityIssues = identifyIssuesFromStatistics(statistics, request);

            // 获取总体统计
            TraceabilityAnalysisResponse.OverallStatistics overallStats = calculateOverallStatistics(
                    request.getDimension(), startTime, endTime, request.getScenarioCode());

            // 构建响应
            TraceabilityAnalysisResponse response = TraceabilityAnalysisResponse.builder()
                    .dimension(request.getDimension().getDescription())
                    .startTime(startTime)
                    .endTime(endTime)
                    .overallStatistics(overallStats)
                    .detailStatistics(statistics)
                    .qualityIssues(qualityIssues)
                    .build();

            log.info("溯源分析完成，维度：{}，统计项数：{}，问题数：{}",
                    request.getDimension(), statistics.size(), qualityIssues.size());

            return BaseResponse.success(response);

        } catch (Exception e) {
            log.error("执行溯源分析失败", e);
            return BaseResponse.error("溯源分析失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<QualityStatisticsResponse>> getQualityStatistics(TraceabilityQueryRequest request) {
        try {
            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 如果只查询有问题的数据，进行过滤
            if (Boolean.TRUE.equals(request.getOnlyProblematic())) {
                BigDecimal threshold = BigDecimal.valueOf(request.getFailRateThreshold());
                statistics = statistics.stream()
                        .filter(stat -> stat.hasQualityIssue(threshold))
                        .collect(Collectors.toList());
            }

            return BaseResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取质量统计数据失败", e);
            return BaseResponse.error("获取质量统计数据失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<QualityIssueResponse>> identifyQualityIssues(TraceabilityQueryRequest request) {
        try {
            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);
            List<QualityIssueResponse> qualityIssues = identifyIssuesFromStatistics(statistics, request);

            return BaseResponse.success(qualityIssues);

        } catch (Exception e) {
            log.error("识别质量问题失败", e);
            return BaseResponse.error("识别质量问题失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<BatchDetailResponse>> getProblematicBatches(String dimension, String dimensionValue,
                                                                        String startTime, String endTime) {
        try {
            log.info("开始获取问题批次详情，维度：{}，值：{}，开始时间：{}，结束时间：{}",
                    dimension, dimensionValue, startTime, endTime);

            // 参数验证
            if (!StringUtils.hasText(dimension) || !StringUtils.hasText(dimensionValue)) {
                log.warn("维度或维度值参数为空：dimension={}, dimensionValue={}", dimension, dimensionValue);
                return BaseResponse.error("维度和维度值不能为空");
            }

            // 解析日期时间参数
            LocalDateTime start = parseDateTime(startTime);
            LocalDateTime end = parseDateTime(endTime);

            log.debug("解析后的时间参数：start={}, end={}", start, end);

            // 验证时间范围的合理性
            if (start != null && end != null && start.isAfter(end)) {
                log.warn("开始时间晚于结束时间：start={}, end={}", start, end);
                return BaseResponse.error("开始时间不能晚于结束时间");
            }

            // 执行查询
            List<WireMaterial> failedBatches = wireMaterialRepository.getFailedBatchesByDimension(
                    dimension, dimensionValue, start, end);

            log.info("查询到 {} 个问题批次", failedBatches.size());

            // 转换为响应对象
            List<BatchDetailResponse> batchDetails = failedBatches.stream()
                    .map(BatchDetailResponse::fromEntity)
                    .collect(Collectors.toList());

            return BaseResponse.success(batchDetails);

        } catch (Exception e) {
            log.error("获取问题批次详情失败，维度：{}，值：{}，开始时间：{}，结束时间：{}",
                    dimension, dimensionValue, startTime, endTime, e);
            return BaseResponse.error("获取问题批次详情失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<String> sendQualityIssueNotifications(List<QualityIssueResponse> qualityIssues) {
        try {
            int successCount = 0;
            int totalCount = qualityIssues.size();

            // 发送给相关负责人
            for (QualityIssueResponse issue : qualityIssues) {
                if (StringUtils.hasText(issue.getContactEmail())) {
                    try {
                        sendIssueNotificationEmail(issue);
                        issue.setNotified(true);
                        issue.setNotifiedTime(LocalDateTime.now());
                        successCount++;
                    } catch (Exception e) {
                        log.error("发送质量问题通知邮件失败，问题ID：{}，邮箱：{}",
                                issue.getIssueId(), issue.getContactEmail(), e);
                    }
                }
            }

            // 发送汇总通知给管理员（仅当有严重或高风险问题时）
            List<QualityIssueResponse> criticalIssues = qualityIssues.stream()
                    .filter(issue -> issue.getSeverity() == QualityIssueResponse.IssueSeverity.CRITICAL ||
                                   issue.getSeverity() == QualityIssueResponse.IssueSeverity.HIGH)
                    .collect(Collectors.toList());

            if (!criticalIssues.isEmpty() && notificationConfig.getAdminEmails().length > 0) {
                sendAdminSummaryNotification(criticalIssues);
            }

            String result = String.format("质量问题通知发送完成，成功：%d/%d", successCount, totalCount);
            log.info(result);

            return BaseResponse.success(result);

        } catch (Exception e) {
            log.error("发送质量问题通知失败", e);
            return BaseResponse.error("发送质量问题通知失败：" + e.getMessage());
        }
    }

    /**
     * 根据维度获取统计数据
     */
    private List<QualityStatisticsResponse> getStatisticsByDimension(TraceabilityQueryRequest request) {
        // 验证必要参数
        if (request == null) {
            log.error("TraceabilityQueryRequest不能为null");
            throw new IllegalArgumentException("查询请求不能为空");
        }

        if (request.getDimension() == null) {
            log.error("查询维度不能为null，请求：{}", request);
            throw new IllegalArgumentException("查询维度不能为空");
        }

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();
        String scenarioCode = request.getScenarioCode();
        String dimensionValue = request.getDimensionValue();

//        // 设置默认时间范围（如果未提供）
//        if (startTime == null) {
//            startTime = LocalDateTime.now().minusDays(30);
//            log.info("未提供开始时间，使用默认值：{}", startTime);
//        }
//        if (endTime == null) {
//            endTime = LocalDateTime.now();
//            log.info("未提供结束时间，使用默认值：{}", endTime);
//        }

        List<Object[]> rawData;
        String dimensionName;

        log.info("根据维度获取统计数据，维度：{}，维度值：{}，时间范围：{} 到 {}",
                request.getDimension(), dimensionValue, startTime, endTime);

        switch (request.getDimension()) {
            case MANUFACTURER:
                rawData = wireMaterialRepository.getManufacturerStatistics(startTime, endTime, scenarioCode, dimensionValue);
                dimensionName = "生产商";
                break;
            case RESPONSIBLE_PERSON:
                rawData = wireMaterialRepository.getResponsiblePersonStatistics(startTime, endTime, scenarioCode, dimensionValue);
                dimensionName = "负责人";
                break;
            case PROCESS_TYPE:
                rawData = wireMaterialRepository.getProcessTypeStatistics(startTime, endTime, scenarioCode, dimensionValue);
                dimensionName = "工艺类型";
                break;
            case PRODUCTION_MACHINE:
                rawData = wireMaterialRepository.getProductionMachineStatistics(startTime, endTime, scenarioCode, dimensionValue);
                dimensionName = "生产机器";
                break;
            default:
                throw new IllegalArgumentException("不支持的查询维度：" + request.getDimension());
        }

        return convertToQualityStatistics(rawData, dimensionName);
    }

    /**
     * 将原始查询结果转换为质量统计响应对象
     */
    private List<QualityStatisticsResponse> convertToQualityStatistics(List<Object[]> rawData, String dimensionName) {
        return rawData.stream()
                .map(row -> {
                    QualityStatisticsResponse stat = QualityStatisticsResponse.builder()
                            .dimensionName(dimensionName)
                            .dimensionValue((String) row[0])
                            .totalCount(((Number) row[1]).longValue())
                            .passCount(((Number) row[2]).longValue())
                            .failCount(((Number) row[3]).longValue())
                            .pendingReviewCount(((Number) row[4]).longValue())
                            .unknownCount(((Number) row[5]).longValue())
                            .contactEmail((String) row[6])
                            .build();

                    stat.calculateRates();
                    return stat;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从统计数据中识别质量问题
     */
    private List<QualityIssueResponse> identifyIssuesFromStatistics(List<QualityStatisticsResponse> statistics,
                                                                   TraceabilityQueryRequest request) {
        BigDecimal threshold = BigDecimal.valueOf(request.getFailRateThreshold());

        return statistics.stream()
                .filter(stat -> stat.hasQualityIssue(threshold))
                .map(stat -> {
                    QualityIssueResponse issue = QualityIssueResponse.builder()
                            .issueId(generateIssueId(request.getDimension().name(), stat.getDimensionValue()))
                            .dimension(stat.getDimensionName())
                            .dimensionValue(stat.getDimensionValue())
                            .severity(QualityIssueResponse.IssueSeverity.fromFailRate(stat.getFailRate()))
                            .failRate(stat.getFailRate())
                            .failCount(stat.getFailCount())
                            .totalCount(stat.getTotalCount())
                            .contactEmail(stat.getContactEmail())
                            .discoveredTime(LocalDateTime.now())
                            .build();

                    issue.generateDescription();
                    issue.generateRecommendation();

                    return issue;
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算总体统计信息
     */
    private TraceabilityAnalysisResponse.OverallStatistics calculateOverallStatistics(
            TraceabilityQueryRequest.QueryDimension dimension, LocalDateTime startTime,
            LocalDateTime endTime, String scenarioCode) {

        List<Object[]> overallDataList = wireMaterialRepository.getOverallStatistics(startTime, endTime, scenarioCode);

        // 处理查询结果为空的情况
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

        Object[] overallData = overallDataList.get(0);

        // 安全的类型转换，处理可能的null值
        Long totalBatches = convertToLong(overallData[0]);
        Long totalPassBatches = convertToLong(overallData[1]);
        Long totalFailBatches = convertToLong(overallData[2]);

        Double overallPassRate = totalBatches > 0 ?
                (totalPassBatches.doubleValue() / totalBatches.doubleValue()) * 100 : 0.0;
        Double overallFailRate = totalBatches > 0 ?
                (totalFailBatches.doubleValue() / totalBatches.doubleValue()) * 100 : 0.0;

        // 获取维度统计（用于计算有问题的维度数量）
        List<QualityStatisticsResponse> dimensionStats = getStatisticsByDimension(
                TraceabilityQueryRequest.builder()
                        .dimension(dimension)
                        .startTime(startTime)
                        .endTime(endTime)
                        .scenarioCode(scenarioCode)
                        .failRateThreshold(5.0)
                        .build());

        Long totalDimensions = (long) dimensionStats.size();
        Long problematicDimensions = dimensionStats.stream()
                .mapToLong(stat -> stat.hasQualityIssue(DEFAULT_FAIL_RATE_THRESHOLD) ? 1 : 0)
                .sum();

        return TraceabilityAnalysisResponse.OverallStatistics.builder()
                .totalDimensions(totalDimensions)
                .problematicDimensions(problematicDimensions)
                .totalBatches(totalBatches)
                .totalPassBatches(totalPassBatches)
                .totalFailBatches(totalFailBatches)
                .overallPassRate(overallPassRate)
                .overallFailRate(overallFailRate)
                .build();
    }

    /**
     * 生成问题ID
     */
    private String generateIssueId(String dimension, String dimensionValue) {
        return String.format("%s_%s_%d", dimension, dimensionValue, System.currentTimeMillis());
    }

    /**
     * 发送质量问题通知邮件
     */
    private void sendIssueNotificationEmail(QualityIssueResponse issue) {
        String subject = String.format("【质量问题通知】%s质量异常 - %s", issue.getDimension(), issue.getSeverity().getLevel());

        String content = String.format(
                "尊敬的负责人，\n\n" +
                "检测到质量问题：\n" +
                "问题维度：%s\n" +
                "问题对象：%s\n" +
                "严重程度：%s\n" +
                "不合格率：%.2f%%\n" +
                "不合格批次：%d/%d\n\n" +
                "问题描述：\n%s\n\n" +
                "建议措施：\n%s\n\n" +
                "发现时间：%s\n\n" +
                "请及时处理相关质量问题。\n\n" +
                "此邮件由金属微丝质量溯源系统自动发送。",
                issue.getDimension(),
                issue.getDimensionValue(),
                issue.getSeverity().getLevel(),
                issue.getFailRate(),
                issue.getFailCount(),
                issue.getTotalCount(),
                issue.getDescription(),
                issue.getRecommendation(),
                issue.getDiscoveredTime().format(STANDARD_DATE_TIME_FORMATTER)
        );

        emailService.sendSimpleEmail(issue.getContactEmail(), subject, content);
    }

    /**
     * 发送管理员汇总通知
     */
    private void sendAdminSummaryNotification(List<QualityIssueResponse> criticalIssues) {
        String subject = String.format("【质量问题汇总通知】检测到%d个严重/高风险质量问题", criticalIssues.size());

        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body>");
        htmlContent.append("<h2>质量问题汇总通知</h2>");
        htmlContent.append("<p><strong>检测时间：</strong>").append(LocalDateTime.now().format(STANDARD_DATE_TIME_FORMATTER)).append("</p>");
        htmlContent.append("<p><strong>问题总数：</strong>").append(criticalIssues.size()).append("</p>");

        htmlContent.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
        htmlContent.append("<tr style='background-color: #f2f2f2;'>");
        htmlContent.append("<th>维度</th><th>对象</th><th>严重程度</th><th>不合格率</th><th>不合格数/总数</th><th>联系邮箱</th>");
        htmlContent.append("</tr>");

        for (QualityIssueResponse issue : criticalIssues) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(issue.getDimension()).append("</td>");
            htmlContent.append("<td>").append(issue.getDimensionValue()).append("</td>");
            htmlContent.append("<td style='color: ").append(getSeverityColor(issue.getSeverity())).append(";'>")
                      .append(issue.getSeverity().getLevel()).append("</td>");
            htmlContent.append("<td>").append(String.format("%.2f%%", issue.getFailRate())).append("</td>");
            htmlContent.append("<td>").append(issue.getFailCount()).append("/").append(issue.getTotalCount()).append("</td>");
            htmlContent.append("<td>").append(issue.getContactEmail() != null ? issue.getContactEmail() : "无").append("</td>");
            htmlContent.append("</tr>");
        }

        htmlContent.append("</table>");
        htmlContent.append("<p><strong>建议措施：</strong></p>");
        htmlContent.append("<ul>");
        htmlContent.append("<li>立即联系相关负责人处理严重质量问题</li>");
        htmlContent.append("<li>检查生产工艺和质量控制流程</li>");
        htmlContent.append("<li>加强质量监控和预防措施</li>");
        htmlContent.append("</ul>");
        htmlContent.append("<p><em>此邮件由金属微丝质量溯源系统自动发送</em></p>");
        htmlContent.append("</body></html>");

        // 发送给所有管理员
        for (String adminEmail : notificationConfig.getAdminEmails()) {
            try {
                emailService.sendHtmlEmail(adminEmail, subject, htmlContent.toString());
                log.info("质量问题汇总通知已发送给管理员：{}", adminEmail);
            } catch (Exception e) {
                log.error("发送质量问题汇总通知给管理员{}失败", adminEmail, e);
            }
        }
    }

    /**
     * 获取严重程度对应的颜色
     */
    private String getSeverityColor(QualityIssueResponse.IssueSeverity severity) {
        switch (severity) {
            case CRITICAL:
                return "#FF0000"; // 红色
            case HIGH:
                return "#FF8C00"; // 橙色
            case MEDIUM:
                return "#FFD700"; // 金色
            case LOW:
                return "#32CD32"; // 绿色
            default:
                return "#000000"; // 黑色
        }
    }

    /**
     * 解析日期时间字符串，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (!StringUtils.hasText(dateTimeStr)) {
            log.debug("日期时间字符串为空，返回null");
            return null;
        }

        // 尝试多种日期时间格式
        DateTimeFormatter[] formatters = {
            ISO_DATE_TIME_FORMATTER,           // yyyy-MM-dd'T'HH:mm:ss
            STANDARD_DATE_TIME_FORMATTER,      // yyyy-MM-dd HH:mm:ss
            DateTimeFormatter.ISO_LOCAL_DATE_TIME  // 标准ISO格式
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime result = LocalDateTime.parse(dateTimeStr, formatter);
                log.debug("成功解析日期时间：{} -> {}", dateTimeStr, result);
                return result;
            } catch (DateTimeParseException e) {
                log.debug("使用格式 {} 解析日期时间 {} 失败：{}", formatter.toString(), dateTimeStr, e.getMessage());
            }
        }

        log.warn("所有格式都无法解析日期时间：{}，返回null", dateTimeStr);
        return null;
    }

    @Override
    public BaseResponse<List<QualityStatisticsResponse>> getManufacturerRanking(String startTime, String endTime, String scenarioCode) {
        try {
            TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                    .dimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER)
                    .startTime(parseDateTime(startTime))
                    .endTime(parseDateTime(endTime))
                    .scenarioCode(scenarioCode)
                    .build();

            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 按不合格率降序排序
            statistics.sort((a, b) -> {
                if (a.getFailRate() == null && b.getFailRate() == null) return 0;
                if (a.getFailRate() == null) return 1;
                if (b.getFailRate() == null) return -1;
                return b.getFailRate().compareTo(a.getFailRate());
            });

            return BaseResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取生产商质量排名失败", e);
            return BaseResponse.error("获取生产商质量排名失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<QualityStatisticsResponse>> getResponsiblePersonRanking(String startTime, String endTime, String scenarioCode) {
        try {
            TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                    .dimension(TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON)
                    .startTime(parseDateTime(startTime))
                    .endTime(parseDateTime(endTime))
                    .scenarioCode(scenarioCode)
                    .build();

            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 按不合格率降序排序
            statistics.sort((a, b) -> {
                if (a.getFailRate() == null && b.getFailRate() == null) return 0;
                if (a.getFailRate() == null) return 1;
                if (b.getFailRate() == null) return -1;
                return b.getFailRate().compareTo(a.getFailRate());
            });

            return BaseResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取负责人绩效排名失败", e);
            return BaseResponse.error("获取负责人绩效排名失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<QualityStatisticsResponse>> getProcessTypeAnalysis(String startTime, String endTime, String scenarioCode) {
        try {
            TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                    .dimension(TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE)
                    .startTime(parseDateTime(startTime))
                    .endTime(parseDateTime(endTime))
                    .scenarioCode(scenarioCode)
                    .build();

            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 按不合格率降序排序
            statistics.sort((a, b) -> {
                if (a.getFailRate() == null && b.getFailRate() == null) return 0;
                if (a.getFailRate() == null) return 1;
                if (b.getFailRate() == null) return -1;
                return b.getFailRate().compareTo(a.getFailRate());
            });

            return BaseResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取工艺类型质量分析失败", e);
            return BaseResponse.error("获取工艺类型质量分析失败：" + e.getMessage());
        }
    }

    @Override
    public BaseResponse<List<QualityStatisticsResponse>> getProductionMachineAnalysis(String startTime, String endTime, String scenarioCode) {
        try {
            TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                    .dimension(TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE)
                    .startTime(parseDateTime(startTime))
                    .endTime(parseDateTime(endTime))
                    .scenarioCode(scenarioCode)
                    .build();

            List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);

            // 按不合格率降序排序
            statistics.sort((a, b) -> {
                if (a.getFailRate() == null && b.getFailRate() == null) return 0;
                if (a.getFailRate() == null) return 1;
                if (b.getFailRate() == null) return -1;
                return b.getFailRate().compareTo(a.getFailRate());
            });

            return BaseResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取生产机器质量分析失败", e);
            return BaseResponse.error("获取生产机器质量分析失败：" + e.getMessage());
        }
    }

    // ==================== 纯统计分析方法（不涉及邮件） ====================

    @Override
    public BaseResponse<List<QualityIssueResponse>> analyzeAllQualityIssues() {
        log.info("开始全量历史数据质量统计分析");

        List<QualityIssueResponse> allIssues = new ArrayList<>();

        // 全量统计各维度（无时间限制）
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER, null, null));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON, null, null));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE, null, null));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE, null, null));

        log.info("全量历史数据质量统计分析完成，发现{}个质量问题", allIssues.size());
        return BaseResponse.success(allIssues);
    }

    @Override
    public BaseResponse<List<QualityIssueResponse>> analyzeQualityIssuesByTimeWindow(
            LocalDateTime startTime, LocalDateTime endTime) {
        log.info("开始时间窗口质量统计分析，时间范围：{} 至 {}", startTime, endTime);

        List<QualityIssueResponse> allIssues = new ArrayList<>();

        // 基于时间窗口统计各维度
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER, startTime, endTime));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON, startTime, endTime));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE, startTime, endTime));
        allIssues.addAll(analyzeQualityIssuesByDimension(TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE, startTime, endTime));

        log.info("时间窗口质量统计分析完成，发现{}个质量问题", allIssues.size());
        return BaseResponse.success(allIssues);
    }

    /**
     * 核心统计分析方法（纯数据处理，不涉及通知）
     */
    private List<QualityIssueResponse> analyzeQualityIssuesByDimension(
            TraceabilityQueryRequest.QueryDimension dimension, LocalDateTime startTime, LocalDateTime endTime) {

        TraceabilityQueryRequest request = TraceabilityQueryRequest.builder()
                .dimension(dimension)
                .startTime(startTime)
                .endTime(endTime)
                .onlyProblematic(true)
                .failRateThreshold(qualityMonitorConfig.getFailRateThreshold())
                .build();

        List<QualityStatisticsResponse> statistics = getStatisticsByDimension(request);
        return identifyIssuesFromStatistics(statistics, request);
    }

    // ==================== 自动检测+通知方法（基于时间窗口） ====================

    @Override
    public BaseResponse<String> autoDetectAndNotifyQualityIssues() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(qualityMonitorConfig.getDetectionWindowHours());

        return autoDetectAndNotifyQualityIssues(startTime, endTime);
    }

    @Override
    public BaseResponse<String> autoDetectAndNotifyQualityIssues(
            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("开始基于时间窗口的质量问题自动检测，时间范围：{} 至 {}", startTime, endTime);

            // 1. 调用纯统计分析方法
            BaseResponse<List<QualityIssueResponse>> analysisResult =
                analyzeQualityIssuesByTimeWindow(startTime, endTime);

            if (!"success".equals(analysisResult.getCode())) {
                return BaseResponse.error("质量问题分析失败：" + analysisResult.getMsg());
            }

            List<QualityIssueResponse> qualityIssues = analysisResult.getData();

            // 2. 根据分析结果决定通知策略
            if (qualityIssues.isEmpty()) {
                // 无质量问题：仅向管理员发送确认邮件
                return sendNoIssuesNotificationToAdmin(startTime, endTime);
            } else {
                // 有质量问题：向相关人员发送问题通知
                BaseResponse<String> notificationResult = sendQualityIssueNotifications(qualityIssues);

                String result = String.format("增量检测完成，时间窗口：%s 至 %s，发现%d个质量问题，%s",
                        startTime.format(STANDARD_DATE_TIME_FORMATTER),
                        endTime.format(STANDARD_DATE_TIME_FORMATTER),
                        qualityIssues.size(),
                        notificationResult.getData());
                log.info(result);

                return BaseResponse.success(result);
            }

        } catch (Exception e) {
            log.error("基于时间窗口的质量问题自动检测失败", e);
            return BaseResponse.error("质量问题自动检测失败：" + e.getMessage());
        }
    }

    // ==================== 独立的邮件发送方法 ====================

    @Override
    public BaseResponse<String> sendCustomNotification(CustomNotificationRequest request) {
        try {
            log.info("开始发送自定义邮件通知，收件人：{}", request.getRecipients());

            int successCount = 0;
            int totalCount = request.getRecipients().size();

            for (String recipient : request.getRecipients()) {
                try {
                    if (Boolean.TRUE.equals(request.getIsHtml())) {
                        emailService.sendHtmlEmail(recipient, request.getSubject(), request.getContent());
                    } else {
                        emailService.sendSimpleEmail(recipient, request.getSubject(), request.getContent());
                    }
                    successCount++;
                    log.info("自定义邮件发送成功，收件人：{}", recipient);
                } catch (Exception e) {
                    log.error("自定义邮件发送失败，收件人：{}，错误：{}", recipient, e.getMessage());
                }
            }

            String result = String.format("自定义邮件发送完成，成功：%d/%d，收件人：%s",
                successCount, totalCount, String.join(", ", request.getRecipients()));
            log.info(result);

            if (successCount == totalCount) {
                return BaseResponse.success(result);
            } else if (successCount > 0) {
                return BaseResponse.success(result + "（部分成功）");
            } else {
                return BaseResponse.error("所有邮件发送失败");
            }

        } catch (Exception e) {
            log.error("自定义邮件发送异常", e);
            return BaseResponse.error("自定义邮件发送异常：" + e.getMessage());
        }
    }

    /**
     * 无质量问题时向管理员发送确认邮件
     */
    private BaseResponse<String> sendNoIssuesNotificationToAdmin(
            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (!qualityMonitorConfig.isSendNoIssueNotificationToAdmin()) {
                String result = String.format("时间窗口 %s 至 %s 内未检测到质量问题",
                    startTime.format(STANDARD_DATE_TIME_FORMATTER),
                    endTime.format(STANDARD_DATE_TIME_FORMATTER));
                log.info(result);
                return BaseResponse.success(result);
            }

            String subject = String.format("质量监控系统运行确认 - %s",
                endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            String content = String.format(
                "质量监控系统运行正常。\n\n" +
                "检测时间范围：%s 至 %s\n" +
                "检测结果：未发现质量问题\n" +
                "系统状态：正常运行\n\n" +
                "此邮件仅发送给管理员，用于确认系统正常工作。",
                startTime.format(STANDARD_DATE_TIME_FORMATTER),
                endTime.format(STANDARD_DATE_TIME_FORMATTER)
            );

            // 仅发送给管理员邮箱
            String[] adminEmails = notificationConfig.getAdminEmails();
            int successCount = 0;

            for (String adminEmail : adminEmails) {
                try {
                    emailService.sendSimpleEmail(adminEmail, subject, content);
                    successCount++;
                    log.info("管理员确认邮件发送成功，收件人：{}", adminEmail);
                } catch (Exception e) {
                    log.error("管理员确认邮件发送失败，收件人：{}，错误：{}", adminEmail, e.getMessage());
                }
            }

            if (successCount > 0) {
                String result = String.format("时间窗口 %s 至 %s 内未检测到质量问题，已向%d个管理员发送确认邮件",
                    startTime.format(STANDARD_DATE_TIME_FORMATTER),
                    endTime.format(STANDARD_DATE_TIME_FORMATTER),
                    successCount);
                log.info(result);
                return BaseResponse.success(result);
            } else {
                return BaseResponse.error("管理员确认邮件发送失败");
            }

        } catch (Exception e) {
            log.error("发送管理员确认邮件失败", e);
            return BaseResponse.error("发送管理员确认邮件失败：" + e.getMessage());
        }
    }

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
}
