
> 新手用户请优先阅读《使用说明.txt》，可获得快速上手和常见问题解答。

---

## 1. 项目简介
睡眠积分奖励App是一款帮助用户养成健康作息习惯的应用。通过监控手机使用情况，按时睡觉和起床可获得积分，积分可兑换奖品。

## 2. 主要功能
- 用户注册/登录，设置作息时间
- 睡眠监控，检测睡觉和起床
- 达标奖励积分，连续达标有额外奖励
- 违规惩罚，积分清零或中断连续
- 数据统计与历史查询
- 兑换商城，积分换奖品
- 数据本地与云端同步
- 后台管理系统（管理员专用）

## 3. 快速开始
### 安卓App端
1. 安装Android Studio和JDK 11+
2. 克隆本项目，配置google-services.json
3. 用Android Studio打开并运行

#### 如何将App安装到手机
**方法一：通过Android Studio安装（推荐）**
1. 用数据线将手机连接到电脑，并在手机上开启"开发者模式"和"USB调试"。
2. 在Android Studio中点击"运行"按钮，选择你的手机设备，App会自动安装到手机上。

**方法二：通过APK文件安装**
1. 在Android Studio中点击"Build">"Build Bundle(s) / APK(s)">"Build APK(s)"。
2. 构建完成后，点击提示中的"locate"找到生成的APK文件。
3. 将APK文件拷贝到手机，点击安装（如提示"禁止安装未知应用"，请在手机设置中允许）。

### 后台管理系统
1. 进入sleep_app_html目录
2. 运行`npm install`和`npm start`
3. 浏览器访问 http://localhost:3000
4. 管理员账号需在Firebase后台设置

## 4. 详细功能说明
### 用户端
- 注册/登录：邮箱注册，首次设置作息
- 睡眠监控：自动检测睡觉、起床时间，违规提醒
- 积分系统：按时睡觉/起床得分，连续奖励，违规清零
- 数据统计：睡眠成功率、积分明细、历史记录
- 兑换商城：虚拟/实物商品兑换
- 数据同步：本地Room数据库+云端Firestore

### 管理端
- 管理员登录（需在admins集合添加UID）
- 用户管理：查看、编辑、停用/启用、删除用户
- 商品管理：添加、编辑、上下架、库存管理
- 积分流水：查看、搜索、导出
- 数据统计：活跃度、积分分布、兑换统计
- 日志管理：操作日志、导出
- 批量操作：批量推送、批量修复脚本

## 5. 技术架构
- Android端：Kotlin、MVVM、Room、WorkManager、Glide、Firebase Auth/Firestore
- 后台管理：React+TypeScript+Ant Design Pro、Firebase
- 数据同步：本地与云端自动同步
- 安全：严格的Firebase安全规则

## 6. 项目结构
```
app/                  # 安卓App源码
sleep_app_html/       # 后台管理系统源码
README.md             # 项目说明
使用说明.txt           # 新手快速上手指南
notice.md             # 问题修复与维护记录
rules.txt             # Firestore安全规则
SQL.md                # 数据库结构定义文件
fixUsersSchema.js     # 智能数据库结构同步脚本
```

## 7. 数据库管理
### 智能结构同步脚本
项目提供了智能的数据库结构同步脚本，可以自动读取 `SQL.md` 文件并同步到 Firestore：

**使用方法：**
```bash
# 同步所有表结构
node fixUsersSchema.js

# 同步指定表结构
node fixUsersSchema.js users
node fixUsersSchema.js points_history
node fixUsersSchema.js products
```

**工作流程：**
1. 修改 `SQL.md` 文件中的表结构（添加新字段、修改默认值等）
2. 运行 `node fixUsersSchema.js` 命令
3. 脚本自动解析 `SQL.md` 并同步到数据库

**支持的字段类型：**
- `VARCHAR/TEXT` → 字符串，默认值 `""`
- `INT` → 数字，默认值 `0`
- `TIMESTAMP` → 时间戳，默认值 `服务器时间`
- `JSON` → 对象，默认值 `{}`
- `DATE` → 日期，默认值 `null`

