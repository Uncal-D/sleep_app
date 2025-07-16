# 项目开发与调试问题记录（notice.md）

本文件用于记录在本项目开发和调试过程中遇到的主要问题、解决方法和经验教训，帮助后续开发者或自己在遇到类似问题时能快速定位和解决。

---

## 1. Kotlin/Gradle 配置相关问题

### 问题描述
- 构建时报错：`kaptGenerateStubsDebugKotlin` 失败，提示 `Unknown Kotlin JVM target: 21`。

### 解决方法
- Kotlin 插件当前不支持 JVM target 21，需要将所有 Gradle 配置中的 `jvmTarget` 设置为 `17`。
- 检查 `build.gradle`（包括 app 和 project 级别）以及 IDE 的 Project Structure 设置，确保一致。
- 不要直接修改 `.idea/misc.xml` 和 `.idea/compiler.xml`，应通过 Android Studio 的 Project Structure 界面修改。

### 经验总结
- Kotlin/Gradle 相关配置要统一，优先通过 IDE 设置，避免手动改配置文件。
- 遇到构建版本相关报错，优先查阅官方文档和社区 FAQ。

### 2024-07-01 Kotlin JVM target: 21 构建错误彻底排查与修复经验
- 问题描述：
  - 构建时报错 `Unknown Kotlin JVM target: 21`，即使 build.gradle 中已设置 jvmTarget = '17'。
- 排查过程：
  1. 全面检查所有 build.gradle、gradle.properties 文件，确认未设置 jvmTarget=21。
  2. 检查本地 JDK 版本，确保 Android Studio 配置为 JDK 17。
  3. 发现问题根源为本地 Gradle/IDE 缓存残留了错误的 jvmTarget=21 设置。
- 解决方法：
  1. 在 Android Studio 中执行 File > Invalidate Caches / Restart，彻底清理缓存。
  2. 手动删除 .gradle/、android_app/.gradle/、android_app/app/build/ 等缓存目录。
  3. 重新编译，问题彻底解决。
- 经验总结：
  - 遇到 JVM target 报错，优先检查 build.gradle 和 JDK 版本。
  - 若配置无误但依旧报错，99%是缓存问题，务必清理所有缓存再重试。
  - 以后每次升级JDK或修改jvmTarget后，建议先清理缓存再编译。

---

## 2. Room 数据库与 DAO 查询问题

### 问题描述
- Room DAO 查询报错：`no such column: pointsEarned` 或 `no such column: streakBonus`。

### 解决方法
- 检查 Entity 数据类（如 `SleepRecordEntity`）的字段，确保 SQL 查询字段与数据类字段完全一致。
- 如果数据库结构有变动，务必同步更新 DAO 查询语句。
- 每次修改 Entity 字段后，记得升级数据库版本号（`version`），以触发 Room 自动迁移。

### 经验总结
- SQL 查询字段必须和 Entity 保持一致，字段名拼写要完全相同。
- 数据库结构变动要及时升级版本号，避免旧表结构导致的运行时错误。

---

## 3. ViewBinding 与布局文件问题

### 问题描述
- 代码中引用的 View ID 在布局 XML 文件中不存在，导致编译或运行时报错。

### 解决方法
- 检查对应的 XML 布局文件，确保所有在代码中引用的 View ID（如 `btnLogin`, `etEmail` 等）都已声明。
- 若新增或修改布局文件，需同步更新 ViewBinding 相关代码。

### 经验总结
- 布局文件和代码要保持同步，View ID 变动后及时检查和修改相关代码。
- 利用 IDE 的自动补全和跳转功能，快速定位和修正 View ID 问题。

---

## 4. 类型与导入不一致问题

### 问题描述
- 代码中类型不一致（如 `SleepRecordDto?` vs. `SleepRecord!`），或导入路径错误，导致编译失败。

### 解决方法
- 统一数据类型定义，避免同一实体在不同文件中类型不一致。
- 检查 import 路径，确保引用的是正确的类和包。

### 经验总结
- 项目中数据类型要统一，建议集中管理数据模型。
- 导入路径出错时，优先用 IDE 的自动导入和修复功能。

