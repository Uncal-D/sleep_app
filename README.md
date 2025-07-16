# 睡眠积分奖励App

一个帮助用户养成健康作息习惯的Android应用，通过监控手机使用、积分奖励和商品兑换等方式激励用户保持良好的睡眠习惯。

## 📱 项目概述

本项目包含：
- **Android应用** (`android_app/`) - 主要的移动端应用
- **Web管理后台** (`web_admin/`) - 管理员后台系统
- **云函数** (`cloud_functions/`) - Firebase云函数
- **文档** (`docs/`) - 项目文档和说明

## ✨ 主要功能

### Android端功能
- **用户注册/登录** - 邮箱注册、登录验证、异常处理
- **睡眠监控** - 设置睡觉/起床时间、违规检测、积分管理
- **积分系统** - 积分获取、连续奖励、违规清零
- **兑换商城** - 商品浏览、积分兑换、兑换记录
- **数据同步** - 本地Room数据库与Firebase云端同步

### Web管理后台功能
- **用户管理** - 查看用户信息、睡眠状态、积分流水
- **商品管理** - 商品增删改查、上下架管理
- **积分管理** - 积分流水查询、手动调整
- **数据统计** - 用户活跃度、积分消耗趋势等

## 🛠️ 技术栈

### Android端
- **语言**: Kotlin
- **架构**: MVVM + Repository Pattern
- **数据库**: Room (本地) + Firestore (云端)
- **UI**: Material Design Components
- **异步处理**: Kotlin Coroutines
- **图片加载**: Glide
- **依赖注入**: 手动依赖注入

### Web管理后台
- **框架**: React 18 + TypeScript
- **UI库**: Ant Design
- **状态管理**: React Hooks
- **数据库**: Firebase Firestore
- **构建工具**: Create React App

### 后端服务
- **数据库**: Firebase Firestore
- **认证**: Firebase Authentication
- **云函数**: Firebase Functions
- **文件存储**: Firebase Storage

## 📋 系统要求

### Android端
- Android 8.0 (API level 26) 或更高版本
- 至少 2GB RAM
- 100MB 可用存储空间

### Web管理后台
- 现代浏览器 (Chrome 90+, Firefox 88+, Safari 14+)
- 网络连接

### 开发环境
- **Android开发**: Android Studio Arctic Fox 或更高版本
- **Web开发**: Node.js 16+ 和 npm/yarn
- **Java**: JDK 17
- **Gradle**: 8.12

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd sleep_app
```

### 2. Firebase配置
1. 在 [Firebase Console](https://console.firebase.google.com/) 创建新项目
2. 启用以下服务：
   - Authentication (邮箱/密码登录)
   - Firestore Database
   - Storage
   - Functions
3. 下载配置文件：
   - Android: `google-services.json` → `android_app/app/`
   - Web: Firebase配置 → `web_admin/src/services/firebase.ts`

### 3. Android应用开发

#### 环境准备
```bash
# 确保已安装 Android Studio 和 JDK 17
# 设置 ANDROID_HOME 环境变量
```

#### 构建和运行
```bash
cd android_app

# 清理构建缓存
./gradlew clean

# 构建Debug版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

#### 常见问题解决
- **JVM target错误**: 确保使用JDK 17，检查 `gradle.properties` 配置
- **依赖冲突**: 运行 `./gradlew clean` 清理缓存
- **签名错误**: 检查 `google-services.json` 文件是否正确放置

### 4. Web管理后台开发

#### 安装依赖
```bash
cd web_admin
npm install
```

#### 开发运行
```bash
# 启动开发服务器
npm start

# 构建生产版本
npm run build
```

#### 部署到Firebase Hosting
```bash
# 安装Firebase CLI
npm install -g firebase-tools

# 登录Firebase
firebase login

# 部署
firebase deploy --only hosting
```

## 📁 项目结构

```
sleep_app/
├── android_app/                 # Android应用
│   ├── app/
│   │   ├── src/main/java/com/example/sleepapp/
│   │   │   ├── data/           # 数据层
│   │   │   ├── ui/             # UI层
│   │   │   ├── service/        # 服务层
│   │   │   └── utils/          # 工具类
│   │   └── build.gradle        # 应用级构建配置
│   ├── build.gradle            # 项目级构建配置
│   └── gradle.properties       # Gradle属性配置
├── web_admin/                   # Web管理后台
│   ├── src/
│   │   ├── pages/              # 页面组件
│   │   ├── services/           # 服务层
│   │   └── App.tsx             # 主应用组件
│   ├── package.json            # 依赖配置
│   └── tsconfig.json           # TypeScript配置
├── cloud_functions/             # Firebase云函数
│   ├── index.js                # 云函数入口
│   └── package.json            # 依赖配置
├── docs/                        # 项目文档
├── firebase.json               # Firebase项目配置
└── README.md                   # 项目说明
```

## 🔧 配置说明

### Android配置文件

#### `android_app/gradle.properties`
```properties
# 项目级Gradle设置
android.useAndroidX=true
android.enableJetifier=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.parallel=true
org.gradle.caching=true
android.nonTransitiveRClass=true
android.nonFinalResIds=false
```