**示例：**
```sql
-- 在 SQL.md 中添加新字段
CREATE TABLE users (
  id VARCHAR(64) PRIMARY KEY,
  email VARCHAR(128),
  newField VARCHAR(64) DEFAULT '默认值' COMMENT '新字段说明'
);
```

然后运行 `node fixUsersSchema.js users`，新字段会自动添加到所有用户记录中。

### MySQL 迁移支持
项目现在支持迁移到 MySQL 数据库，特别适合有现有 MySQL 基础设施的公司。

**MySQL 迁移优势：**
- ✅ 利用现有 MySQL 服务器基础设施
- ✅ 团队对 MySQL 更熟悉，维护成本低
- ✅ 更强的数据一致性和 ACID 特性
- ✅ 避免 Firebase 按使用量计费
- ✅ 更方便的数据导出和分析

**迁移文件：**
- `MySQL_Schema.sql` - MySQL 数据库结构定义
- `fixMySQLSchema.js` - MySQL 智能结构同步脚本
- `MySQL迁移指南.md` - 详细迁移指南

**MySQL 使用方法：**
```bash
# 安装 MySQL 依赖
npm install mysql2

# 配置环境变量
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password
export MYSQL_DATABASE=sleep_app_db

# 初始化 MySQL 数据库
mysql -u root -p < MySQL_Schema.sql

# 使用智能同步脚本
node fixMySQLSchema.js
```

**MySQL 特性：**
- 完整的表结构设计，包含外键约束
- 自动触发器更新用户积分
- 统计视图和存储过程
- 完整的索引优化
- 系统日志和配置管理

详细迁移步骤请参考 `MySQL迁移指南.md` 文件。

## 8. 安全与权限
- 用户只能访问自己的数据
- 管理员权限隔离，需在admins集合添加UID
- 商品管理、日志、系统配置仅管理员可操作
- 详细安全规则见rules.txt

## 9. 测试与发布
- 单元测试：`./gradlew test`
- UI测试：`./gradlew connectedAndroidTest`
- 性能测试：Android Profiler、Battery Historian
- 发布历史与版本说明见下方"发布说明"

## 10. 常见问题
- 运行报错：检查依赖（Node.js、JDK、SDK等）
- App无法登录：检查google-services.json
- 后台无法登录：检查管理员UID设置
- 数据不同步：检查网络和Firebase配置
- 详细问题见《使用说明.txt》

## 11. 贡献指南
1. Fork项目，创建分支，提交Pull Request
2. 建议先提Issue讨论新功能或Bug
3. 详细贡献流程见下方"贡献指南"

## 12. 未来计划与建议
### 短期目标（1-2周）
- 完善用户管理（密码重置、注册/登录时间、活跃度统计）
- 增强数据统计（用户增长、积分分布、兑换统计、达标率）
- 商品管理优化（图片存储、分类、库存、上下架）

### 中期目标（1个月）
- 系统监控（日志、审计、备份、性能监控）
- 批量操作（积分、账号、数据导入导出、通知）
- 通知系统（公告、用户通知、邮件、推送）

### 长期目标（2-3个月）
- 高级分析（用户行为、睡眠质量、积分、商品偏好）
- 移动端适配（响应式、PWA、离线）
- 多语言支持（中英文切换、国际化、本地化）

### 建议
- 持续关注用户反馈，优化用户体验
- 增加社交互动，提升用户粘性
- 加强数据安全和隐私保护
- 定期备份数据，防止丢失

## 13. 联系方式
- 项目维护者：[请填写]
- 邮箱：[请填写]
- 项目主页：[请填写]

---

> 详细开发文档、API说明、批量修复脚本等请参见本文件后续章节和《使用说明.txt》。如有建议欢迎反馈！

---

# 以下为详细技术文档与历史内容（供开发者查阅）

