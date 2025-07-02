-- MySQL 数据库结构设计
-- 睡眠积分奖励App - MySQL版本
-- 基于原有Firebase Firestore结构适配

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sleep_app_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE sleep_app_db;

-- 1. 用户表
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY COMMENT '用户ID（UUID）',
  email VARCHAR(128) UNIQUE NOT NULL COMMENT '邮箱',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  nickname VARCHAR(64) DEFAULT '' COMMENT '昵称',
  avatar VARCHAR(256) DEFAULT '' COMMENT '头像URL',
  phone VARCHAR(32) DEFAULT '' COMMENT '手机号',
  gender VARCHAR(8) DEFAULT '' COMMENT '性别',
  birthday DATE NULL COMMENT '生日',
  status VARCHAR(8) DEFAULT '启用' COMMENT '账号状态：启用/停用',
  sleep_status VARCHAR(8) DEFAULT '未知' COMMENT '当前睡眠状态：睡着/清醒/未知',
  points INT DEFAULT 0 COMMENT '当前总积分',
  sleep_points_today INT DEFAULT 0 COMMENT '今日获得积分',
  streak INT DEFAULT 0 COMMENT '连续达标天数',
  register_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
  extra_info JSON COMMENT '扩展信息',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  INDEX idx_email (email),
  INDEX idx_status (status),
  INDEX idx_register_time (register_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 积分流水表
CREATE TABLE points_history (
  id VARCHAR(64) PRIMARY KEY COMMENT '流水ID（UUID）',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  points_today INT DEFAULT 0 COMMENT '当日获得积分',
  points_total INT DEFAULT 0 COMMENT '用户总积分',
  points_conc INT DEFAULT 0 COMMENT '连续达标天数',
  reason VARCHAR(64) NOT NULL COMMENT '变动原因',
  type VARCHAR(16) DEFAULT 'system' COMMENT '类型（system、reward、exchange等）',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  operator_id VARCHAR(64) NULL COMMENT '操作人ID',
  extra_info JSON COMMENT '扩展信息',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_create_time (create_time),
  INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- 3. 商品表
CREATE TABLE products (
  id VARCHAR(64) PRIMARY KEY COMMENT '商品ID（UUID）',
  name VARCHAR(64) NOT NULL COMMENT '商品名称',
  description TEXT COMMENT '商品描述',
  image VARCHAR(256) DEFAULT '' COMMENT '商品图片URL',
  price INT NOT NULL DEFAULT 0 COMMENT '所需积分',
  stock INT DEFAULT 0 COMMENT '库存',
  status VARCHAR(8) DEFAULT '上架' COMMENT '商品状态：上架/下架/已删除',
  category VARCHAR(32) DEFAULT '' COMMENT '商品分类',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  extra_info JSON COMMENT '扩展信息',
  INDEX idx_status (status),
  INDEX idx_category (category),
  INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 4. 管理员表
CREATE TABLE admins (
  id VARCHAR(64) PRIMARY KEY COMMENT '管理员ID（UUID）',
  user_id VARCHAR(64) NOT NULL COMMENT '关联用户ID',
  email VARCHAR(128) NOT NULL COMMENT '管理员邮箱',
  nickname VARCHAR(64) DEFAULT '' COMMENT '昵称',
  role VARCHAR(32) DEFAULT 'admin' COMMENT '角色：admin/super_admin',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  extra_info JSON COMMENT '扩展信息',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_id (user_id),
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- 5. 兑换记录表
CREATE TABLE redemptions (
  id VARCHAR(64) PRIMARY KEY COMMENT '兑换记录ID（UUID）',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  product_id VARCHAR(64) NOT NULL COMMENT '商品ID',
  quantity INT DEFAULT 1 COMMENT '兑换数量',
  points_used INT NOT NULL COMMENT '消耗积分',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  user_address_info_id VARCHAR(64) NULL COMMENT '用户兑换地址ID',
  status VARCHAR(16) DEFAULT '待处理' COMMENT '状态：待处理/已发货/已完成/已取消',
  extra_info JSON COMMENT '扩展信息',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_product_id (product_id),
  INDEX idx_create_time (create_time),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兑换记录表';

-- 6. 用户地址表
CREATE TABLE user_address (
  id VARCHAR(64) PRIMARY KEY COMMENT '用户地址ID（UUID）',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
  phone_number VARCHAR(16) NOT NULL COMMENT '收货人电话',
  address TEXT NOT NULL COMMENT '收货人地址',
  is_default BOOLEAN DEFAULT FALSE COMMENT '是否默认地址',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  extra_info JSON COMMENT '扩展信息',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- 7. 睡眠记录表
CREATE TABLE sleep_records (
  id VARCHAR(64) PRIMARY KEY COMMENT '睡眠记录ID（UUID）',
  user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
  sleep_date DATE NOT NULL COMMENT '睡眠日期',
  sleep_time TIME NULL COMMENT '睡觉时间',
  wake_time TIME NULL COMMENT '起床时间',
  sleep_duration INT DEFAULT 0 COMMENT '睡眠时长（分钟）',
  is_compliant BOOLEAN DEFAULT FALSE COMMENT '是否达标',
  points_earned INT DEFAULT 0 COMMENT '获得积分',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  extra_info JSON COMMENT '扩展信息',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_date (user_id, sleep_date),
  INDEX idx_user_id (user_id),
  INDEX idx_sleep_date (sleep_date),
  INDEX idx_is_compliant (is_compliant)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='睡眠记录表';

-- 8. 系统日志表
CREATE TABLE system_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
  user_id VARCHAR(64) NULL COMMENT '操作用户ID',
  action VARCHAR(64) NOT NULL COMMENT '操作类型',
  target_type VARCHAR(32) NULL COMMENT '目标类型',
  target_id VARCHAR(64) NULL COMMENT '目标ID',
  description TEXT COMMENT '操作描述',
  ip_address VARCHAR(45) NULL COMMENT 'IP地址',
  user_agent TEXT NULL COMMENT '用户代理',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_action (action),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 9. 系统配置表
CREATE TABLE system_config (
  id VARCHAR(64) PRIMARY KEY COMMENT '配置ID',
  config_key VARCHAR(64) UNIQUE NOT NULL COMMENT '配置键',
  config_value TEXT COMMENT '配置值',
  description VARCHAR(255) COMMENT '配置描述',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认配置
INSERT INTO system_config (id, config_key, config_value, description) VALUES
('1', 'sleep_bonus_points', '1', '按时睡觉奖励积分'),
('2', 'wake_bonus_points', '1', '按时起床奖励积分'),
('3', 'streak_3_bonus', '3', '连续3天达标额外奖励'),
('4', 'streak_7_bonus', '10', '连续7天达标额外奖励'),
('5', 'violation_penalty', '0', '违规惩罚积分（清零）');

-- 创建视图：用户统计视图
CREATE VIEW user_stats AS
SELECT 
  u.id,
  u.email,
  u.nickname,
  u.points,
  u.streak,
  u.register_time,
  u.last_login_time,
  COUNT(sr.id) as total_sleep_records,
  SUM(CASE WHEN sr.is_compliant = 1 THEN 1 ELSE 0 END) as compliant_days,
  ROUND(SUM(CASE WHEN sr.is_compliant = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(sr.id), 2) as compliance_rate
FROM users u
LEFT JOIN sleep_records sr ON u.id = sr.user_id
GROUP BY u.id, u.email, u.nickname, u.points, u.streak, u.register_time, u.last_login_time;

-- 创建视图：积分统计视图
CREATE VIEW points_stats AS
SELECT 
  DATE(create_time) as date,
  COUNT(*) as total_transactions,
  SUM(CASE WHEN type = 'reward' THEN 1 ELSE 0 END) as reward_count,
  SUM(CASE WHEN type = 'exchange' THEN 1 ELSE 0 END) as exchange_count,
  SUM(points_today) as total_points_earned
FROM points_history
GROUP BY DATE(create_time)
ORDER BY date DESC;

-- 创建存储过程：计算用户积分
DELIMITER //
CREATE PROCEDURE CalculateUserPoints(IN user_id_param VARCHAR(64))
BEGIN
  DECLARE total_points INT DEFAULT 0;
  
  -- 计算用户总积分
  SELECT COALESCE(SUM(points_today), 0) INTO total_points
  FROM points_history 
  WHERE user_id = user_id_param;
  
  -- 更新用户表
  UPDATE users 
  SET points = total_points, 
      update_time = CURRENT_TIMESTAMP
  WHERE id = user_id_param;
  
  SELECT total_points as calculated_points;
END //
DELIMITER ;

-- 创建触发器：积分流水更新时自动更新用户积分
DELIMITER //
CREATE TRIGGER after_points_history_insert
AFTER INSERT ON points_history
FOR EACH ROW
BEGIN
  CALL CalculateUserPoints(NEW.user_id);
END //
DELIMITER ;

-- 创建触发器：积分流水更新时自动更新用户积分
DELIMITER //
CREATE TRIGGER after_points_history_update
AFTER UPDATE ON points_history
FOR EACH ROW
BEGIN
  CALL CalculateUserPoints(NEW.user_id);
END //
DELIMITER ;

-- 创建事件：每日重置今日积分
DELIMITER //
CREATE EVENT reset_daily_points
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  UPDATE users SET sleep_points_today = 0;
END //
DELIMITER ;

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 创建索引优化查询性能
CREATE INDEX idx_points_history_user_time ON points_history(user_id, create_time);
CREATE INDEX idx_sleep_records_user_date ON sleep_records(user_id, sleep_date);
CREATE INDEX idx_redemptions_user_time ON redemptions(user_id, create_time);
CREATE INDEX idx_users_status_points ON users(status, points);

-- 添加注释
COMMENT ON TABLE users IS '用户基本信息表';
COMMENT ON TABLE points_history IS '积分变动流水表';
COMMENT ON TABLE products IS '商品信息表';
COMMENT ON TABLE admins IS '管理员权限表';
COMMENT ON TABLE redemptions IS '商品兑换记录表';
COMMENT ON TABLE user_address IS '用户收货地址表';
COMMENT ON TABLE sleep_records IS '用户睡眠记录表';
COMMENT ON TABLE system_logs IS '系统操作日志表';
COMMENT ON TABLE system_config IS '系统配置参数表'; 