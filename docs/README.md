# 睡眠积分奖励App 项目结构说明（2024最新版）

## 目录结构

```
/docs/                # 所有文档、说明书、数据库结构、迁移指南、规则等
/scripts/             # 所有批量修复、结构同步、迁移等脚本
/logs/                # 所有日志、调试、自动生成的构建/缓存/IDE相关目录
/config/              # 所有配置文件
/android_app/         # 安卓源码
/web_admin/           # 后台管理系统源码
/cloud_functions/     # 云函数源码
```

## 目录用途说明
- **/docs/**
  - `README.md`              本说明文件
  - `notice.md`              问题修复与维护记录
  - `user_guide.txt`         新手快速上手指南
  - `firestore_schema.md`    Firestore数据库结构说明
  - `firestore_rules.txt`    Firestore安全规则
  - `project_overview.txt`   项目程序说明
  - `ideas.txt`              产品/功能想法与建议

- **/scripts/**
  - `fix_firestore_users_schema.js` Firestore用户表结构同步脚本
  - `run.js`                        其他批量/自动化脚本
  - `gradlew`/`gradlew.bat`         Android构建脚本

- **/logs/**
  - `firebase-debug.log`            Firebase相关日志
  - `pglite-debug.log`/`debug.log`  其他调试日志
  - `20240627_dev_notes.txt`        开发过程记录
  - `.gradle/` `.idea/` `build/`    构建/IDE缓存

- **/config/**
  - `serviceAccountKey.json`        Firebase服务密钥
  - `google-services.json`          Android Firebase配置
  - `firebase.json` `.firebaserc`   Firebase项目配置
  - `package.json`/`package-lock.json` Node依赖
  - `gradle.properties`/`build.gradle`/`settings.gradle`/`local.properties` Android构建配置
  - `.npmrc` `.gitignore`           其他配置

- **/android_app/**
  - 安卓端所有源码、资源、布局、入口

- **/web_admin/**
  - 后台管理系统所有源码、依赖、构建产物

- **/cloud_functions/**
  - 云函数相关源码、依赖

## 命名规范
- 所有文件/文件夹均采用小写+下划线/中划线分隔（如：`fix_firestore_users_schema.js`）
- 文档类统一放在 `/docs/`，脚本类统一放在 `/scripts/`，日志类统一放在 `/logs/`，配置类统一放在 `/config/`
- 代码主模块分别放在 `/android_app/`、`/web_admin/`、`/cloud_functions/`
- 临时/调试/历史文件请及时归档或删除，保持目录整洁

## 迁移与维护建议
- 所有新文档、脚本、配置请严格按上述目录和命名规范归类
- 迁移/重命名后请同步修正所有引用路径（脚本、代码、文档）
- 重要变更请在 `notice.md` 记录
- 如有特殊需求或建议，请在 `ideas.txt` 或 `notice.md` 说明

---

## 未来计划（Future Plans）
- MySQL 相关迁移（如 `mysql_schema.sql`、`mysql_migration_guide.md`、`fix_mysql_schema.js` 等）已归档至 `/docs/` 和 `/scripts/`，**暂不纳入当前开发计划**，未来如需支持MySQL数据库再行推进。
- 其他数据库适配、平台扩展、架构升级等内容也将在后续规划中逐步考虑。
- 后期可以分为会员和非会员，会员可以获取积分，进行商品兑换，非会员只有普通功能，不可以兑换商品。
---

> 本项目结构已全面规范化，便于团队协作、维护和扩展。如有疑问请查阅本文件或联系维护者。
