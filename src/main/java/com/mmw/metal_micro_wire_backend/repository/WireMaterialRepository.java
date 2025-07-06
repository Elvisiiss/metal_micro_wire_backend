package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.WireMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线材数据访问层
 */
@Repository
public interface WireMaterialRepository extends JpaRepository<WireMaterial, String>, JpaSpecificationExecutor<WireMaterial> {

    /**
     * 根据应用场景编号查找线材数据
     */
    List<WireMaterial> findByScenarioCode(String scenarioCode);

    /**
     * 根据最终评估结果查找线材数据
     */
    List<WireMaterial> findByFinalEvaluationResult(WireMaterial.FinalEvaluationResult finalEvaluationResult);

    /**
     * 根据多个最终评估结果查找线材数据
     * 用于获取需要人工处理的线材（未评估 + 待审核）
     */
    List<WireMaterial> findByFinalEvaluationResultIn(List<WireMaterial.FinalEvaluationResult> finalEvaluationResults);

    /**
     * 根据多个最终评估结果分页查找线材数据
     * 用于获取需要人工处理的线材（未评估 + 待审核）
     */
    Page<WireMaterial> findByFinalEvaluationResultIn(List<WireMaterial.FinalEvaluationResult> finalEvaluationResults, Pageable pageable);

    // ==================== 溯源分析查询方法 ====================

    /**
     * 根据生产商查找线材数据
     */
    List<WireMaterial> findByManufacturer(String manufacturer);

    /**
     * 根据负责人查找线材数据
     */
    List<WireMaterial> findByResponsiblePerson(String responsiblePerson);

    /**
     * 根据工艺类型查找线材数据
     */
    List<WireMaterial> findByProcessType(String processType);

    /**
     * 根据生产机器查找线材数据
     */
    List<WireMaterial> findByProductionMachine(String productionMachine);

