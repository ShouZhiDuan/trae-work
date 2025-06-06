# AQ2.0 Post Task API

一个基于Spring Boot的POST接口服务，用于处理包含数据库配置、FTP配置和查询信息的复杂JSON请求。

## 功能特性

- ✅ 支持复杂JSON请求参数验证
- ✅ 数据库连接验证
- ✅ FTP连接验证
- ✅ 公安场景和医院场景查询处理
- ✅ 统一异常处理
- ✅ 完整的日志记录
- ✅ RESTful API设计

## 技术栈

- **Java 11**
- **Spring Boot 2.7.14**
- **Spring Web**
- **Spring Data JPA**
- **MySQL Connector**
- **Apache Commons Net** (FTP客户端)
- **Lombok**
- **Jackson** (JSON处理)
- **Maven**

## 项目结构

```
src/
├── main/
│   ├── java/com/example/aq2/
│   │   ├── Aq2PostTaskApplication.java     # 主应用类
│   │   ├── controller/
│   │   │   └── QueryController.java        # 查询控制器
│   │   ├── dto/                           # 数据传输对象
│   │   │   ├── ApiResponse.java           # 统一响应格式
│   │   │   ├── QueryRequest.java          # 主请求实体
│   │   │   ├── JdbcConf.java             # 数据库配置
│   │   │   ├── FtpConf.java              # FTP配置
│   │   │   ├── QueryInfo.java            # 查询信息
│   │   │   ├── GaInfo.java               # 公安场景信息
│   │   │   ├── YyInfo.java               # 医院场景信息
│   │   │   ├── GaTask.java               # 公安任务信息
│   │   │   └── RunListItem.java          # 运行列表项
│   │   ├── service/
│   │   │   └── QueryService.java         # 查询处理服务
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java # 全局异常处理
│   └── resources/
│       └── application.yml                # 应用配置
└── pom.xml                               # Maven配置
```

## API接口

### 1. 处理查询请求

**POST** `/api/v1/query`

处理包含数据库配置、FTP配置和查询信息的复杂JSON请求。

#### 请求示例

```json
{
  "jdbcConf": {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "pass": "password",
    "dbName": "test_db"
  },
  "ftpConf": {
    "host": "ftp.example.com",
    "port": 21,
    "user": "ftpuser",
    "pass": "ftppass"
  },
  "queryFilePath": "/data/storage/request/123/query.csv",
  "queryIdName": "idexName",
  "queryInfo": {
    "ga": {
      "task": {
        "id": 123,
        "name": "任务名称",
        "modelName": "模型名称",
        "user": "执行用户",
        "nodeName": "某某医院"
      },
      "runList": [
        {
          "tbName": "tb1",
          "idName": "tb1_id",
          "publicKey": "XXXXXX",
          "responseFilePath": "/data/storage/query/result.csv"
        }
      ]
    },
    "yy": {
      "runList": [
        {
          "tbName": "tb3",
          "idName": "tb3_id",
          "publicKey": "XXXXXX",
          "responseFilePath": "/data/storage/query/result/100/result.csv"
        }
      ]
    }
  }
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "queryFilePath": "/data/storage/request/123/query.csv",
    "queryIdName": "idexName",
    "processTime": 1703123456789,
    "status": "success",
    "gaProcessResult": {
      "taskInfo": {
        "taskId": 123,
        "taskName": "任务名称",
        "modelName": "模型名称",
        "user": "执行用户",
        "nodeName": "某某医院"
      },
      "runListCount": 1,
      "runList": [...]
    },
    "yyProcessResult": {
      "runListCount": 1,
      "runList": [...]
    }
  },
  "timestamp": 1703123456789
}
```

### 2. 健康检查

**GET** `/api/v1/health`

检查服务运行状态。

### 3. 获取API信息

**GET** `/api/v1/info`

获取API基本信息和可用端点。

## 快速开始

### 1. 环境要求

- Java 11+
- Maven 3.6+
- MySQL 5.7+ (可选，用于数据库连接验证)

### 2. 构建项目

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package
```

### 3. 运行应用

```bash
# 方式1：使用Maven运行
mvn spring-boot:run

# 方式2：运行JAR包
java -jar target/aq2-post-task-1.0.0.jar
```

### 4. 访问应用

应用启动后，可以通过以下地址访问：

- 健康检查：http://localhost:8080/api/v1/health
- API信息：http://localhost:8080/api/v1/info
- 查询接口：http://localhost:8080/api/v1/query (POST)

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8080  # 服务端口

logging:
  level:
    com.example.aq2: INFO  # 日志级别
  file:
    name: logs/aq2-post-task.log  # 日志文件路径

app:
  ftp:
    connection-timeout: 30000  # FTP连接超时时间
    data-timeout: 60000       # FTP数据传输超时时间
  database:
    connection-timeout: 5000   # 数据库连接超时时间
    validation-timeout: 5000   # 数据库验证超时时间
```

## 参数验证

项目使用Spring Boot Validation进行参数验证：

- `@NotNull`: 字段不能为空
- `@NotBlank`: 字符串不能为空或空白
- `@Min/@Max`: 数值范围验证
- `@Valid`: 嵌套对象验证

## 异常处理

全局异常处理器 `GlobalExceptionHandler` 统一处理：

- 参数验证异常
- 约束违反异常
- 运行时异常
- 其他未知异常

## 日志记录

- 使用SLF4J + Logback进行日志记录
- 支持控制台和文件输出
- 可配置日志级别和格式
- 自动日志文件轮转

## 开发建议

1. **安全性**：在生产环境中，建议对敏感信息（如数据库密码、FTP密码）进行加密处理
2. **性能优化**：可以考虑添加连接池来优化数据库和FTP连接
3. **监控**：建议添加应用监控和健康检查端点
4. **测试**：建议添加单元测试和集成测试
5. **文档**：可以集成Swagger来自动生成API文档

## 许可证

MIT License