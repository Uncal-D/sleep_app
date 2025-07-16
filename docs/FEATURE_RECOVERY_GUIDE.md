# 功能恢复指南

## 📊 功能变化对比

### ✅ 保留的核心功能
- Android应用完整功能 (用户系统、睡眠监控、积分系统、兑换商城)
- Web管理后台基础功能 (用户管理、商品管理、积分流水)
- Firebase数据同步
- 安全规则配置

### ⚠️ 被移除/简化的功能
1. **MySQL迁移支持** - 完整的MySQL数据库替代方案
2. **智能数据库同步脚本** - 自动读取SQL.md并同步到数据库
3. **高级管理功能** - 批量推送、日志管理、系统监控
4. **详细的使用说明文档** - 新手快速上手指南

## 🔄 如何恢复被移除的功能

### 1. 恢复MySQL迁移支持

#### 当前状态
- MySQL相关文件已移动到 `docs/` 和 `scripts/` 目录
- 需要重新配置依赖和环境

#### 恢复步骤
```powershell
# 1. 安装MySQL依赖
cd web_admin
npm install mysql2

# 2. 创建环境配置文件
New-Item -Path ".env.local" -ItemType File
```

在 `.env.local` 中添加：
```env
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_USER=root
MYSQL_PASSWORD=your_password
MYSQL_DATABASE=sleep_app_db
```

#### 使用MySQL脚本
```powershell
# 初始化MySQL数据库
mysql -u root -p < docs\mysql_schema.sql

# 运行同步脚本
node scripts\fix_mysql_schema.js
```

### 2. 恢复智能数据库同步脚本

#### 创建SQL.md文件
```powershell
New-Item -Path "SQL.md" -ItemType File
```

添加数据库结构定义：
```sql
-- 用户表
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY,
  email VARCHAR(128),
  points INT DEFAULT 0,
  consecutiveDays INT DEFAULT 0,
  sleepStatus VARCHAR(32) DEFAULT 'awake',
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 积分历史表
CREATE TABLE points_history (
  id VARCHAR(64) PRIMARY KEY,
  userId VARCHAR(64),
  points INT,
  type VARCHAR(32),
  description TEXT,
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 使用同步脚本
```powershell
# 同步所有表结构
node scripts\fix_firestore_users_schema.js

# 同步指定表
node scripts\fix_firestore_users_schema.js users
```

### 3. 恢复高级管理功能

#### 批量推送功能
在 `web_admin/src/pages/` 创建 `BatchOperations.tsx`：
```typescript
import React, { useState } from 'react';
import { Button, Select, Input, message } from 'antd';
import { db } from '../services/firebase';
import { collection, query, where, getDocs, updateDoc, doc } from 'firebase/firestore';

const BatchOperations: React.FC = () => {
  const [selectedUsers, setSelectedUsers] = useState<string[]>([]);
  const [pushMessage, setPushMessage] = useState('');

  const handleBatchPush = async () => {
    try {
      // 批量推送逻辑
      for (const userId of selectedUsers) {
        await updateDoc(doc(db, 'notifications', userId), {
          message: pushMessage,
          timestamp: new Date(),
          read: false
        });
      }
      message.success('批量推送成功');
    } catch (error) {
      message.error('批量推送失败');
    }
  };

  return (
    <div>
      <h2>批量操作</h2>
      <Select
        mode="multiple"
        placeholder="选择用户"
        style={{ width: '100%', marginBottom: 16 }}
        onChange={setSelectedUsers}
      />
      <Input.TextArea
        placeholder="推送消息内容"
        value={pushMessage}
        onChange={(e) => setPushMessage(e.target.value)}
        rows={4}
        style={{ marginBottom: 16 }}
      />
      <Button type="primary" onClick={handleBatchPush}>
        发送批量推送
      </Button>
    </div>
  );
};

export default BatchOperations;
```

#### 日志管理功能
创建 `web_admin/src/pages/LogManagement.tsx`：
```typescript
import React, { useState, useEffect } from 'react';
import { Table, Button, DatePicker, Select, message } from 'antd';
import { db } from '../services/firebase';
import { collection, query, orderBy, limit, getDocs } from 'firebase/firestore';

const LogManagement: React.FC = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const q = query(
        collection(db, 'logs'),
        orderBy('timestamp', 'desc'),
        limit(100)
      );
      const querySnapshot = await getDocs(q);
      const logsData = querySnapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setLogs(logsData);
    } catch (error) {
      message.error('获取日志失败');
    } finally {
      setLoading(false);
    }
  };

  const exportLogs = () => {
    const csvContent = logs.map(log => 
      `${log.timestamp},${log.action},${log.userId},${log.details}`
    ).join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'logs.csv';
    a.click();
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const columns = [
    { title: '时间', dataIndex: 'timestamp', key: 'timestamp' },
    { title: '操作', dataIndex: 'action', key: 'action' },
    { title: '用户ID', dataIndex: 'userId', key: 'userId' },
    { title: '详情', dataIndex: 'details', key: 'details' },
  ];

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" onClick={exportLogs}>
          导出日志
        </Button>
        <Button onClick={fetchLogs} style={{ marginLeft: 8 }}>
          刷新
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={logs}
        loading={loading}
        rowKey="id"
      />
    </div>
  );
};

export default LogManagement;
```

### 4. 恢复详细使用说明

#### 创建使用说明文件
```powershell
New-Item -Path "docs\USER_GUIDE.md" -ItemType File
```

参考 `docs/readme_old.md` 中的内容，创建详细的使用说明。

## 🔧 配置路由和菜单

在 `web_admin/src/App.tsx` 中添加新页面的路由：
```typescript
import BatchOperations from './pages/BatchOperations';
import LogManagement from './pages/LogManagement';

// 在路由配置中添加
{
  path: '/batch-operations',
  element: <BatchOperations />
},
{
  path: '/log-management',
  element: <LogManagement />
}
```

## 📋 恢复优先级建议

### 高优先级 (立即恢复)
1. **详细使用说明文档** - 帮助新用户快速上手
2. **Firebase配置指南** - 确保项目能正常部署

### 中优先级 (按需恢复)
1. **智能数据库同步脚本** - 简化数据库维护
2. **基础日志管理** - 便于问题排查

### 低优先级 (可选恢复)
1. **MySQL迁移支持** - 仅在需要时恢复
2. **高级批量操作** - 功能增强

## 🚀 快速恢复命令

```powershell
# 1. 恢复基础文档
Copy-Item "docs\readme_old.md" "docs\DETAILED_USER_GUIDE.md"

# 2. 恢复智能同步脚本
Copy-Item "scripts\fix_firestore_users_schema.js" "fixUsersSchema.js"

# 3. 创建SQL定义文件
New-Item -Path "SQL.md" -ItemType File

# 4. 如果需要MySQL支持
cd web_admin
npm install mysql2
```

## 💡 建议

1. **优先确保核心功能正常** - Android应用和Web后台基础功能
2. **按需恢复高级功能** - 根据实际使用需求决定是否恢复
3. **保持项目结构清晰** - 避免重复配置文件
4. **定期备份重要配置** - 防止配置丢失

如果您需要恢复特定功能，请告诉我具体需求，我可以提供详细的恢复步骤。
