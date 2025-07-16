# 修复npm安装Firebase CLI问题

## 🚨 问题描述
运行 `npm install -g firebase-tools` 时遇到权限错误和路径问题。

## 🔧 解决方案

### 方案1：以管理员身份运行（推荐）

1. **关闭当前PowerShell窗口**

2. **以管理员身份打开PowerShell**：
   - 按 `Win + X`
   - 选择 "Windows PowerShell (管理员)" 或 "终端 (管理员)"
   - 或者在开始菜单搜索 "PowerShell"，右键选择 "以管理员身份运行"

3. **清理之前的安装残留**：
```powershell
# 清理npm缓存
npm cache clean --force

# 删除全局firebase-tools（如果存在）
npm uninstall -g firebase-tools
```

4. **重新安装Firebase CLI**：
```powershell
npm install -g firebase-tools
```

### 方案2：使用yarn安装

如果npm继续有问题，可以尝试使用yarn：

```powershell
# 安装yarn
npm install -g yarn

# 使用yarn安装firebase-tools
yarn global add firebase-tools
```

### 方案3：使用独立安装包

```powershell
# 下载并安装Firebase CLI独立版本
# 访问 https://firebase.tools/ 下载Windows版本
# 或者使用以下命令
curl -sL https://firebase.tools | bash
```

### 方案4：修改npm配置

```powershell
# 设置npm全局安装目录到用户目录
npm config set prefix "C:\Users\%USERNAME%\npm-global"

# 添加到环境变量PATH
# 手动添加 C:\Users\你的用户名\npm-global 到系统PATH

# 然后重新安装
npm install -g firebase-tools
```

## 🔍 验证安装

安装完成后，验证Firebase CLI是否正常工作：

```powershell
# 检查版本
firebase --version

# 如果提示找不到命令，重启PowerShell或添加PATH
```

## 🚨 常见问题解决

### 问题1：权限不足
```
错误：EPERM: operation not permitted
解决：以管理员身份运行PowerShell
```

### 问题2：路径过长
```
错误：路径名过长
解决：使用方案4修改npm全局目录
```

### 问题3：网络问题
```powershell
# 设置npm镜像源
npm config set registry https://registry.npmmirror.com/

# 或者使用淘宝镜像
npm config set registry https://registry.npm.taobao.org/
```

### 问题4：中文乱码
```powershell
# 设置PowerShell编码
chcp 65001
```

## 📋 完整的安装步骤

1. **以管理员身份打开PowerShell**
2. **设置编码**：
```powershell
chcp 65001
```

3. **清理环境**：
```powershell
npm cache clean --force
npm uninstall -g firebase-tools
```

4. **安装Firebase CLI**：
```powershell
npm install -g firebase-tools
```

5. **验证安装**：
```powershell
firebase --version
```

6. **如果成功，继续配置**：
```powershell
firebase login
```

## 🎯 推荐执行顺序

1. 先尝试方案1（管理员权限）
2. 如果还有问题，尝试方案2（yarn）
3. 最后考虑方案3（独立安装包）

## 💡 预防措施

为避免将来遇到类似问题：

1. **始终以管理员身份安装全局npm包**
2. **定期清理npm缓存**：`npm cache clean --force`
3. **使用yarn作为npm的替代方案**
4. **考虑使用nvm管理Node.js版本**
