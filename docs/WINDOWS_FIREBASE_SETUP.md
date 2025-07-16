# Windows PowerShell Firebase配置指南

## 🔧 环境准备

### 1. 安装Node.js
```powershell
# 方法1: 从官网下载安装包
# 访问 https://nodejs.org/ 下载LTS版本

# 方法2: 使用Chocolatey (如果已安装)
choco install nodejs

# 方法3: 使用winget
winget install OpenJS.NodeJS
```

### 2. 验证安装
```powershell
node --version
npm --version
```

### 3. 安装Firebase CLI
```powershell
# 全局安装Firebase CLI
npm install -g firebase-tools

# 验证安装
firebase --version
```

## 🔥 Firebase项目配置

### 1. 登录Firebase
```powershell
# 登录Firebase账户
firebase login

# 如果浏览器无法打开，使用以下命令
firebase login --no-localhost
```

### 2. 初始化Firebase项目
```powershell
# 进入项目目录
cd D:\code\sleep_app

# 初始化Firebase项目
firebase init

# 选择需要的服务:
# - Firestore: Configure security rules and indexes files for Firestore
# - Functions: Configure a Cloud Functions directory and its files
# - Hosting: Configure files for Firebase Hosting and (optionally) GitHub Action deploys
# - Storage: Configure a security rules file for Cloud Storage
```

### 3. 配置Firestore
```powershell
# 选择现有项目或创建新项目
# 选择Firestore规则文件位置 (默认: firestore.rules)
# 选择Firestore索引文件位置 (默认: firestore.indexes.json)
```

### 4. 部署Firestore规则
```powershell
# 部署安全规则
firebase deploy --only firestore:rules

# 部署索引
firebase deploy --only firestore:indexes
```

## 📱 Android应用配置

### 1. 下载google-services.json
1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 选择您的项目
3. 点击"项目设置" → "常规"
4. 在"您的应用"部分，点击Android图标
5. 下载 `google-services.json` 文件

### 2. 放置配置文件
```powershell
# 将google-services.json复制到Android应用目录
Copy-Item "下载路径\google-services.json" "android_app\app\"
```

### 3. 验证配置
```powershell
# 进入Android项目目录
cd android_app

# 清理并构建项目
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

## 🌐 Web管理后台配置

### 1. 获取Web配置
1. 在Firebase Console中，点击"项目设置"
2. 在"常规"标签下，找到"您的应用"部分
3. 点击Web应用图标 `</>`
4. 复制配置对象

### 2. 配置Web应用
```powershell
# 进入Web管理后台目录
cd web_admin

# 创建Firebase配置文件
New-Item -Path "src\services\firebase.ts" -ItemType File -Force
```

### 3. 编辑Firebase配置
在 `web_admin\src\services\firebase.ts` 中添加：
```typescript
import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';
import { getAuth } from 'firebase/auth';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
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
export const storage = getStorage(app);
```

### 4. 安装依赖并启动
```powershell
# 安装依赖
npm install

# 启动开发服务器
npm start
```

## 🔐 管理员权限配置

### 1. 添加管理员
```powershell
# 使用Firebase CLI访问Firestore
firebase firestore:shell

# 在shell中执行 (替换为实际的用户UID)
db.collection('admins').doc('your-user-uid').set({
  email: 'admin@example.com',
  role: 'admin',
  createdAt: new Date()
})
```

### 2. 或者通过Firebase Console
1. 访问Firebase Console
2. 进入Firestore Database
3. 创建集合 `admins`
4. 添加文档，文档ID为用户UID
5. 添加字段：
   - `email`: 管理员邮箱
   - `role`: "admin"
   - `createdAt`: 当前时间戳

## 🚀 部署指南

### 1. 构建Web应用
```powershell
cd web_admin
npm run build
```

### 2. 部署到Firebase Hosting
```powershell
# 配置Hosting
firebase init hosting

# 选择build目录作为public目录
# 配置为单页应用 (SPA): Yes
# 不覆盖index.html: No

# 部署
firebase deploy --only hosting
```

### 3. 部署云函数 (如果有)
```powershell
cd cloud_functions
firebase deploy --only functions
```

## 🔍 常见问题解决

### 1. PowerShell执行策略问题
```powershell
# 如果遇到执行策略错误，运行：
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 2. npm权限问题
```powershell
# 以管理员身份运行PowerShell
# 或者配置npm全局目录
npm config set prefix "C:\Users\%USERNAME%\AppData\Roaming\npm"
```

### 3. Firebase登录问题
```powershell
# 清除Firebase缓存
firebase logout
firebase login --reauth
```

### 4. 网络问题
```powershell
# 如果网络受限，可以设置代理
npm config set proxy http://proxy-server:port
npm config set https-proxy http://proxy-server:port
```

## 📋 配置检查清单

- [ ] Node.js已安装 (v16+)
- [ ] Firebase CLI已安装
- [ ] Firebase项目已创建
- [ ] 已登录Firebase账户
- [ ] `google-services.json` 已放置在 `android_app/app/` 目录
- [ ] Web Firebase配置已添加到 `web_admin/src/services/firebase.ts`
- [ ] Firestore安全规则已部署
- [ ] 管理员UID已添加到 `admins` 集合
- [ ] Android应用可以成功构建
- [ ] Web应用可以正常启动
- [ ] 管理员可以正常登录Web后台

## 🆘 获取帮助

如果遇到问题，可以：
1. 查看Firebase文档: https://firebase.google.com/docs
2. 检查Firebase Console中的错误日志
3. 运行 `firebase --help` 查看命令帮助
4. 检查项目的 `docs/SECURITY.md` 文件
