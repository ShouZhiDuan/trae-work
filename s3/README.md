# S3 文件管理系统

这是一个基于Spring Boot和AWS Java SDK S3的文件上传下载系统，提供了完整的文件管理功能。

## 功能特性

- ✅ 文件上传到AWS S3
- ✅ 文件从AWS S3下载
- ✅ 文件删除
- ✅ 文件列表查看
- ✅ 文件存在性检查
- ✅ 友好的Web界面
- ✅ RESTful API接口
- ✅ 全局异常处理
- ✅ 文件大小限制（最大100MB）

## 技术栈

- **Spring Boot 3.2.0** - 主框架
- **AWS Java SDK S3 2.21.29** - AWS S3客户端
- **Maven** - 依赖管理
- **Java 17** - 编程语言

## 项目结构

```
s3/
├── pom.xml                                    # Maven配置文件
├── README.md                                  # 项目说明文档
└── src/
    └── main/
        ├── java/com/example/s3/
        │   ├── S3FileServiceApplication.java      # 主应用类
        │   ├── config/
        │   │   └── S3Config.java                 # S3配置类
        │   ├── controller/
        │   │   └── S3Controller.java             # REST控制器
        │   ├── service/
        │   │   └── S3Service.java                # S3服务类
        │   └── exception/
        │       └── GlobalExceptionHandler.java   # 全局异常处理
        └── resources/
            ├── application.yml                    # 应用配置文件
            └── static/
                └── index.html                     # Web界面
```

## 快速开始

### 1. 环境要求

- Java 17+
- Maven 3.6+
- AWS账户和S3存储桶

### 2. 配置AWS凭证

在运行应用之前，需要配置AWS凭证。有以下几种方式：

#### 方式一：环境变量（推荐）

```bash
export AWS_ACCESS_KEY_ID=your-access-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-access-key
export AWS_REGION=us-east-1
export AWS_S3_BUCKET_NAME=your-bucket-name
```

#### 方式二：修改application.yml

```yaml
aws:
  access:
    key:
      id: your-access-key-id
  secret:
    access:
      key: your-secret-access-key
  region: us-east-1
  s3:
    bucket:
      name: your-bucket-name
```

### 3. 运行应用

```bash
# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用启动后，访问 http://localhost:8080 即可使用Web界面。

## API接口文档

### 文件上传

```http
POST /api/s3/upload
Content-Type: multipart/form-data

Parameters:
- file: 要上传的文件（必需）
- keyName: S3中的文件键名（可选，不提供则自动生成）
```

**响应示例：**
```json
{
  "success": true,
  "message": "文件上传成功",
  "fileUrl": "https://your-bucket.s3.amazonaws.com/filename.jpg",
  "fileName": "filename.jpg",
  "fileSize": 1024
}
```

### 文件下载

```http
GET /api/s3/download/{keyName}
```

返回文件流，浏览器会自动下载文件。

### 文件删除

```http
DELETE /api/s3/delete/{keyName}
```

**响应示例：**
```json
{
  "success": true,
  "message": "文件删除成功"
}
```

### 获取文件列表

```http
GET /api/s3/files
```

**响应示例：**
```json
{
  "success": true,
  "message": "获取文件列表成功",
  "files": ["file1.jpg", "file2.pdf"],
  "count": 2
}
```

### 检查文件是否存在

```http
GET /api/s3/exists/{keyName}
```

**响应示例：**
```json
{
  "success": true,
  "exists": true,
  "keyName": "filename.jpg"
}
```

## 配置说明

### application.yml配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `aws.access.key.id` | AWS访问密钥ID | - |
| `aws.secret.access.key` | AWS秘密访问密钥 | - |
| `aws.region` | AWS区域 | us-east-1 |
| `aws.s3.bucket.name` | S3存储桶名称 | - |
| `spring.servlet.multipart.max-file-size` | 最大文件大小 | 100MB |
| `spring.servlet.multipart.max-request-size` | 最大请求大小 | 100MB |
| `server.port` | 服务端口 | 8080 |

## 安全注意事项

1. **不要在代码中硬编码AWS凭证**
2. **使用环境变量或AWS IAM角色管理凭证**
3. **确保S3存储桶有适当的访问权限**
4. **在生产环境中启用HTTPS**
5. **考虑添加文件类型和大小验证**

## 错误处理

应用包含全局异常处理器，会统一处理以下异常：

- S3服务异常
- 文件大小超限异常
- 运行时异常
- 通用异常

所有错误响应都遵循统一格式：

```json
{
  "success": false,
  "message": "错误描述"
}
```

## 开发和测试

### 本地开发

1. 确保已配置AWS凭证
2. 创建S3存储桶
3. 运行应用：`mvn spring-boot:run`
4. 访问 http://localhost:8080 测试功能

### 测试用例

可以使用以下工具测试API：

- **Web界面**：http://localhost:8080
- **Postman**：导入API接口进行测试
- **curl命令**：

```bash
# 上传文件
curl -X POST -F "file=@/path/to/file.jpg" http://localhost:8080/api/s3/upload

# 获取文件列表
curl http://localhost:8080/api/s3/files

# 下载文件
curl -O http://localhost:8080/api/s3/download/filename.jpg
```

## 部署

### 打包应用

```bash
mvn clean package
```

生成的JAR文件位于 `target/s3-file-service-1.0.0.jar`

### 运行JAR文件

```bash
java -jar target/s3-file-service-1.0.0.jar
```

### Docker部署（可选）

可以创建Dockerfile进行容器化部署：

```dockerfile
FROM openjdk:17-jre-slim
COPY target/s3-file-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 许可证

MIT License

## 贡献

欢迎提交Issue和Pull Request！