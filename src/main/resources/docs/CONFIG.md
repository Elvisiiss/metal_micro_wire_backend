# 配置文件说明

## application.yml 配置详解

### JWT Token配置
```yaml
jwt:
  # JWT签名密钥，生产环境请使用复杂的随机字符串
  secret: metal_micro_wire_secret_key_2024_very_secure_random_key_for_jwt_signing
  
  # 普通登录Token过期时间（小时）
  # 建议值：1-4小时，平衡安全性和用户体验
  expiration-normal: 2
  
  # 记住登录Token过期时间（小时）
  # 建议值：168小时（7天）到720小时（30天）
  expiration-remember: 168
```

### 验证码配置
```yaml
verification:
  # 验证码有效期（分钟）
  # 建议值：3-10分钟，太短用户来不及输入，太长安全性降低
  code-expire-minutes: 5
  
  # 验证码发送冷却时间（秒）
  # 建议值：30-120秒，防止频繁发送但允许合理重试
  send-cooldown-seconds: 60
```

### 数据库配置
```yaml
spring:
   datasource:
      url: jdbc:postgresql://127.0.0.1:5432/postgres?useSSL=false&serverTimezone=Asia/Shanghai
      username: username
      password: password
      driver-class-name: org.postgresql.Driver
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password  # 如有密码
      database: 0
      timeout: 10000
```

### 邮件配置
```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your_email@qq.com
    password: your_email_auth_code
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            enable: true
```

## 环境配置建议

### 开发环境
```yaml
jwt:
  expiration-normal: 8        # 8小时，方便开发调试
  expiration-remember: 168    # 7天

verification:
  code-expire-minutes: 10     # 10分钟，方便测试
  send-cooldown-seconds: 30   # 30秒，方便快速测试
```

### 测试环境
```yaml
jwt:
  expiration-normal: 4        # 4小时
  expiration-remember: 168    # 7天

verification:
  code-expire-minutes: 5      # 5分钟
  send-cooldown-seconds: 60   # 60秒
```

### 生产环境
```yaml
jwt:
  secret: "生产环境请使用更复杂的密钥"
  expiration-normal: 2        # 2小时，提高安全性
  expiration-remember: 168    # 7天

verification:
  code-expire-minutes: 5      # 5分钟
  send-cooldown-seconds: 60   # 60秒，防止恶意攻击
```

## 配置优化建议

### 安全性考虑
1. **JWT密钥**：
   - 生产环境使用至少32位的随机字符串
   - 定期轮换密钥（需要考虑现有Token失效）
   - 不要在代码中硬编码，使用环境变量

2. **Token过期时间**：
   - 普通登录时间不宜过长，建议1-4小时
   - 记住登录时间根据业务需求，一般7-30天
   - 敏感操作可以要求重新验证

3. **验证码安全**：
   - 冷却时间不宜过短，防止暴力攻击
   - 有效期不宜过长，降低被猜测风险
   - 生产环境建议增加图形验证码

### 性能考虑
1. **Redis连接**：
   - 配置连接池参数
   - 设置合适的超时时间
   - 监控Redis性能

2. **数据库连接**：
   - 配置连接池大小
   - 设置连接超时时间
   - 启用SQL性能监控

### 用户体验
1. **Token过期**：
   - 提供Token刷新机制（可选）
   - 过期前提醒用户
   - 自动跳转到登录页

2. **验证码体验**：
   - 显示剩余冷却时间
   - 提供重新发送按钮
   - 验证码输入错误提示

## 配置文件模板

### application-dev.yml（开发环境）
```yaml
spring:
  profiles:
    active: dev

jwt:
  secret: dev_secret_key_for_development_only
  expiration-normal: 8
  expiration-remember: 168

verification:
  code-expire-minutes: 10
  send-cooldown-seconds: 30

logging:
  level:
    com.mmw.metal_micro_wire_backend: DEBUG
```

### application-prod.yml（生产环境）
```yaml
spring:
  profiles:
    active: prod

jwt:
  secret: ${JWT_SECRET:your_production_secret_key}
  expiration-normal: ${JWT_NORMAL_HOURS:2}
  expiration-remember: ${JWT_REMEMBER_HOURS:168}

verification:
  code-expire-minutes: ${VERIFICATION_EXPIRE_MINUTES:5}
  send-cooldown-seconds: ${VERIFICATION_COOLDOWN_SECONDS:60}

logging:
  level:
    com.mmw.metal_micro_wire_backend: INFO
```

## 环境变量配置

生产环境建议使用环境变量：

```bash
# JWT配置
export JWT_SECRET="your_very_secure_production_secret_key"
export JWT_NORMAL_HOURS=2
export JWT_REMEMBER_HOURS=168

# 验证码配置
export VERIFICATION_EXPIRE_MINUTES=5
export VERIFICATION_COOLDOWN_SECONDS=60

# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=metal_micro_wire_new
export DB_USERNAME=root
export DB_PASSWORD=your_db_password

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password

# 邮件配置
export MAIL_HOST=smtp.qq.com
export MAIL_USERNAME=your_email@qq.com
export MAIL_PASSWORD=your_email_auth_code
``` 
