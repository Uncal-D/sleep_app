 # Firebase Firestore 数据库结构对照表（SQL风格）

> 本表用于帮助你理解和核查各集合（表）的字段、类型和用途。实际存储为NoSQL，但用SQL表结构便于对照。

---

## 1. 用户表（users）
```sql
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY COMMENT '用户ID（文档ID）',
  email VARCHAR(128) COMMENT '邮箱',
  nickname VARCHAR(64) COMMENT '昵称',
  avatar VARCHAR(256) COMMENT '头像URL',
  phone VARCHAR(32) COMMENT '手机号',
  gender VARCHAR(8) COMMENT '性别',
  birthday DATE COMMENT '生日',
  status VARCHAR(8) DEFAULT '启用' COMMENT '账号状态：启用/停用',
  sleepStatus VARCHAR(8) DEFAULT '未知' COMMENT '当前睡眠状态：睡着/清醒/未知',
  points INT DEFAULT 0 COMMENT '当前总积分',
  sleepPointsToday INT DEFAULT 0 COMMENT '今日获得积分',
  streak INT DEFAULT 0 COMMENT '连续达标天数',
  registerTime TIMESTAMP COMMENT '注册时间',
  lastLoginTime TIMESTAMP COMMENT '最后登录时间',
  extraInfo JSON COMMENT '扩展信息',
  updateTime TIMESTAMP COMMENT '最后更新时间'
);
```

---

## 2. 积分流水表（points_history）
```sql
CREATE TABLE points_history (
  id VARCHAR(64) PRIMARY KEY COMMENT '流水ID（文档ID）',
  userId VARCHAR(64) COMMENT '用户ID',
  pointsToday int comment '当日获得积分',
  pointsTotal INT COMMENT '用户总积分',
  pointsConc INT COMMENT '连续达标天数',
  reason VARCHAR(64) COMMENT '变动原因',
  type VARCHAR(16) COMMENT '类型（system、reward、exchange等）',
  createTime TIMESTAMP COMMENT '创建时间',
  operatorId VARCHAR(64) COMMENT '操作人ID',
  extraInfo JSON COMMENT '扩展信息'
);
```

---

## 3. 商品表（products）
```sql
CREATE TABLE products (
  id VARCHAR(64) PRIMARY KEY COMMENT '商品ID（文档ID）',
  name VARCHAR(64) COMMENT '商品名称',
  description TEXT COMMENT '商品描述',
  image VARCHAR(256) COMMENT '商品图片URL',
  price INT COMMENT '所需积分',
  stock INT COMMENT '库存',
  status VARCHAR(8) DEFAULT '上架' COMMENT '商品状态：上架/下架/已删除',
  category VARCHAR(32) COMMENT '商品分类',
  createTime TIMESTAMP COMMENT '创建时间',
  updateTime TIMESTAMP COMMENT '最后更新时间',
  extraInfo JSON COMMENT '扩展信息'
);
```

---

## 4. 管理员表（admins）
```sql
CREATE TABLE admins (
  id VARCHAR(64) PRIMARY KEY COMMENT '管理员UID（文档ID）',
  email VARCHAR(128) COMMENT '管理员邮箱',
  nickname VARCHAR(64) COMMENT '昵称',
  createTime TIMESTAMP COMMENT '创建时间',
  extraInfo JSON COMMENT '扩展信息'
);
```

---

## 5. 兑换记录表（redemptions）
```sql
CREATE TABLE redemptions (
  id VARCHAR(64) PRIMARY KEY COMMENT '兑换记录ID（文档ID）',
  userId VARCHAR(64) COMMENT '用户ID',
  productId VARCHAR(64) COMMENT '商品ID',
  quantity INT COMMENT '兑换数量',
  createTime TIMESTAMP COMMENT '兑换时间',
  userAddressInfoID VARCHAR(64) COMMENT '用户兑换地址ID',
  extraInfo JSON COMMENT '扩展信息'
);
```

---

## 6. 用户地市表（userAddress）
```sql
CREATE TABLE points_history (
  userAddressInfoID VARCHAR(64) PRIMARY KEY COMMENT '用户兑换地址ID',
  userId VARCHAR(64) COMMENT '用户ID',
  name VARCHAR(64) COMMENT '收货人姓名',
  phoneNumber VARCHAR(16) COMMENT '收货人电话',
  createTime TIMESTAMP COMMENT '创建时间',
  userAddressInfo VARCHAR(64) COMMENT '收货人地址',
  userTrackingNumber VARCHAR(64) COMMENT '快递单号',
  extraInfo JSON COMMENT '扩展信息'
);
```


---

> 说明：
> - Firestore为NoSQL数据库，字段可动态扩展，表结构仅供参考。
> - 字段类型为常见SQL类型，实际存储以Firestore为准。
> - extraInfo字段建议用于存储灵活扩展信息。
> - 如需导出实际数据，请使用导出脚本。
> - 同步所有表结构
> - node fixUsersSchema.js
> - 
> - 只同步用户表
> - node fixUsersSchema.js users
> - 
> - 只同步积分流水表  
> - node fixUsersSchema.js points_history