## 开发环境要求
- Android Studio Arctic Fox (2020.3.1) 或更高版本
- JDK 11 或更高版本
- Android SDK 31 (Android 12) 或更高版本
- Gradle 7.0.2 或更高版本
- Node.js 16+ (用于后台管理系统)

## 依赖版本
```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.7.0'
    implementation 'androidx.appcompat:appcompat:1.4.1'
    implementation 'com.google.android.material:material:1.5.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.3'
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-firestore:24.0.1'
    implementation 'androidx.room:room-runtime:2.4.2'
    implementation 'androidx.work:work-runtime-ktx:2.7.1'
    implementation 'com.github.bumptech.glide:glide:4.12.0'
}
```

## 核心功能
1. **用户系统**
   - 邮箱注册登录
   - 首次使用引导（设置初始作息时间）
2. **睡眠监控**
   - 检测手机使用时间段
   - 记录用户设定的"睡觉开始时间"和"起床闹钟时间"
   - 判断是否在睡觉期间违规使用手机
3. **积分规则**
   - 每日积分条件：
     - 在设定睡觉时间前30分钟未使用手机 +1分
     - 起床后1小时内关闭闹钟 +1分
   - 连续达标奖励：
     - 连续3天额外+3分
     - 连续7天额外+10分
   - 违规惩罚：
     - 睡觉期间使用手机 → 当日积分清零
     - 未按时关闭闹钟 → 中断连续记录
4. **数据统计**
   - 睡眠成功率统计
   - 积分获取明细（睡前积分、起床积分、奖励积分）
   - 历史记录查询（按日期范围）
   - 最长连续天数记录
5. **兑换商城**
   - 虚拟商品（壁纸/音效包）
   - 优惠券（咖啡优惠券）
   - 实体商品（睡眠眼罩等）
6. **数据同步**
   - 本地数据存储（Room数据库）
   - 云端数据备份（Firebase Firestore）
   - 每日自动同步
7. **后台管理系统**
   - 管理员登录认证
   - 用户管理（查看用户列表、用户详情）
   - 商品管理（添加、编辑、删除商品）
   - 积分流水管理（查看用户积分历史）
   - 数据统计看板（用户活跃度、积分分布等）
   - 调试信息弹窗（显示登录状态和权限信息）

## 后台管理系统功能
### 管理员登录
- 只有Firebase Firestore中`admins`集合中存在的UID才能登录后台
- 使用邮箱和密码进行身份验证
- 登录成功后自动显示调试信息弹窗

### 调试信息弹窗
登录成功后会自动弹出调试信息窗口，显示以下信息：
- **当前登录UID**: 当前登录用户的唯一标识符
- **管理员UID列表**: 所有具有管理员权限的用户UID，多个UID用分号(;)分隔
- **request.auth.uid**: Firebase安全规则中的认证用户UID值

**弹窗示例输出格式:**

    当前登录UID: abc123def456
    管理员UID列表: abc123def456; xyz789uvw012
    request.auth.uid: abc123def456

### 页面功能
1. **数据统计**: 显示用户活跃度、积分分布等统计图表
2. **用户管理**: 查看所有注册用户列表和详细信息
3. **商品管理**: 管理兑换商城中的商品信息
4. **积分流水**: 查看所有用户的积分获取和消费记录

## Firebase安全规则
### 安全规则概述
项目使用严格的Firebase Firestore安全规则来保护数据安全。规则文件位置：`sleep_app_html/rules.txt`

### 主要安全规则
#### 1. 用户数据保护
```javascript
match /users/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```
#### 2. 管理员权限控制
```javascript
match /admins/{adminId} {
  allow read, write: if request.auth != null && request.auth.uid == adminId;
}
function isAdmin() {
  return exists(/databases/$(database)/documents/admins/$(request.auth.uid));
}
```
#### 3. 商品管理
```javascript
match /products/{productId} {
  allow read: if request.auth != null;
  allow write: if isAdmin();
}
```
#### 4. 个人数据保护
```javascript
match /points_history/{docId} {
  allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
}
match /sleep_records/{docId} {
  allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
}
match /redemptions/{docId} {
  allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
}
```
#### 5. 系统管理
```javascript
match /system_config/{configId} {
  allow read, write: if isAdmin();
}
match /logs/{logId} {
  allow read, write: if isAdmin();
}
```

