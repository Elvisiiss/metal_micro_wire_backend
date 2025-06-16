package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUserName(String userName);
    
    /**
     * 根据邮箱或用户名查找用户
     */
    Optional<User> findByEmailOrUserName(String email, String userName);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUserName(String userName);
    
    /**
     * 根据邮箱和用户名查找用户
     */
    Optional<User> findByEmailAndUserName(String email, String userName);
} 