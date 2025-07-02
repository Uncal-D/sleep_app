/**
 * 智能同步 MySQL 数据库结构脚本
 * 用法：node fixMySQLSchema.js [表名]
 * 功能：自动读取 MySQL_Schema.sql 文件，解析表结构，同步到 MySQL
 * 示例：node fixMySQLSchema.js users
 * 示例：node fixMySQLSchema.js (同步所有表)
 */

const mysql = require('mysql2/promise');
const fs = require('fs');
const path = require('path');

// MySQL 连接配置
const dbConfig = {
  host: process.env.MYSQL_HOST || 'localhost',
  port: process.env.MYSQL_PORT || 3306,
  user: process.env.MYSQL_USER || 'root',
  password: process.env.MYSQL_PASSWORD || '',
  database: process.env.MYSQL_DATABASE || 'sleep_app_db',
  charset: 'utf8mb4'
};

// 解析 MySQL_Schema.sql 文件中的表结构
function parseMySQLSchema() {
  const sqlContent = fs.readFileSync("MySQL_Schema.sql", "utf8");
  const tables = {};
  
  // 匹配 CREATE TABLE 语句
  const tableRegex = /CREATE TABLE (\w+)\s*\(([\s\S]*?)\)\s*ENGINE=/gs;
  let match;
  
  while ((match = tableRegex.exec(sqlContent)) !== null) {
    const tableName = match[1];
    const fieldsContent = match[2];
    
    // 解析字段
    const fields = {};
    const fieldLines = fieldsContent.split('\n').filter(line => line.trim());
    
    for (const line of fieldLines) {
      const fieldMatch = line.match(/^\s*(\w+)\s+([^,\s]+)(?:\s+DEFAULT\s+([^,\s]+))?(?:\s+COMMENT\s+'([^']+)')?/);
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
  if (typeLower.includes('varchar') || typeLower.includes('text')) return "''";
  if (typeLower.includes('int')) return "0";
  if (typeLower.includes('timestamp')) return "CURRENT_TIMESTAMP";
  if (typeLower.includes('json')) return "NULL";
  if (typeLower.includes('date')) return "NULL";
  if (typeLower.includes('boolean')) return "FALSE";
  return "NULL";
}

// 检查表是否存在
async function tableExists(connection, tableName) {
  const [rows] = await connection.execute(
    "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = ? AND table_name = ?",
    [dbConfig.database, tableName]
  );
  return rows[0].count > 0;
}

// 获取表的现有字段
async function getTableColumns(connection, tableName) {
  const [rows] = await connection.execute(
    "SHOW COLUMNS FROM " + tableName
  );
  return rows.map(row => row.Field);
}

// 同步单个表的结构
async function syncTableSchema(connection, tableName, fields) {
  console.log(`\n开始同步表: ${tableName}`);
  
  // 检查表是否存在
  const exists = await tableExists(connection, tableName);
  
  if (!exists) {
    console.log(`  - 表 ${tableName} 不存在，跳过同步`);
    return 0;
  }
  
  // 获取现有字段
  const existingColumns = await getTableColumns(connection, tableName);
  let count = 0;
  
  // 检查每个字段是否存在，不存在则添加
  for (const [fieldName, fieldInfo] of Object.entries(fields)) {
    if (!existingColumns.includes(fieldName)) {
      try {
        const alterSql = `ALTER TABLE ${tableName} ADD COLUMN ${fieldName} ${fieldInfo.type} DEFAULT ${fieldInfo.defaultValue}`;
        await connection.execute(alterSql);
        count++;
        console.log(`  - 添加字段 ${fieldName}: ${fieldInfo.type}`);
      } catch (error) {
        console.error(`  - 添加字段 ${fieldName} 失败:`, error.message);
      }
    }
  }
  
  if (count === 0) {
    console.log(`  - 表 ${tableName} 结构已是最新，无需更新`);
  } else {
    console.log(`  - 表 ${tableName} 同步完成，添加了 ${count} 个字段`);
  }
  
  return count;
}

// 创建数据库（如果不存在）
async function createDatabaseIfNotExists(connection) {
  try {
    await connection.execute(`CREATE DATABASE IF NOT EXISTS ${dbConfig.database} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`);
    console.log(`数据库 ${dbConfig.database} 已准备就绪`);
  } catch (error) {
    console.error('创建数据库失败:', error.message);
    throw error;
  }
}

// 主函数
async function fixMySQLSchema() {
  let connection;
  
  try {
    // 检查 MySQL_Schema.sql 文件是否存在
    if (!fs.existsSync("MySQL_Schema.sql")) {
      console.error("错误：找不到 MySQL_Schema.sql 文件");
      process.exit(1);
    }
    
    // 解析表结构
    console.log("正在解析 MySQL_Schema.sql 文件...");
    const tables = parseMySQLSchema();
    
    if (Object.keys(tables).length === 0) {
      console.error("错误：MySQL_Schema.sql 文件中没有找到有效的表结构");
      process.exit(1);
    }
    
    console.log(`找到 ${Object.keys(tables).length} 个表: ${Object.keys(tables).join(', ')}`);
    
    // 连接数据库
    console.log("正在连接 MySQL 数据库...");
    connection = await mysql.createConnection(dbConfig);
    
    // 创建数据库（如果不存在）
    await createDatabaseIfNotExists(connection);
    
    // 获取命令行参数
    const targetTable = process.argv[2];
    
    if (targetTable) {
      // 同步指定表
      if (!tables[targetTable]) {
        console.error(`错误：表 ${targetTable} 在 MySQL_Schema.sql 中不存在`);
        console.log(`可用的表: ${Object.keys(tables).join(', ')}`);
        process.exit(1);
      }
      
      await syncTableSchema(connection, targetTable, tables[targetTable]);
    } else {
      // 同步所有表
      let totalCount = 0;
      for (const [tableName, fields] of Object.entries(tables)) {
        const count = await syncTableSchema(connection, tableName, fields);
        totalCount += count;
      }
      console.log(`\n所有表同步完成，总共添加了 ${totalCount} 个字段`);
    }
    
    console.log("\n✅ MySQL 数据库结构同步成功！");
    
  } catch (error) {
    console.error("❌ 同步失败：", error.message);
    if (error.code === 'ECONNREFUSED') {
      console.error("请检查 MySQL 服务是否启动，以及连接配置是否正确");
    }
    process.exit(1);
  } finally {
    if (connection) {
      await connection.end();
    }
  }
}

// 运行脚本
fixMySQLSchema(); 