---

## 5. 项目结构与缺失文件问题

### 问题描述
- 缺少工具类（如 `TimeUtils`, `UsageStatsHelper`）、数据模型或实现类，导致找不到类错误。

### 解决方法
- 根据项目需求补全缺失的类和文件。
- 参考已有代码风格和结构，保持一致性。

### 经验总结
- 项目初期应梳理好结构，缺失文件要及时补全。
- 可以用占位类/方法先保证项目可编译，再逐步完善实现。

---

## 6. 其他常见问题与建议

- 遇到问题时，优先查看错误日志和提示，逐步定位问题根源。
- 每次修改后建议小步提交并编译，便于快速发现和定位新问题。
- 重要配置和结构变动后，及时更新 README 和 notice 文件，方便后续查阅。

---

## 7. React+Firebase 后台权限与调试弹窗问题（2024-06）

### 问题描述
- 在开发后台管理系统（React+Firebase）过程中，遇到多次"Missing or insufficient permissions"权限报错，导致用户管理、商品管理、积分流水等页面无法正常获取数据。
- 登录后台后，部分情况下调试信息弹窗未能自动弹出，影响权限链路排查。
- 管理员UID校验逻辑与Firestore安全规则联调时，出现UID不一致、权限链路不通等问题。
- 前端多次请求数据时，因token未刷新或auth.currentUser未就绪，导致权限校验失败。
- 终端（PowerShell）下npm命令运行报错，影响依赖安装和开发流程。

### 解决方法
- 前端所有页面数据请求前，统一增加`auth.currentUser`判断，确保登录后再发起请求。
- 登录页（LoginPage）登录成功后，前端主动校验UID是否在admins集合，只有管理员可进入后台。
- App.tsx中实现Tabs切换和登录后token刷新，避免token未刷新导致的权限报错。
- 按需实现登录后自动弹出调试信息弹窗，显示当前登录UID、管理员UID列表、request.auth.uid等，便于权限链路调试。
- 多次调整和收紧Firestore安全规则，逐步定位到规则导致的权限问题，最终采用"普通用户只能访问自己数据，products集合所有登录用户可读，只有管理员可写"的方案。
- 终端命令问题通过明确切换目录、规范npm命令格式解决。

### 经验总结
- Firebase权限报错优先排查安全规则，逐步放宽/收紧定位问题。
- 前端调试信息弹窗对权限链路排查极为重要，建议每次登录后自动弹出。
- 管理员UID校验要确保前端、后端、规则三方一致，避免因UID拼写或格式问题导致权限失效。
- 所有数据请求前务必校验auth.currentUser，避免未登录状态下发起请求。
- 终端命令建议用标准格式，遇到语法报错优先检查当前目录和命令拼写。
- 本次权限与调试问题排查耗时较长，建议后续遇到类似问题时，优先：1）打印调试信息 2）逐步调整安全规则 3）核查UID一致性。

## 8.遇到问题无法定位

### 问题描述
- 账号登录后多次因为权限问题导致后台无法访问

### 解决方法
- 通过增加弹窗显示当前用户UID，admins集合中的UID，以及
- 通过F12观察控制台的错误报告来定位问题

### 经验总结
- 可以通过弹窗或者控制台来输出或展示关键数值或字段来定位问题，从而解决问题
- 通过控制台检验网页报错内容，从而找到解决方案

### 2024-06-27 用户管理功能优化与文档同步
- 用户管理界面增加用户ID、连续天数、注册时间（自动格式化）、当前睡眠状态等字段展示。
- 支持通过用户ID、邮箱、睡眠状态多条件查询和筛选。
- 增加"刷新"按钮，可一键刷新所有用户的睡眠状态。
- 用户详情弹窗展示更多信息，睡眠状态旁增加刷新图标，支持单用户状态刷新。
- 修正注册时间显示Invalid Date问题。
- 同步更新README.md，详细补充用户管理功能说明。

