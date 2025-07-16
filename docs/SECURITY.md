# 安全配置指南

## 🔐 敏感文件管理

### 不要提交到版本控制的文件
- `serviceAccountKey.json` - Firebase服务账户密钥
- `google-services.json` - Android Firebase配置
- `.env` 文件 - 环境变量
- `local.properties` - 本地配置

### 安全存储建议
1. 使用环境变量存储敏感信息
2. 在生产环境中使用密钥管理服务
3. 定期轮换API密钥和服务账户密钥
4. 限制服务账户权限到最小必要范围

## 🛡️ Firebase安全规则

### Firestore规则最佳实践
1. **最小权限原则** - 只授予必要的权限
2. **身份验证检查** - 确保用户已登录
3. **数据所有权验证** - 用户只能访问自己的数据
4. **管理员权限控制** - 严格控制管理员访问
5. **输入验证** - 验证数据格式和内容

### 当前规则说明
```javascript
// 用户只能读写自己的数据
match /users/{userId} {
  allow read, write: if request.auth.uid == userId || isAdmin();
}

// 商品信息所有人可读，只有管理员可写
match /products/{productId} {
  allow read: if request.auth != null;
  allow write: if isAdmin();
}
```

## 🔑 认证安全

### Firebase Authentication配置
1. 启用邮箱/密码认证
2. 配置密码强度要求
3. 启用账户保护功能
4. 设置合理的会话超时时间

### 管理员账户管理
1. 在Firestore中创建`admins`集合
2. 添加管理员UID作为文档ID
3. 定期审查管理员列表
4. 使用强密码和双因素认证

## 🌐 网络安全

### HTTPS配置
- 所有API调用必须使用HTTPS
- 配置HSTS头部
- 使用安全的TLS版本

### CORS配置
```javascript
// 只允许特定域名访问
const corsOptions = {
  origin: ['https://yourdomain.com', 'https://admin.yourdomain.com'],
  credentials: true
};
```

## 📱 Android应用安全

### 代码混淆
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 网络安全配置
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">yourdomain.com</domain>
    </domain-config>
</network-security-config>
```

### 本地数据加密
- 使用Android Keystore存储敏感信息
- 对本地数据库进行加密
- 避免在日志中输出敏感信息

## 🔍 安全审计

### 定期检查项目
1. 扫描代码中的硬编码密钥
2. 检查依赖项的安全漏洞
3. 审查Firebase规则
4. 监控异常访问模式

### 工具推荐
- **代码扫描**: SonarQube, CodeQL
- **依赖检查**: npm audit, Snyk
- **规则测试**: Firebase Rules Unit Testing

## 🚨 安全事件响应

### 发现安全问题时的步骤
1. 立即撤销受影响的密钥
2. 更新Firebase规则
3. 通知相关用户
4. 记录事件详情
5. 实施预防措施

### 联系方式
如发现安全漏洞，请发送邮件至：security@yourdomain.com

## 📋 安全检查清单

### 部署前检查
- [ ] 所有敏感文件已从版本控制中移除
- [ ] Firebase规则已正确配置
- [ ] API密钥权限已限制到最小范围
- [ ] 代码已进行安全扫描
- [ ] 依赖项已更新到最新安全版本
- [ ] 网络通信已启用HTTPS
- [ ] 用户输入已进行验证和清理
- [ ] 错误信息不包含敏感信息

### 运行时监控
- [ ] 设置异常访问告警
- [ ] 监控API调用频率
- [ ] 定期检查用户权限
- [ ] 审查系统日志
- [ ] 监控数据库访问模式
