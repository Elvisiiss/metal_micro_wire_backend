package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.Root;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Root用户数据访问层
 */
@Repository
public interface RootRepository extends JpaRepository<Root, Long> {
    
    /**
     * 根据用户名查找Root用户
     */
    Optional<Root> findByUserName(String userName);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUserName(String userName);
} 