### 2024-06-27 商品管理功能优化与文档同步
- 商品管理界面增加排序功能，支持对所有主要字段升序/降序排序。
- 增加多条件查询和筛选功能。
- 支持多选商品，批量上架、下架、修改积分、删除等操作，所有批量操作均有二次确认。
- 增加刷新按钮，随时拉取最新商品库存信息。
- 同步更新README.md，详细补充商品管理功能说明。

## 2024-06-27 用户管理功能补充实现记录
- 按README第398行后要求，补充实现了用户管理界面：
  - 增加了多条件查询（用户ID、邮箱、当前睡眠状态）
  - 列表展示所有关键字段，注册时间自动格式化
  - 顶部增加"刷新全部用户状态"按钮
  - 用户详情弹窗展示所有字段，睡眠状态旁增加刷新图标，可单独刷新该用户状态
- 代码已加详细注释，便于维护
- README已同步标注已实现功能

### 2024-06-09 字段结构调整与修复：
- 新增status字段，所有用户和新建用户默认为"启用"，可编辑。
- sleepStatus字段所有用户和新建用户默认为"未知"，可编辑。
- 所有积分相关页面、统计、编辑都用points字段。
- sleepPointsToday仅用于详情-积分流水。
- 删除totalPoints字段及所有相关代码。
- 用户积分变动后同步写入积分流水表，用户总积分等于积分流水表的积分总和。
- 积分流水页面正负积分变色。

### 2024-06-27 批量修复所有用户状态与积分流水
- 新增脚本 scripts/fix_firestore_users_schema.js
- 支持分批处理所有用户，将 status 设为"启用"，sleepStatus 设为"未知"，删除 totalPoints 字段
- 每个用户同步插入一条积分流水（reason="系统批量修正"，points=0）
- Firestore 批量操作有上限，脚本自动分批提交
- 操作过程有详细日志，便于排查
- 建议操作前备份数据库，避免误操作
- 如遇异常，优先检查 serviceAccountKey.json 权限和网络连接
- 本脚本仅用于批量修正历史数据，后续如需再次批量操作可复用

### 2024-06-30 登录失败弹窗优化与经验总结
- 登录失败时，原先直接弹出英文原始错误信息，用户体验较差。
- 现已根据Firebase异常类型，分别给出更友好的中文提示：
  - 账号不存在：账号不存在，请先注册
  - 密码错误：密码错误，请重试
  - 其他异常：登录失败，请检查网络或稍后重试
- 同时保留详细日志，便于开发排查。
- 经验教训：以后所有涉及用户交互的错误提示，都应根据异常类型给出用户能理解的友好提示，避免直接暴露英文原文或技术细节。

### 2024-12-19 智能数据库结构同步脚本开发
- **问题背景**：原先的 `fixUsersSchema.js` 脚本是硬编码的，每次修改数据库结构都需要手动更新脚本代码，容易出错且维护困难。
- **解决方案**：重写脚本为智能版本，能够自动读取 `SQL.md` 文件并解析表结构，然后同步到 Firestore 数据库。
- **核心功能**：
  - 自动解析 `SQL.md` 中的 `CREATE TABLE` 语句
  - 支持所有主要字段类型（VARCHAR、INT、TIMESTAMP、JSON、DATE）
  - 智能设置默认值（字符串为空、数字为0、时间戳为服务器时间等）
  - 支持同步单个表或所有表
  - 详细的执行日志和错误处理
- **使用方法**：
  ```bash
  # 同步所有表
  node fixUsersSchema.js
  
  # 同步指定表
  node fixUsersSchema.js users
  node fixUsersSchema.js points_history
  ```
- **工作流程**：
  1. 修改 `SQL.md` 文件中的表结构
  2. 运行同步脚本
  3. 自动解析并同步到数据库
- **经验总结**：
  - 数据库结构管理应该以文档为准，脚本自动同步，避免手动维护
  - 正则表达式解析 SQL 语句比硬编码更灵活，但需要注意边界情况
  - 详细的日志输出对调试和监控很重要
  - 支持命令行参数让脚本更灵活，可以针对性地同步特定表

