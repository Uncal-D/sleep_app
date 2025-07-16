# 睡眠积分奖励应用数据库结构设计

## 数据库表结构

### 1. users 用户表
```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,           -- Firebase UID
    email VARCHAR(255) NOT NULL UNIQUE,   -- 邮箱
    nickname VARCHAR(100),                 -- 昵称
    phone VARCHAR(20),                     -- 手机号
    avatar_url VARCHAR(500),               -- 头像URL
    sleep_time VARCHAR(10) DEFAULT '23:00', -- 目标睡觉时间
    wake_time VARCHAR(10) DEFAULT '07:00',  -- 目标起床时间
    total_points INT DEFAULT 0,            -- 总积分
    current_streak INT DEFAULT 0,          -- 当前连续天数
    max_streak INT DEFAULT 0,              -- 最大连续天数
    status VARCHAR(20) DEFAULT 'active',   -- 用户状态：active/inactive/banned
    sleep_status VARCHAR(20) DEFAULT 'unknown', -- 睡眠状态：good/normal/poor/unknown
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,               -- 最后登录时间
    timezone VARCHAR(50) DEFAULT 'Asia/Shanghai' -- 时区
);
```

### 2. sleep_records 睡眠记录表
```sql
CREATE TABLE sleep_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    record_date DATE NOT NULL,             -- 记录日期
    target_sleep_time TIME,                -- 目标睡觉时间
    target_wake_time TIME,                 -- 目标起床时间
    actual_sleep_time TIMESTAMP,           -- 实际睡觉时间
    actual_wake_time TIMESTAMP,            -- 实际起床时间
    sleep_duration_minutes INT,            -- 睡眠时长（分钟）
    sleep_quality VARCHAR(20) DEFAULT 'unknown', -- 睡眠质量：excellent/good/fair/poor/unknown
    is_sleep_on_time BOOLEAN DEFAULT FALSE, -- 是否按时睡觉
    is_wake_on_time BOOLEAN DEFAULT FALSE,  -- 是否按时起床
    phone_usage_minutes INT DEFAULT 0,     -- 睡眠期间手机使用时长
    alarm_snooze_count INT DEFAULT 0,      -- 闹钟贪睡次数
    points_earned INT DEFAULT 0,           -- 本次获得积分
    streak_bonus INT DEFAULT 0,            -- 连续奖励积分
    notes TEXT,                            -- 备注
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, record_date)
);
```

### 3. products 商品表
```sql
CREATE TABLE products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,            -- 商品名称
    description TEXT,                      -- 商品描述
    image_url VARCHAR(500),                -- 商品图片URL
    price INT NOT NULL,                    -- 积分价格
    type VARCHAR(50) NOT NULL,             -- 商品类型：virtual_wallpaper/virtual_sound/coupon/physical_item
    category VARCHAR(100),                 -- 商品分类
    stock_quantity INT DEFAULT -1,         -- 库存数量，-1表示无限
    is_active BOOLEAN DEFAULT TRUE,        -- 是否上架
    sort_order INT DEFAULT 0,              -- 排序权重
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4. redemption_records 兑换记录表
```sql
CREATE TABLE redemption_records (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(200) NOT NULL,    -- 冗余字段，防止商品删除后无法查看历史
    quantity INT DEFAULT 1,               -- 兑换数量
    points_cost INT NOT NULL,             -- 消耗积分
    status VARCHAR(20) DEFAULT 'pending', -- 状态：pending/processing/completed/cancelled
    delivery_info JSON,                   -- 配送信息
    notes TEXT,                           -- 备注
    redeemed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,               -- 处理时间
    completed_at TIMESTAMP,               -- 完成时间
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);
```

### 5. points_history 积分流水表
```sql
CREATE TABLE points_history (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    points_change INT NOT NULL,           -- 积分变化（正数为获得，负数为消耗）
    points_balance INT NOT NULL,          -- 变化后的积分余额
    type VARCHAR(50) NOT NULL,            -- 类型：sleep_reward/wake_reward/streak_bonus/redemption/manual_adjust
    source_id VARCHAR(255),               -- 来源ID（睡眠记录ID、兑换记录ID等）
    description VARCHAR(500),             -- 描述
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 6. app_settings 应用设置表
```sql
CREATE TABLE app_settings (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),                 -- 用户ID，NULL表示全局设置
    setting_key VARCHAR(100) NOT NULL,    -- 设置键
    setting_value TEXT,                   -- 设置值
    setting_type VARCHAR(20) DEFAULT 'string', -- 值类型：string/int/boolean/json
    description VARCHAR(500),             -- 设置描述
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_setting (user_id, setting_key)
);
```

## 索引优化
```sql
-- 用户表索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);

-- 睡眠记录表索引
CREATE INDEX idx_sleep_records_user_date ON sleep_records(user_id, record_date);
CREATE INDEX idx_sleep_records_date ON sleep_records(record_date);
CREATE INDEX idx_sleep_records_created_at ON sleep_records(created_at);

-- 商品表索引
CREATE INDEX idx_products_type ON products(type);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_sort ON products(sort_order);

-- 兑换记录表索引
CREATE INDEX idx_redemption_user_id ON redemption_records(user_id);
CREATE INDEX idx_redemption_status ON redemption_records(status);
CREATE INDEX idx_redemption_date ON redemption_records(redeemed_at);

-- 积分流水表索引
CREATE INDEX idx_points_history_user_id ON points_history(user_id);
CREATE INDEX idx_points_history_type ON points_history(type);
CREATE INDEX idx_points_history_created_at ON points_history(created_at);

-- 应用设置表索引
CREATE INDEX idx_app_settings_user_key ON app_settings(user_id, setting_key);
```

## 数据同步策略

### 1. 实时同步
- 用户操作（登录、设置修改）立即同步到Firebase
- 睡眠记录创建/更新立即同步
- 积分变化立即同步

### 2. 定期同步
- 每次应用启动时同步用户数据
- 每日定时同步睡眠记录
- 商品数据缓存1小时后重新同步

### 3. 离线支持
- 本地Room数据库缓存关键数据
- 网络恢复后自动同步离线期间的变更
- 冲突解决策略：服务器数据优先
