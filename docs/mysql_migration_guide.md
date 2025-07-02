# MySQL 迁移指南

## 概述

本指南将帮助你将睡眠积分奖励App从 Firebase Firestore 迁移到 MySQL 数据库。由于你们公司已有 MySQL 服务器用于日志和系统数据，使用 MySQL 可以更好地集成到现有基础设施中。

## 迁移优势

### ✅ 优势
- **现有基础设施**：利用公司已有的 MySQL 服务器
- **团队熟悉度**：开发团队对 MySQL 更熟悉，维护成本低
- **数据一致性**：MySQL 的 ACID 特性比 Firebase Firestore 更强
- **成本控制**：避免 Firebase 的按使用量计费
- **数据导出**：MySQL 数据导出和分析更方便
- **性能优化**：可以通过索引、查询优化等手段提升性能
- **备份恢复**：MySQL 的备份恢复机制更成熟

### ⚠️ 需要考虑的问题
- **实时同步**：Firebase 的实时数据同步需要重新实现
- **身份认证**：需要替换 Firebase Auth
- **移动端适配**：Android App 需要修改数据访问层
- **部署复杂度**：需要管理 MySQL 服务器

## 技术架构对比

| 特性 | Firebase Firestore | MySQL |
|------|-------------------|-------|
| 数据库类型 | NoSQL | 关系型数据库 |
| 实时同步 | 内置支持 | 需要 WebSocket/轮询 |
| 身份认证 | Firebase Auth | 自定义实现 |
| 扩展性 | 自动扩展 | 手动扩展 |
| 成本 | 按使用量计费 | 固定成本 |
| 数据一致性 | 最终一致性 | ACID |
| 查询能力 | 有限 | 强大的 SQL 查询 |

## 迁移步骤

### 第一步：环境准备

1. **安装 MySQL 依赖**
```bash
npm install mysql2
```

2. **配置环境变量**
创建 `.env` 文件：
```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=sleep_app_db
```

3. **运行数据库初始化脚本**
```bash
# 方法一：直接执行 SQL 文件
mysql -u root -p < MySQL_Schema.sql

# 方法二：使用智能同步脚本
node fixMySQLSchema.js
```

### 第二步：数据迁移

1. **从 Firebase 导出数据**
```javascript
// 使用现有的 Firebase 脚本导出数据
const admin = require('firebase-admin');
const fs = require('fs');

// 导出用户数据
const usersSnapshot = await admin.firestore().collection('users').get();
const users = usersSnapshot.docs.map(doc => ({
  id: doc.id,
  ...doc.data()
}));
fs.writeFileSync('users_export.json', JSON.stringify(users, null, 2));
```

2. **转换数据格式**
```javascript
// 转换用户数据格式
const convertUserData = (firebaseUser) => ({
  id: firebaseUser.id,
  email: firebaseUser.email,
  password_hash: generatePasswordHash(firebaseUser.password), // 需要重新生成
  nickname: firebaseUser.nickname || '',
  avatar: firebaseUser.avatar || '',
  phone: firebaseUser.phone || '',
  gender: firebaseUser.gender || '',
  birthday: firebaseUser.birthday || null,
  status: firebaseUser.status || '启用',
  sleep_status: firebaseUser.sleepStatus || '未知',
  points: firebaseUser.points || 0,
  sleep_points_today: firebaseUser.sleepPointsToday || 0,
  streak: firebaseUser.streak || 0,
  register_time: firebaseUser.registerTime || new Date(),
  last_login_time: firebaseUser.lastLoginTime || null,
  extra_info: JSON.stringify(firebaseUser.extraInfo || {}),
  update_time: firebaseUser.updateTime || new Date()
});
```

3. **导入到 MySQL**
```javascript
const mysql = require('mysql2/promise');

async function importUsers(users) {
  const connection = await mysql.createConnection(dbConfig);
  
  for (const user of users) {
    await connection.execute(`
      INSERT INTO users SET ?
    `, [user]);
  }
  
  await connection.end();
}
```

### 第三步：身份认证系统替换

1. **实现 JWT 认证**
```javascript
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');

// 用户登录
async function login(email, password) {
  const [users] = await connection.execute(
    'SELECT * FROM users WHERE email = ?',
    [email]
  );
  
  if (users.length === 0) {
    throw new Error('用户不存在');
  }
  
  const user = users[0];
  const isValid = await bcrypt.compare(password, user.password_hash);
  
  if (!isValid) {
    throw new Error('密码错误');
  }
  
  // 生成 JWT token
  const token = jwt.sign(
    { userId: user.id, email: user.email },
    process.env.JWT_SECRET,
    { expiresIn: '7d' }
  );
  
  return { user, token };
}
```

2. **中间件验证**
```javascript
const authenticateToken = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ error: '未提供认证令牌' });
  }
  
  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: '令牌无效' });
    }
    req.user = user;
    next();
  });
};
```

### 第四步：API 接口开发

