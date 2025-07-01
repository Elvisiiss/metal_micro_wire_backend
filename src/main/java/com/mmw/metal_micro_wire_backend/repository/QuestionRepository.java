package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 问题数据访问层
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
} 