### 2024-12-19 MySQL 迁移支持开发
- **需求背景**：用户公司已有 MySQL 服务器用于日志和系统数据，希望将项目迁移到 MySQL 以更好地集成到现有基础设施。
- **解决方案**：开发完整的 MySQL 迁移方案，包括数据库结构、同步脚本和详细指南。
- **核心文件**：
  - `MySQL_Schema.sql` - 完整的 MySQL 数据库结构设计
  - `fixMySQLSchema.js` - MySQL 版本的智能结构同步脚本
  - `MySQL迁移指南.md` - 详细的迁移步骤和最佳实践
- **MySQL 架构特点**：
  - 完整的表结构设计，包含外键约束和索引优化
  - 自动触发器实现积分计算和更新
  - 统计视图和存储过程支持复杂查询
  - 系统日志表和配置管理
  - 支持分区表和性能优化
- **迁移优势**：
  - 利用现有 MySQL 基础设施，降低部署成本
  - 团队对 MySQL 更熟悉，维护成本低
  - 更强的数据一致性和 ACID 特性
  - 避免 Firebase 按使用量计费
  - 更方便的数据导出和分析
- **技术实现**：
  - 使用 mysql2 驱动，支持 Promise 和连接池
  - 智能解析 SQL 文件，自动同步表结构
  - 支持环境变量配置，便于不同环境部署
  - 完整的错误处理和日志记录
- **使用示例**：
  ```bash
  # 安装依赖
  npm install mysql2
  
  # 配置环境变量
  export MYSQL_HOST=localhost
  export MYSQL_DATABASE=sleep_app_db
  
  # 初始化数据库
  mysql -u root -p < MySQL_Schema.sql
  
  # 使用智能同步脚本
  node fixMySQLSchema.js
  ```
- **经验总结**：
  - 数据库迁移需要充分考虑现有基础设施和团队技术栈
  - MySQL 的关系型特性提供了更强的数据一致性和查询能力
  - 需要重新设计身份认证和数据同步机制
  - 分阶段迁移可以降低风险，确保系统稳定性
  - 详细的迁移指南和检查清单对成功迁移至关重要

---

> 本文件会持续更新，欢迎在开发过程中补充更多问题与解决经验。 

### 什么是FireBase

前段时间学习PWA，在跟着官方教程完成demo后，想要在手机上测试一下效果。然而，遇到的一个问题就是：PWA需要HTTPS协议（或者使用localhost）。

这就需要我们有一个HTTPS的服务，并在其上面部署我们本地写好的demo。而官方demo的最后，推荐使用firebase来托管你的代码。

FireBase的各种功能与服务

在FireBase的众多使用场景中，Develop -> Hosting（托管）就是我需要用到的了。然而，在执行`firebase login`（账号登录）过程中，却遇到了一些问题。

### 在浏览器登录账号后，无反应（无法获取authorization code）

最开始，我在CLI中输入`firebase login`，选择`y`后，CLI会需要一个authorization code；而浏览器会打开并提示你进行登录。

这里我用google账户进行授权登录。然而，在授权之后，却迟迟没有响应（无法得到authorization code）。这时候，我发现浏览器显示，似乎是在等待`localhost`进行响应。

解决这个问题的方法就是：在登录时，使用`firebase login --no-localhost`进行登录。

重新使用`firebase login --no-localhost`登录。这里我选择了google账号进行登录，重复上面的过程：

这次，你就会在浏览器中获得一串authorization code值：

authorization code

将它粘贴到CLI中即可。

# 2025-07-01 项目源码恢复与架构重构记录

## 问题描述
- 本次项目在进行架构重构和目录规范化过程中，因批量移动命令操作失误，导致原有安卓端(app/)和Web后台(sleep_app_html/)源码全部丢失，android_app/和web_admin/目录为空。
- 经过多次排查，确认源码未进入回收站，原目录已被删除，且无最新Git提交可直接恢复。