    /**
     * 根据时间范围查找线材数据
     */
    List<WireMaterial> findByEventTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据生产商和时间范围查找线材数据
     */
    List<WireMaterial> findByManufacturerAndEventTimeBetween(String manufacturer, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据负责人和时间范围查找线材数据
     */
    List<WireMaterial> findByResponsiblePersonAndEventTimeBetween(String responsiblePerson, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据工艺类型和时间范围查找线材数据
     */
    List<WireMaterial> findByProcessTypeAndEventTimeBetween(String processType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据生产机器和时间范围查找线材数据
     */
    List<WireMaterial> findByProductionMachineAndEventTimeBetween(String productionMachine, LocalDateTime startTime, LocalDateTime endTime);

    // ==================== 统计查询方法 ====================

    /**
     * 按生产商统计质量数据
     */
    @Query(value = "SELECT w.manufacturer, " +
           "COUNT(w.batch_number) as totalCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PENDING_REVIEW' THEN 1 ELSE 0 END) as pendingReviewCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'UNKNOWN' THEN 1 ELSE 0 END) as unknownCount, " +
           "w.contact_email " +
           "FROM wire_materials w " +
           "WHERE w.event_time >= COALESCE(?1, '1900-01-01'::timestamp) " +
           "AND w.event_time <= COALESCE(?2, '2100-12-31'::timestamp) " +
           "AND (COALESCE(?3, '') = '' OR w.scenario_code = ?3) " +
           "AND (COALESCE(?4, '') = '' OR w.manufacturer = ?4) " +
           "GROUP BY w.manufacturer, w.contact_email " +
           "ORDER BY failCount DESC, totalCount DESC", nativeQuery = true)
    List<Object[]> getManufacturerStatistics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("scenarioCode") String scenarioCode,
                                            @Param("dimensionValue") String dimensionValue);

    /**
     * 按负责人统计质量数据
     */
    @Query(value = "SELECT w.responsible_person, " +
           "COUNT(w.batch_number) as totalCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PENDING_REVIEW' THEN 1 ELSE 0 END) as pendingReviewCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'UNKNOWN' THEN 1 ELSE 0 END) as unknownCount, " +
           "w.contact_email " +
           "FROM wire_materials w " +
           "WHERE w.event_time >= COALESCE(?1, '1900-01-01'::timestamp) " +
           "AND w.event_time <= COALESCE(?2, '2100-12-31'::timestamp) " +
           "AND (COALESCE(?3, '') = '' OR w.scenario_code = ?3) " +
           "AND (COALESCE(?4, '') = '' OR w.responsible_person = ?4) " +
           "GROUP BY w.responsible_person, w.contact_email " +
           "ORDER BY failCount DESC, totalCount DESC", nativeQuery = true)
    List<Object[]> getResponsiblePersonStatistics(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("scenarioCode") String scenarioCode,
                                                 @Param("dimensionValue") String dimensionValue);

    /**
     * 按工艺类型统计质量数据
     */
    @Query(value = "SELECT w.process_type, " +
           "COUNT(w.batch_number) as totalCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PENDING_REVIEW' THEN 1 ELSE 0 END) as pendingReviewCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'UNKNOWN' THEN 1 ELSE 0 END) as unknownCount, " +
           "w.contact_email " +
           "FROM wire_materials w " +
           "WHERE w.event_time >= COALESCE(?1, '1900-01-01'::timestamp) " +
           "AND w.event_time <= COALESCE(?2, '2100-12-31'::timestamp) " +
           "AND (COALESCE(?3, '') = '' OR w.scenario_code = ?3) " +
           "AND (COALESCE(?4, '') = '' OR w.process_type = ?4) " +
           "GROUP BY w.process_type, w.contact_email " +
           "ORDER BY failCount DESC, totalCount DESC", nativeQuery = true)
    List<Object[]> getProcessTypeStatistics(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("scenarioCode") String scenarioCode,
                                          @Param("dimensionValue") String dimensionValue);

    /**
     * 按生产机器统计质量数据
     */
    @Query(value = "SELECT w.production_machine, " +
           "COUNT(w.batch_number) as totalCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PENDING_REVIEW' THEN 1 ELSE 0 END) as pendingReviewCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'UNKNOWN' THEN 1 ELSE 0 END) as unknownCount, " +
           "w.contact_email " +
           "FROM wire_materials w " +
           "WHERE w.event_time >= COALESCE(?1, '1900-01-01'::timestamp) " +
           "AND w.event_time <= COALESCE(?2, '2100-12-31'::timestamp) " +
           "AND (COALESCE(?3, '') = '' OR w.scenario_code = ?3) " +
           "AND (COALESCE(?4, '') = '' OR w.production_machine = ?4) " +
           "GROUP BY w.production_machine, w.contact_email " +
           "ORDER BY failCount DESC, totalCount DESC", nativeQuery = true)
    List<Object[]> getProductionMachineStatistics(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("scenarioCode") String scenarioCode,
                                                 @Param("dimensionValue") String dimensionValue);

    /**
     * 获取指定维度和值的不合格批次详情 - 生产商维度（无时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.manufacturer = ?1 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByManufacturerNoTime(@Param("manufacturer") String manufacturer);

    /**
     * 获取指定维度和值的不合格批次详情 - 生产商维度（有时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.manufacturer = ?1 " +
           "AND w.event_time >= ?2 " +
           "AND w.event_time <= ?3 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByManufacturerWithTime(@Param("manufacturer") String manufacturer,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定维度和值的不合格批次详情 - 负责人维度（无时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.responsible_person = ?1 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByResponsiblePersonNoTime(@Param("responsiblePerson") String responsiblePerson);

    /**
     * 获取指定维度和值的不合格批次详情 - 负责人维度（有时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.responsible_person = ?1 " +
           "AND w.event_time >= ?2 " +
           "AND w.event_time <= ?3 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByResponsiblePersonWithTime(@Param("responsiblePerson") String responsiblePerson,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定维度和值的不合格批次详情 - 工艺类型维度（无时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.process_type = ?1 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByProcessTypeNoTime(@Param("processType") String processType);

    /**
     * 获取指定维度和值的不合格批次详情 - 工艺类型维度（有时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.process_type = ?1 " +
           "AND w.event_time >= ?2 " +
           "AND w.event_time <= ?3 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByProcessTypeWithTime(@Param("processType") String processType,
                                                            @Param("startTime") LocalDateTime startTime,
                                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定维度和值的不合格批次详情 - 生产机器维度（无时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.production_machine = ?1 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByProductionMachineNoTime(@Param("productionMachine") String productionMachine);

    /**
     * 获取指定维度和值的不合格批次详情 - 生产机器维度（有时间过滤）
     */
    @Query(value = "SELECT * FROM wire_materials w " +
           "WHERE w.final_evaluation_result = 'FAIL' " +
           "AND w.production_machine = ?1 " +
           "AND w.event_time >= ?2 " +
           "AND w.event_time <= ?3 " +
           "ORDER BY w.event_time DESC", nativeQuery = true)
    List<WireMaterial> getFailedBatchesByProductionMachineWithTime(@Param("productionMachine") String productionMachine,
                                                                  @Param("startTime") LocalDateTime startTime,
                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取指定维度和值的不合格批次详情
     * 通过调用具体的维度方法来避免openGauss参数类型推断问题
     */
    default List<WireMaterial> getFailedBatchesByDimension(String dimension, String dimensionValue,
                                                          LocalDateTime startTime, LocalDateTime endTime) {
        boolean hasTimeFilter = startTime != null && endTime != null;

        switch (dimension) {
            case "MANUFACTURER":
                return hasTimeFilter ?
                    getFailedBatchesByManufacturerWithTime(dimensionValue, startTime, endTime) :
                    getFailedBatchesByManufacturerNoTime(dimensionValue);
            case "RESPONSIBLE_PERSON":
                return hasTimeFilter ?
                    getFailedBatchesByResponsiblePersonWithTime(dimensionValue, startTime, endTime) :
                    getFailedBatchesByResponsiblePersonNoTime(dimensionValue);
            case "PROCESS_TYPE":
                return hasTimeFilter ?
                    getFailedBatchesByProcessTypeWithTime(dimensionValue, startTime, endTime) :
                    getFailedBatchesByProcessTypeNoTime(dimensionValue);
            case "PRODUCTION_MACHINE":
                return hasTimeFilter ?
                    getFailedBatchesByProductionMachineWithTime(dimensionValue, startTime, endTime) :
                    getFailedBatchesByProductionMachineNoTime(dimensionValue);
            default:
                throw new IllegalArgumentException("不支持的维度类型: " + dimension);
        }
    }

    /**
     * 获取总体统计信息
     */
    @Query(value = "SELECT " +
           "COUNT(w.batch_number) as totalCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'PASS' THEN 1 ELSE 0 END) as passCount, " +
           "SUM(CASE WHEN w.final_evaluation_result = 'FAIL' THEN 1 ELSE 0 END) as failCount " +
           "FROM wire_materials w " +
           "WHERE w.event_time >= COALESCE(?1, '1900-01-01'::timestamp) " +
           "AND w.event_time <= COALESCE(?2, '2100-12-31'::timestamp) " +
           "AND (COALESCE(?3, '') = '' OR w.scenario_code = ?3)", nativeQuery = true)
    List<Object[]> getOverallStatistics(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("scenarioCode") String scenarioCode);
}