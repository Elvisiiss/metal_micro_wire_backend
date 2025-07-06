package com.mmw.metal_micro_wire_backend.task;

import com.mmw.metal_micro_wire_backend.config.NotificationConfig;
import com.mmw.metal_micro_wire_backend.dto.BaseResponse;
import com.mmw.metal_micro_wire_backend.dto.traceability.TraceabilityQueryRequest;
import com.mmw.metal_micro_wire_backend.service.EmailService;
import com.mmw.metal_micro_wire_backend.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 质量监控定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.quality-monitor.enabled", havingValue = "true", matchIfMissing = true)
public class QualityMonitorTask {

    private final TraceabilityService traceabilityService;
    private final NotificationConfig notificationConfig;
    private final EmailService emailService;
    
    /**
     * 每小时检测一次质量问题
     * 可通过配置文件调整频率
     */
    @Scheduled(cron = "${app.quality-monitor.cron:0 0 * * * ?}")
    public void autoDetectQualityIssues() {
        log.info("开始执行定时质量问题检测任务");
        
        try {
            BaseResponse<String> result = traceabilityService.autoDetectAndNotifyQualityIssues();
            
            if ("success".equals(result.getCode())) {
                log.info("定时质量问题检测任务完成：{}", result.getData());
            } else {
                log.error("定时质量问题检测任务失败：{}", result.getMsg());
            }
            
        } catch (Exception e) {
            log.error("定时质量问题检测任务异常", e);
        }
    }
    
    /**
     * 每天凌晨2点生成质量报告
     */
    @Scheduled(cron = "${app.quality-report.cron:0 0 2 * * ?}")
    @ConditionalOnProperty(name = "app.quality-report.enabled", havingValue = "true", matchIfMissing = true)
    public void generateDailyQualityReport() {
        log.info("开始生成每日质量报告");

        try {
            // 生成昨日质量统计报告
            LocalDateTime endTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime startTime = endTime.minusDays(1);

            TraceabilityQueryRequest request = new TraceabilityQueryRequest();
            request.setDimension(TraceabilityQueryRequest.QueryDimension.MANUFACTURER);
            request.setStartTime(startTime);
            request.setEndTime(endTime);
            request.setFailRateThreshold(notificationConfig.getFailRateThreshold());

            // 获取各维度统计数据
            BaseResponse<?> manufacturerStats = traceabilityService.getQualityStatistics(request);

            request.setDimension(TraceabilityQueryRequest.QueryDimension.RESPONSIBLE_PERSON);
            BaseResponse<?> responsiblePersonStats = traceabilityService.getQualityStatistics(request);

            request.setDimension(TraceabilityQueryRequest.QueryDimension.PROCESS_TYPE);
            BaseResponse<?> processTypeStats = traceabilityService.getQualityStatistics(request);

            request.setDimension(TraceabilityQueryRequest.QueryDimension.PRODUCTION_MACHINE);
            BaseResponse<?> productionMachineStats = traceabilityService.getQualityStatistics(request);

            // 发送报告给管理员
            if (notificationConfig.getAdminEmails().length > 0) {
                String reportContent = generateReportContent(startTime, endTime,
                    manufacturerStats, responsiblePersonStats, processTypeStats, productionMachineStats);

                for (String adminEmail : notificationConfig.getAdminEmails()) {
                    try {
                        emailService.sendHtmlEmail(adminEmail,
                            String.format("每日质量报告 - %s", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))),
                            reportContent);
                        log.info("每日质量报告已发送给管理员：{}", adminEmail);
                    } catch (Exception e) {
                        log.error("发送每日质量报告给管理员{}失败", adminEmail, e);
                    }
                }
            }

            log.info("每日质量报告生成完成");

        } catch (Exception e) {
            log.error("生成每日质量报告异常", e);
        }
    }

    /**
     * 生成质量报告内容
     */
    private String generateReportContent(LocalDateTime startTime, LocalDateTime endTime,
                                       BaseResponse<?> manufacturerStats,
                                       BaseResponse<?> responsiblePersonStats,
                                       BaseResponse<?> processTypeStats,
                                       BaseResponse<?> productionMachineStats) {

        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>每日质量报告</h2>");
        html.append("<p><strong>报告时间：</strong>").append(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .append(" 至 ").append(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("</p>");

        html.append("<h3>生产商质量统计</h3>");
        html.append("<p>").append(manufacturerStats.getData() != null ? manufacturerStats.getData().toString() : "无数据").append("</p>");

        html.append("<h3>负责人质量统计</h3>");
        html.append("<p>").append(responsiblePersonStats.getData() != null ? responsiblePersonStats.getData().toString() : "无数据").append("</p>");

        html.append("<h3>工艺类型质量统计</h3>");
        html.append("<p>").append(processTypeStats.getData() != null ? processTypeStats.getData().toString() : "无数据").append("</p>");

        html.append("<h3>生产机器质量统计</h3>");
        html.append("<p>").append(productionMachineStats.getData() != null ? productionMachineStats.getData().toString() : "无数据").append("</p>");

        html.append("<p><em>此报告由系统自动生成</em></p>");
        html.append("</body></html>");

        return html.toString();
    }
}