## 解决过程
- 发现waibu目录下有一份较新的项目备份，包含完整的app/和sleep_app_html/源码。
- 通过人工比对和目录扫描，确认waibu/sleep_app/app/和waibu/sleep_app/sleep_app_html/下源码、资源、配置文件齐全。
- 按照新项目架构规范，将app/下所有源码、资源、配置迁移至android_app/，sleep_app_html/下所有源码、资源、配置迁移至web_admin/，并排除build、node_modules等自动生成目录。
- 迁移后逐一检查android_app/和web_admin/目录结构、关键文件、依赖配置，确认所有核心代码和资源已恢复。

## 经验教训
- 目录重构和批量移动操作前，**务必提前备份**项目全量源码，或至少用Git提交一次快照。
- 批量移动命令建议优先用copy/xcopy测试，确认无误后再删除原目录，避免move/rmdir导致不可逆丢失。
- 目录迁移后要逐一检查所有核心目录和文件，确保无遗漏。
- 养成定期备份和版本管理习惯，关键节点多做快照，防止意外丢失。
- 本次通过waibu备份成功恢复所有源码，避免了重大损失，后续开发请严格遵循规范操作。

---

### 2024-07-01 Android Studio 构建错误排查与修复记录

#### 问题1：未启用AndroidX导致构建失败
- 检查config/gradle.properties，已确认：
  - android.useAndroidX=true
  - android.enableJetifier=true
- 经验总结：所有新项目建议默认开启AndroidX，避免后续依赖冲突。

#### 问题2：缺少google-services.json文件
- 检查android_app/app/目录，未发现google-services.json。
- 检查android_app/目录，发现google-services.json。
- 解决方法：请将google-services.json从android_app/目录移动到android_app/app/目录下。
- 经验总结：集成Firebase等Google服务时，务必准备好配置文件并放在正确目录。

#### 问题3：依赖库重复（AndroidX与Support混用）
- 全局排查所有build.gradle文件，未发现com.android.support依赖，已统一为androidx。
- 经验总结：依赖库必须统一，混用会导致编译冲突。建议用IDE自动迁移工具辅助检查。

### 2024-07-01 AndroidX配置与依赖冲突彻底修复记录
- 问题：Android Studio报错未启用AndroidX，且存在support-compat依赖冲突。
- 处理：
  1. 将android.useAndroidX=true和android.enableJetifier=true添加到android_app/gradle.properties，确保配置生效。
  2. 在android_app/app/build.gradle中添加configurations.all { exclude ... }，强制排除所有com.android.support相关依赖。
- 经验总结：
  - AndroidX配置必须放在主工程gradle.properties，不能只放在config目录。
  - 依赖冲突优先用exclude彻底排除老support库。

### 2024-07-01 登录/注册异常提示优化经验
- 问题描述：
  - 登录和注册时，遇到账号不存在、密码错误、账号已存在、网络异常等情况，原先提示不友好，部分直接显示英文原文。
- 解决方法：
  1. 登录时根据异常类型，分别提示：账号不存在、密码错误、凭证失效、网络异常、其他异常。
  2. 注册时根据异常类型，分别提示：账号已存在、密码不一致、字段为空、网络异常、注册成功等。
  3. 所有提示均为中文，按钮超时自动恢复，体验媲美大厂。
- 经验总结：
  - 登录/注册等关键流程，必须对所有常见异常类型给出明确、友好的中文提示，不能直接显示英文原文。
  - 按钮要有超时保护，避免卡死。
  - 参考大厂产品体验，持续优化用户交互细节。

---

## 2025-07-16 Android应用关键问题解决记录

### 问题1: ProfileEditActivity黑屏崩溃问题
**现象**: 点击设置界面的铅笔图标后应用黑屏，提示ANR（应用无响应）
**定位方法**:
1. 添加全局异常处理器到SleepApp中
2. 使用adb logcat查看详细崩溃日志
3. 通过错误日志精确定位问题根源

**根本原因**: ActionBar冲突
```
java.lang.IllegalStateException: This Activity already has an action bar supplied by the window decor.
Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.
```

**解决方案**:
1. 在AndroidManifest.xml中为ProfileEditActivity指定NoActionBar主题：
   ```xml
   <activity
       android:name=".ui.ProfileEditActivity"
       android:theme="@style/Theme.SleepApp.NoActionBar" />
   ```