#### `android_app/local.properties`
```properties
# 本地SDK路径配置（不要提交到版本控制）
sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### Web配置文件

#### `web_admin/src/services/firebase.ts`
```typescript
import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';

const firebaseConfig = {
  // 从Firebase Console获取配置
  apiKey: "your-api-key",
  authDomain: "your-project.firebaseapp.com",
  projectId: "your-project-id",
  storageBucket: "your-project.appspot.com",
  messagingSenderId: "123456789",
  appId: "your-app-id"
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
```

## 📊 数据库结构

### Firestore集合结构

#### users 集合
```javascript
{
  uid: string,           // 用户唯一ID
  email: string,         // 邮箱
  points: number,        // 当前积分
  consecutiveDays: number, // 连续天数
  sleepStatus: string,   // 睡眠状态
  createdAt: timestamp,  // 注册时间
  lastLoginAt: timestamp // 最后登录时间
}
```

#### products 集合
```javascript
{
  id: string,           // 商品ID
  name: string,         // 商品名称
  description: string,  // 商品描述
  imageUrl: string,     // 商品图片URL
  pointsRequired: number, // 所需积分
  isActive: boolean,    // 是否上架
  createdAt: timestamp  // 创建时间
}
```

#### pointsHistory 集合
```javascript
{
  userId: string,       // 用户ID
  points: number,       // 积分变动（正数为获得，负数为消耗）
  type: string,         // 类型：'earn', 'spend', 'penalty'
  description: string,  // 描述
  createdAt: timestamp  // 时间
}
```

#### sleepRecords 集合
```javascript
{
  userId: string,       // 用户ID
  date: string,         // 日期 (YYYY-MM-DD)
  bedtime: timestamp,   // 睡觉时间
  wakeupTime: timestamp, // 起床时间
  violations: number,   // 违规次数
  pointsEarned: number, // 获得积分
  createdAt: timestamp  // 记录时间
}
```

## 🔐 安全配置

### Firestore安全规则
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 管理员检查函数
    function isAdmin() {
      return exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }

    // 管理员集合规则
    match /admins/{adminId} {
      allow read, write: if request.auth != null && request.auth.uid == adminId;
    }

    // 用户数据规则
    match /users/{userId} {
      allow read, write: if request.auth != null &&
        (request.auth.uid == userId || isAdmin());
    }

    // 商品数据规则
    match /products/{productId} {
      allow read: if request.auth != null;
      allow write: if isAdmin();
    }

    // 积分历史规则
    match /pointsHistory/{historyId} {
      allow read: if request.auth != null &&
        (resource.data.userId == request.auth.uid || isAdmin());
      allow write: if isAdmin();
    }

    // 睡眠记录规则
    match /sleepRecords/{recordId} {
      allow read, write: if request.auth != null &&
        (resource.data.userId == request.auth.uid || isAdmin());
    }
  }
}
```

## 🧪 测试

### Android单元测试
```bash
cd android_app
./gradlew test
```

### Android UI测试
```bash
cd android_app
./gradlew connectedAndroidTest
```

### Web测试
```bash
cd web_admin
npm test
```

## 📱 应用截图

### Android应用界面
- 登录注册界面
- 主页睡眠监控
- 积分商城界面
- 个人统计页面

### Web管理后台界面
- 用户管理页面
- 商品管理页面
- 积分流水页面
- 数据统计页面

## 🚀 部署

### Android应用发布
1. 生成签名密钥
2. 配置签名信息
3. 构建Release版本
4. 上传到应用商店

### Web后台部署
```bash
# 构建生产版本
cd web_admin
npm run build

# 部署到Firebase Hosting
firebase deploy --only hosting
```

### 云函数部署
```bash
cd cloud_functions
firebase deploy --only functions
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- **Android**: 遵循 [Kotlin编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- **Web**: 遵循 [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- **提交信息**: 使用 [Conventional Commits](https://www.conventionalcommits.org/)

## 📝 更新日志

### v1.0.0 (2024-12-XX)
- ✨ 初始版本发布
- 🎯 Android应用基础功能
- 🌐 Web管理后台
- 🔥 Firebase集成

## ❓ 常见问题

### Q: Android应用无法连接Firebase？
A: 检查 `google-services.json` 文件是否正确放置在 `android_app/app/` 目录下。

### Q: Web后台登录失败？
A: 确保用户UID已添加到Firestore的 `admins` 集合中。

### Q: 构建失败，提示JVM target错误？
A: 确保使用JDK 17，并检查Gradle配置文件中的JVM target设置。

### Q: 如何添加新的管理员？
A: 在Firestore的 `admins` 集合中添加新文档，文档ID为用户的UID。

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

- 项目维护者: UncalD
- 邮箱: dave_u77582@163.com
- 项目链接: [https://github.com/yourusername/sleep_app](https://github.com/yourusername/sleep_app)

## 🙏 致谢

- [Firebase](https://firebase.google.com/) - 后端服务
- [Android Jetpack](https://developer.android.com/jetpack) - Android开发框架
- [React](https://reactjs.org/) - Web前端框架
- [Ant Design](https://ant.design/) - UI组件库
- [Material Design](https://material.io/) - 设计规范