### 安全规则部署
1. 在Firebase控制台中进入Firestore Database
2. 点击"规则"标签
3. 将`rules.txt`文件内容复制到规则编辑器中
4. 点击"发布"按钮

### 安全规则特点
- **最小权限原则**：用户只能访问自己需要的数据
- **身份验证要求**：所有操作都需要用户登录
- **管理员权限隔离**：只有管理员可以管理系统数据
- **数据所有权保护**：用户只能访问属于自己的数据
- **默认拒绝策略**：未明确允许的操作都会被拒绝

## 注意事项
- 应用需要"使用情况访问权限"才能正常工作，首次运行时会引导用户开启该权限
- 为了保证服务能够持续监控睡眠状态，请确保应用不被系统杀死或者被用户手动关闭
- 商城功能需要连接网络才能加载最新商品信息
- 后台管理系统需要管理员权限才能访问
- 调试弹窗会在每次登录后自动显示，帮助排查权限问题

## 已实现功能补充
- 榜单自动聚合：系统自动统计并生成用户积分、睡眠表现等榜单，无需手动操作。
- 批量推送：支持管理员对选定用户进行批量消息推送。
- 日志管理页面：提供系统操作日志的查询、筛选、导出等功能，便于追踪和审计。
- 日志导出：导出日志时可自定义选择需要导出的列，默认全选（打钩），满足不同数据分析需求。

## 测试说明
1. 单元测试
   - 使用JUnit4进行单元测试
   - 使用Mockito进行模拟测试
   - 运行命令：`./gradlew test`
2. 集成测试
   - 使用Espresso进行UI测试
   - 运行命令：`./gradlew connectedAndroidTest`
3. 性能测试
   - 使用Android Profiler监控内存使用
   - 使用Battery Historian分析电量消耗

## 发布说明
### v1.0.0 (2024-03-20)
- 初始版本发布
- 实现基础睡眠监控功能
- 完成积分系统
- 支持数据同步

### v1.1.0 (2024-12-19)
- 添加后台管理系统
- 实现管理员登录功能
- 添加调试信息弹窗
- 支持用户管理、商品管理、积分流水查看
- 添加数据统计看板
- 完成Firebase安全规则配置
- 修复权限问题，确保调试弹窗正常显示

### v1.1.1 (2024-12-19) - 当前版本
- ✅ 管理员登录功能完全正常
- ✅ 调试信息弹窗正常显示
- ✅ Firebase安全规则配置完成
- ✅ 后台界面无权限错误
- ✅ 基础数据统计页面
- ✅ 商品管理功能完善（支持拖拽上传图片）
- ✅ 积分流水管理功能增强（用户搜索、用户管理）
- ✅ 用户管理功能（修改积分、停用/启用账号、删除账号）
- ⚠️ 需要扩展管理员权限以访问完整数据

### v1.1.2 (2024-12-19) - 最新版本
- ✅ 修复商品管理表单验证问题
- ✅ 添加商品图片拖拽上传功能
- ✅ 完善商品管理界面（删除功能、状态显示）
- ✅ 积分流水页面支持用户邮箱模糊搜索
- ✅ 添加用户管理弹窗功能
- ✅ 支持修改用户积分
- ✅ 支持停用/启用用户账号
- ✅ 支持删除用户账号
- ✅ 改进用户界面和用户体验
- ✅ 完善错误处理和提示信息

### v1.2.0 (计划中)
- 添加社交功能
- 优化电量消耗
- 增加更多积分奖励方式
- 后台管理系统功能扩展

## 贡献指南
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 问题反馈
如果您发现任何问题或有改进建议，请：
1. 在GitHub Issues中提交问题
2. 详细描述问题现象和复现步骤
3. 提供设备型号和系统版本信息

## 许可证
本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---