2. 在setupToolbar()方法中添加异常处理
3. 简化ProfileEditActivity，移除复杂的异步操作和头像上传功能

### 问题2: 商城兑换按钮无响应问题
**现象**: 点击商城界面的兑换按钮没有任何反应，无法打开兑换对话框
**定位方法**:
1. 在ProductAdapter、ShopFragment、RedeemDialogFragment、ShopViewModel中添加详细日志
2. 通过日志序列判断问题发生的具体位置
3. 使用Logcat过滤器：`ProductAdapter|ShopFragment|ShopViewModel|RedeemDialogFragment`

**根本原因**: ProductAdapter中只设置了整个卡片的点击事件，没有为兑换按钮单独设置点击事件
**解决方案**:
```kotlin
// 在ProductAdapter的ViewHolder中添加兑换按钮点击事件
binding.btnRedeem.setOnClickListener {
    val position = adapterPosition
    if (position != RecyclerView.NO_POSITION) {
        val product = getItem(position)
        Log.d("ProductAdapter", "兑换按钮被点击: ${product.name}")
        onProductClick(product)
    }
}
```

### 问题3: 主页布局不符合设计要求
**现象**: 主页布局与用户提供的设计图不匹配
**解决方案**: 完全按照用户要求重新设计主页布局
**新布局结构**:
1. 顶部标题栏："主页"
2. 欢迎模块："欢迎使用睡眠积分奖励" + 当前日期
3. 睡眠情况模块：达标状态 + 睡眠时间 + 起床时间 + 获得积分
4. 倒计时："距离下次睡觉还有：XX:XX:XX"
5. 精美古诗模块（随机显示古诗词）
6. 底部导航栏

### 问题4: 首次登录弹窗功能缺失
**现象**: 应用首次启动时没有显示欢迎弹窗
**解决方案**: 恢复HomeFragment中的欢迎弹窗功能
```kotlin
// 只在首次启动时显示欢迎弹窗
if (com.example.sleepapp.ui.MainActivity.isFirstLaunch) {
    showWelcomeDialog()
    com.example.sleepapp.ui.MainActivity.isFirstLaunch = false
}
```

### 系统化调试方法总结
1. **添加详细日志**: 在关键流程的每个步骤添加Log.d()输出
2. **异常捕获**: 使用try-catch包装可能出错的代码段
3. **日志过滤**: 使用特定标签便于在Logcat中筛选相关信息
4. **错误定位**: 通过日志序列判断问题发生的具体位置
5. **系统化排查**: 从UI层到数据层逐层检查，定位问题根源

### 编译错误修复经验
**问题**: Kotlin导入冲突错误
```
Conflicting import, imported name 'Handler' is ambiguous
```
**解决方案**: 清理重复的导入语句，确保每个类只导入一次

### 经验总结
- **问题定位**: 通过添加详细日志和异常处理，可以快速定位问题根源
- **系统化调试**: 按照UI层→业务层→数据层的顺序进行排查
- **预防措施**: 在关键操作中添加异常处理，提高应用稳定性
- **用户体验**: 及时修复影响用户操作的关键功能，如登录、兑换等
- **代码质量**: 保持代码简洁，避免过度复杂的异步操作导致ANR

---

## 2025-07-16 数据同步功能完善记录

### 数据库结构设计完善
**目标**: 确保app上的所有数据都与数据库保持同步

#### **1. 完善的数据库表结构设计**
创建了完整的数据库结构文档 `docs/database_schema.md`，包含：

**核心数据表**:
- `users` - 用户表：包含用户基本信息、积分、连续天数等
- `sleep_records` - 睡眠记录表：详细的睡眠数据记录
- `products` - 商品表：商城商品信息
- `redemption_records` - 兑换记录表：用户兑换历史
- `points_history` - 积分流水表：所有积分变化记录
- `app_settings` - 应用设置表：用户和全局设置

