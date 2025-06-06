# S3-DuckDB 查询工具

这是一个基于 Spring Boot 的应用程序，使用 DuckDB 来查询存储在 SeaweedFS（S3兼容存储）上的 CSV 文件。

## 功能特性

- 🚀 **高性能查询**: 使用 DuckDB 进行快速 CSV 数据分析
- 📁 **S3 兼容**: 支持 SeaweedFS 和其他 S3 兼容存储
- 🌐 **Web 界面**: 提供友好的 Web 查询界面
- 🔌 **REST API**: 完整的 RESTful API 接口
- 📊 **实时查询**: 动态加载 CSV 文件并执行 SQL 查询
- 🛠️ **灵活配置**: 支持自定义分隔符、表头等选项

## 技术栈

- **Spring Boot 2.7.14**: Web 框架
- **DuckDB 0.9.2**: 内存分析数据库
- **AWS SDK for Java**: S3 客户端（兼容 SeaweedFS）
- **Apache Commons CSV**: CSV 处理
- **Lombok**: 减少样板代码

## 快速开始

### 1. 环境要求

- Java 11 或更高版本
- Maven 3.6 或更高版本
- SeaweedFS 或其他 S3 兼容存储服务

### 2. 配置应用

编辑 `src/main/resources/application.yml` 文件，配置你的 SeaweedFS 连接信息：

```yaml
seaweedfs:
  endpoint: "http://your-seaweedfs-host:8333"  # SeaweedFS S3网关地址
  access-key: "your-access-key"
  secret-key: "your-secret-key"
  bucket: "csv-data"  # 存储CSV文件的bucket
  region: "us-east-1"
```

### 3. 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 4. 使用 Web 界面

打开浏览器访问 `http://localhost:8080`，你将看到一个友好的查询界面。

## API 接口

### 执行查询

```http
POST /api/query/execute
Content-Type: application/json

{
  "sql": "SELECT * FROM users WHERE age > 25 LIMIT 10",
  "csvFiles": ["data/users.csv"],
  "hasHeader": true,
  "delimiter": ",",
  "timeoutSeconds": 30
}
```

### 获取 CSV 文件列表

```http
GET /api/query/csv-files
```

### 获取已加载的表

```http
GET /api/query/tables
```

### 获取表的列信息

```http
GET /api/query/tables/{tableName}/columns
```

### 健康检查

```http
GET /api/query/health
```

## 使用示例

### 1. 准备 CSV 文件

首先，将你的 CSV 文件上传到 SeaweedFS 的指定 bucket 中。例如：

```
csv-data/
├── users.csv
├── orders.csv
└── products.csv
```

### 2. 执行查询

假设 `users.csv` 文件包含以下列：`id`, `name`, `age`, `email`

```sql
-- 查询所有用户
SELECT * FROM users LIMIT 10;

-- 按年龄分组统计
SELECT age, COUNT(*) as count 
FROM users 
GROUP BY age 
ORDER BY age;

-- 多表联查（需要先加载多个CSV文件）
SELECT u.name, COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.name;
```

### 3. 使用 curl 测试 API

```bash
# 获取CSV文件列表
curl -X GET http://localhost:8080/api/query/csv-files

# 执行查询
curl -X POST http://localhost:8080/api/query/execute \
  -H "Content-Type: application/json" \
  -d '{
    "sql": "SELECT * FROM users LIMIT 5",
    "csvFiles": ["users.csv"],
    "hasHeader": true,
    "delimiter": ","
  }'
```

## 配置说明

### DuckDB 配置

```yaml
duckdb:
  database-path: ":memory:"  # 使用内存数据库，也可以指定文件路径如 "/tmp/duckdb.db"
```

### SeaweedFS 配置

```yaml
seaweedfs:
  endpoint: "http://localhost:8333"  # S3网关地址
  access-key: "your-access-key"      # 访问密钥
  secret-key: "your-secret-key"      # 密钥
  bucket: "csv-data"                 # bucket名称
  region: "us-east-1"                # 区域（可选）
```

### 日志配置

```yaml
logging:
  level:
    com.example.s3duckdb: DEBUG  # 应用日志级别
    org.duckdb: INFO             # DuckDB日志级别
```

## 注意事项

1. **表名规则**: CSV 文件会被自动转换为表名，文件名中的特殊字符会被替换为下划线
2. **内存使用**: 默认使用内存数据库，大文件可能消耗较多内存
3. **文件缓存**: CSV 文件会被临时下载到本地，查询完成后自动清理
4. **并发查询**: 支持多个并发查询，但共享同一个 DuckDB 实例

## 故障排除

### 常见问题

1. **连接 SeaweedFS 失败**
   - 检查 endpoint 地址是否正确
   - 确认 access-key 和 secret-key 是否有效
   - 验证网络连接

2. **CSV 文件读取失败**
   - 确认文件存在于指定的 bucket 中
   - 检查文件格式是否正确
   - 验证分隔符设置

3. **SQL 查询错误**
   - 确认表名是否正确（基于文件名生成）
   - 检查 SQL 语法
   - 查看应用日志获取详细错误信息

### 查看日志

```bash
# 查看应用日志
tail -f logs/spring.log

# 或者在控制台查看
mvn spring-boot:run
```

## 开发和扩展

### 项目结构

```
src/main/java/com/example/s3duckdb/
├── S3DuckDbApplication.java          # 主应用类
├── config/
│   ├── DuckDbConfig.java             # DuckDB配置
│   └── SeaweedFsConfig.java          # SeaweedFS配置
├── controller/
│   └── QueryController.java         # REST控制器
├── model/
│   ├── ColumnInfo.java               # 列信息模型
│   ├── QueryRequest.java             # 查询请求模型
│   └── QueryResponse.java            # 查询响应模型
└── service/
    ├── DuckDbQueryService.java       # DuckDB查询服务
    └── S3Service.java                # S3文件服务
```

### 添加新功能

1. **支持更多文件格式**: 扩展 `DuckDbQueryService` 支持 Parquet、JSON 等格式
2. **查询缓存**: 添加查询结果缓存机制
3. **用户认证**: 集成 Spring Security 进行用户认证
4. **查询历史**: 保存和管理查询历史记录

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！