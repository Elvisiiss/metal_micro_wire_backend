package com.mmw.metal_micro_wire_backend.repository;

import com.mmw.metal_micro_wire_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    
    /**
     * 分页查询所有用户
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * 根据状态查询用户
     */
    List<User> findByStatus(Integer status);
    
    /**
     * 根据角色ID查询用户
     */
    List<User> findByRoleId(Integer roleId);
    
    /**
     * 根据用户名或邮箱模糊查询用户（分页）
     */
    @Query("SELECT u FROM User u WHERE u.userName LIKE %:keyword% OR u.email LIKE %:keyword%")
    Page<User> findByUserNameContainingOrEmailContaining(@Param("keyword") String keyword, Pageable pageable);
} 