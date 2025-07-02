/**
 * 智能同步 Firebase Firestore 数据库结构脚本
 * 用法：node fixUsersSchema.js [表名]
 * 功能：自动读取 firestore_schema.md 文件，解析表结构，同步到 Firestore
 * 示例：node fixUsersSchema.js users
 * 示例：node fixUsersSchema.js (同步所有表)
 */

const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");
const serviceAccount = require("../config/serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// 解析 firestore_schema.md 文件中的表结构
function parseSQLFile() {
  const sqlContent = fs.readFileSync("../docs/firestore_schema.md", "utf8");
  const tables = {};
  
  // 匹配 CREATE TABLE 语句
  const tableRegex = /CREATE TABLE (\w+)\s*\(([\s\S]*?)\);/g;
  let match;
  
  while ((match = tableRegex.exec(sqlContent)) !== null) {
    const tableName = match[1];
    const fieldsContent = match[2];
    
    // 解析字段
    const fields = {};
    const fieldLines = fieldsContent.split('\n').filter(line => line.trim());
    
    for (const line of fieldLines) {
      const fieldMatch = line.match(/(\w+)\s+([^,\s]+)(?:\s+DEFAULT\s+([^,\s]+))?(?:\s+COMMENT\s+'([^']+)')?/);
      if (fieldMatch) {
        const [, fieldName, fieldType, defaultValue, comment] = fieldMatch;
        fields[fieldName] = {
          type: fieldType,
          defaultValue: defaultValue || getDefaultValue(fieldType),
          comment: comment || ''
        };
      }
    }
    
    tables[tableName] = fields;
  }
  
  return tables;
}

// 根据字段类型获取默认值
function getDefaultValue(type) {
  const typeLower = type.toLowerCase();
  if (typeLower.includes('varchar') || typeLower.includes('text')) return "";
  if (typeLower.includes('int')) return 0;
  if (typeLower.includes('timestamp')) return null;
  if (typeLower.includes('json')) return {};
  if (typeLower.includes('date')) return null;
  return null;
}

// 同步单个表的结构
async function syncTableSchema(tableName, fields) {
  console.log(`\n开始同步表: ${tableName}`);
  
  const collectionRef = db.collection(tableName);
  const snapshot = await collectionRef.get();
  let count = 0;
  
  for (const doc of snapshot.docs) {
    const data = doc.data();
    const updateData = {};
    
    // 检查每个字段是否存在，不存在则添加默认值
    for (const [fieldName, fieldInfo] of Object.entries(fields)) {
      if (data[fieldName] === undefined) {
        let defaultValue = fieldInfo.defaultValue;
        
        // 特殊处理时间戳字段
        if (fieldInfo.type.toLowerCase().includes('timestamp') && defaultValue === null) {
          defaultValue = admin.firestore.FieldValue.serverTimestamp();
        }
        
        updateData[fieldName] = defaultValue;
      }
    }
    
    if (Object.keys(updateData).length > 0) {
      await doc.ref.update(updateData);
      count++;
      console.log(`  - 更新文档 ${doc.id}: 添加了 ${Object.keys(updateData).length} 个字段`);
    }
  }
  
  console.log(`表 ${tableName} 同步完成，更新了 ${count} 条记录`);
  return count;
}

// 主函数
async function fixSchema() {
  try {
    // 检查 SQL.md 文件是否存在
    if (!fs.existsSync("../docs/firestore_schema.md")) {
      console.error("错误：找不到 SQL.md 文件");
      process.exit(1);
    }
    
    // 解析表结构
    console.log("正在解析 firestore_schema.md 文件...");
    const tables = parseSQLFile();
    
    if (Object.keys(tables).length === 0) {
      console.error("firestore_schema.md 文件中没有找到有效的表结构");
      process.exit(1);
    }
    
    console.log(`找到 ${Object.keys(tables).length} 个表: ${Object.keys(tables).join(', ')}`);
    
    // 获取命令行参数
    const targetTable = process.argv[2];
    
    if (targetTable) {
      // 同步指定表
      if (!tables[targetTable]) {
        console.error(`错误：表 ${targetTable} 在 firestore_schema.md 中不存在`);
        console.log(`可用的表: ${Object.keys(tables).join(', ')}`);
        process.exit(1);
      }
      
      await syncTableSchema(targetTable, tables[targetTable]);
    } else {
      // 同步所有表
      let totalCount = 0;
      for (const [tableName, fields] of Object.entries(tables)) {
        const count = await syncTableSchema(tableName, fields);
        totalCount += count;
      }
      console.log(`\n所有表同步完成，总共更新了 ${totalCount} 条记录`);
    }
    
    console.log("\n✅ 数据库结构同步成功！");
    process.exit(0);
    
  } catch (error) {
    console.error("❌ 同步失败：", error);
    process.exit(1);
  }
}

// 运行脚本
fixSchema();