**数据字段完善**:
```sql
-- 用户表新增字段
avatar_url VARCHAR(500),           -- 头像URL
sleep_status VARCHAR(20),          -- 睡眠状态
max_streak INT,                    -- 最大连续天数
last_login_at TIMESTAMP,           -- 最后登录时间
timezone VARCHAR(50)               -- 时区

-- 睡眠记录表新增字段
sleep_quality VARCHAR(20),         -- 睡眠质量
phone_usage_minutes INT,           -- 手机使用时长
alarm_snooze_count INT,            -- 贪睡次数
streak_bonus INT                   -- 连续奖励积分
```

#### **2. 数据模型优化**
**User模型增强**:
- 添加了Firebase PropertyName注解支持
- 增加了睡眠状态、最大连续天数等字段
- 保持向后兼容性

**Product模型完善**:
- 添加了库存管理、分类、排序等字段
- 支持商品状态管理（上架/下架）
- 增强了商品类型枚举

**新增数据模型**:
- `SleepRecordDto` - Firebase同步版本的睡眠记录
- `RedemptionRecord` - 兑换记录模型
- `PointsHistory` - 积分流水模型
- `AppSetting` - 应用设置模型

#### **3. 数据同步策略实现**
**实时同步**:
- 用户操作立即同步到Firebase
- 积分变化实时更新并记录流水
- 兑换操作立即生效

**离线支持**:
- 本地Room数据库缓存关键数据
- 网络恢复后自动同步
- 冲突解决：服务器数据优先

**性能优化**:
- 商品数据缓存机制
- 分页加载历史记录
- 异步数据同步避免阻塞UI

#### **4. 核心功能实现**
**用户数据管理**:
```kotlin
// 更新用户积分并记录流水
suspend fun updateUserPoints(pointsChange: Int, description: String): Boolean

// 更新用户连续天数
suspend fun updateUserStreak(newStreak: Int): Boolean

// 更新睡眠状态
suspend fun updateUserSleepStatus(sleepStatus: String): Boolean
```

**商品兑换系统**:
```kotlin
// 完整的兑换流程
suspend fun redeemProduct(product: Product, quantity: Int = 1): Boolean {
    // 1. 检查积分是否足够
    // 2. 检查库存
    // 3. 创建兑换记录
    // 4. 扣除积分并记录流水
    // 5. 更新库存
}
```

**数据同步机制**:
```kotlin
// 统一的数据同步接口
suspend fun syncUserToFirebase(user: User): Boolean
suspend fun syncSleepRecordToFirebase(sleepRecord: SleepRecordDto): String?
suspend fun getUserFromFirebase(): User?
```

#### **5. 实现的同步功能**
✅ **用户数据同步**：
- 登录时自动同步用户信息
- 实时更新积分、连续天数、睡眠状态
- 记录最后登录时间

✅ **睡眠记录同步**：
- 睡眠数据实时上传到Firebase
- 支持离线记录，网络恢复后同步
- 详细的睡眠质量和行为数据

✅ **商城数据同步**：
- 商品信息从Firebase实时加载
- 兑换记录完整追踪
- 积分流水详细记录

✅ **设置数据同步**：
- 用户个人设置云端存储
- 全局应用设置统一管理
- 支持多设备同步

#### **6. 数据完整性保证**
**事务性操作**:
- 兑换操作确保积分扣除和记录创建的原子性
- 睡眠记录更新同时更新用户统计数据

**数据验证**:
- 积分不能为负数
- 兑换前检查库存和积分余额
- 数据类型和格式验证

**错误处理**:
- 网络异常时的重试机制
- 数据冲突的解决策略
- 详细的错误日志记录

### 技术实现亮点
1. **统一的数据访问层**: 通过Repository模式统一管理数据访问
2. **异步数据同步**: 使用Kotlin协程实现非阻塞数据同步
3. **缓存策略**: 本地缓存 + 云端同步的混合模式
4. **数据一致性**: 确保本地和云端数据的一致性
5. **扩展性设计**: 易于添加新的数据类型和同步规则

### 后续优化方向
- [ ] 实现数据同步状态指示器
- [ ] 添加数据同步冲突解决UI
- [ ] 实现增量数据同步
- [ ] 添加数据导出功能
- [ ] 实现多设备数据同步通知