1. **用户管理 API**
```javascript
// 获取用户列表
app.get('/api/users', authenticateToken, async (req, res) => {
  const [users] = await connection.execute(`
    SELECT * FROM users ORDER BY register_time DESC
  `);
  res.json(users);
});

// 更新用户信息
app.put('/api/users/:id', authenticateToken, async (req, res) => {
  const { id } = req.params;
  const updateData = req.body;
  
  await connection.execute(`
    UPDATE users SET ? WHERE id = ?
  `, [updateData, id]);
  
  res.json({ success: true });
});
```

2. **积分管理 API**
```javascript
// 添加积分流水
app.post('/api/points', authenticateToken, async (req, res) => {
  const { userId, points, reason, type } = req.body;
  
  await connection.execute(`
    INSERT INTO points_history (id, user_id, points_today, reason, type)
    VALUES (?, ?, ?, ?, ?)
  `, [generateUUID(), userId, points, reason, type]);
  
  res.json({ success: true });
});
```

### 第五步：移动端适配

1. **修改 Android App 数据访问层**
```kotlin
// 替换 Firebase 为 HTTP API 调用
class ApiService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://your-api-domain.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(ApiInterface::class.java)
    
    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email, password))
    }
    
    suspend fun getUserData(userId: String): User {
        return api.getUser(userId)
    }
}
```

2. **实现数据同步**
```kotlin
// 定期同步数据到服务器
class DataSyncWorker(context: Context) : CoroutineWorker(context) {
    override suspend fun doWork(): Result {
        val localData = getLocalData()
        val serverData = getServerData()
        
        // 合并数据
        val mergedData = mergeData(localData, serverData)
        
        // 更新本地和服务器
        updateLocalData(mergedData)
        updateServerData(mergedData)
        
        return Result.success()
    }
}
```

## 性能优化建议

### 1. 数据库优化
```sql
-- 创建复合索引
CREATE INDEX idx_users_email_status ON users(email, status);
CREATE INDEX idx_points_history_user_time ON points_history(user_id, create_time);

-- 分区表（如果数据量大）
ALTER TABLE points_history PARTITION BY RANGE (YEAR(create_time)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

### 2. 缓存策略
```javascript
const Redis = require('ioredis');
const redis = new Redis();

// 缓存用户数据
async function getUserWithCache(userId) {
  const cached = await redis.get(`user:${userId}`);
  if (cached) {
    return JSON.parse(cached);
  }
  
  const [users] = await connection.execute(
    'SELECT * FROM users WHERE id = ?',
    [userId]
  );
  
  if (users.length > 0) {
    await redis.setex(`user:${userId}`, 3600, JSON.stringify(users[0]));
    return users[0];
  }
  
  return null;
}
```

### 3. 连接池配置
```javascript
const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: process.env.MYSQL_HOST,
  user: process.env.MYSQL_USER,
  password: process.env.MYSQL_PASSWORD,
  database: process.env.MYSQL_DATABASE,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  acquireTimeout: 60000,
  timeout: 60000,
  reconnect: true
});
```

## 监控和维护

### 1. 数据库监控
```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看查询缓存命中率
SHOW STATUS LIKE 'Qcache_hits';
```

### 2. 日志记录
```javascript
// 记录系统操作日志
async function logAction(userId, action, targetType, targetId, description) {
  await connection.execute(`
    INSERT INTO system_logs (user_id, action, target_type, target_id, description, ip_address)
    VALUES (?, ?, ?, ?, ?, ?)
  `, [userId, action, targetType, targetId, description, req.ip]);
}
```

### 3. 备份策略
```bash
# 每日备份
mysqldump -u root -p sleep_app_db > backup_$(date +%Y%m%d).sql

# 自动备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u root -p sleep_app_db > /backup/sleep_app_$DATE.sql
find /backup -name "sleep_app_*.sql" -mtime +7 -delete
```

## 迁移检查清单

### 环境准备
- [ ] MySQL 服务器已安装并配置
- [ ] 数据库用户权限已设置
- [ ] 环境变量已配置
- [ ] 依赖包已安装

### 数据迁移
- [ ] Firebase 数据已导出
- [ ] 数据格式已转换
- [ ] MySQL 表结构已创建
- [ ] 数据已导入 MySQL
- [ ] 数据完整性已验证

### 功能迁移
- [ ] 身份认证系统已替换
- [ ] API 接口已开发
- [ ] 移动端已适配
- [ ] 数据同步机制已实现
- [ ] 错误处理已完善

### 性能优化
- [ ] 数据库索引已优化
- [ ] 缓存策略已实现
- [ ] 连接池已配置
- [ ] 查询性能已测试

### 监控维护
- [ ] 日志记录已实现
- [ ] 监控系统已部署
- [ ] 备份策略已制定
- [ ] 故障恢复方案已准备

## 总结

MySQL 迁移是一个复杂但值得的过程，特别是对于有现有 MySQL 基础设施的公司。通过合理的规划和实施，可以获得更好的性能、更低的成本和更强的数据控制能力。

建议分阶段进行迁移，先迁移核心功能，再逐步完善其他功能，确保系统的稳定性和可用性。 