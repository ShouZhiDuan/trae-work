# 高性能HTTP客户端工具

一个基于Spring Boot的高性能、稳定的HTTP调用工具，支持常见的GET、POST请求、文件传输、文件获取等功能。

## 功能特性

### 🚀 高性能特性
- **连接池管理**: 基于OkHttp的高性能连接池
- **异步支持**: 支持同步、异步和响应式编程模型
- **HTTP/2支持**: 自动支持HTTP/2协议
- **连接复用**: 智能连接复用，减少连接开销
- **超时控制**: 灵活的连接、读取、写入超时配置

### 📡 HTTP请求功能
- **多种HTTP方法**: GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS
- **请求头管理**: 灵活的请求头设置和管理
- **查询参数**: 自动URL编码的查询参数支持
- **请求体支持**: JSON、表单、原始数据等多种格式
- **响应处理**: 自动JSON解析和类型转换

### 📁 文件传输功能
- **文件上传**: 支持MultipartFile、本地文件、字节数组上传
- **文件下载**: 支持下载到本地文件或内存字节数组
- **大文件支持**: 流式处理，支持大文件传输
- **进度监控**: 可选的上传/下载进度监控
- **断点续传**: 支持HTTP Range请求

### 🔧 其他特性
- **配置灵活**: 基于Spring Boot配置，支持外部化配置
- **监控集成**: 集成Spring Boot Actuator监控
- **测试友好**: 完整的单元测试和集成测试
- **错误处理**: 完善的错误处理和重试机制

## 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- Spring Boot 3.2+

### 构建和运行

```bash
# 克隆项目
git clone <repository-url>
cd http-client

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run
```

应用启动后，访问 http://localhost:8080 即可使用。

### 健康检查

```bash
curl http://localhost:8080/api/http-client/health
```

## API接口文档

### 基础HTTP请求

#### 1. 通用HTTP请求

**POST** `/api/http-client/request`

```json
{
  "url": "https://api.example.com/data",
  "method": "GET",
  "headers": {
    "Authorization": "Bearer token",
    "Content-Type": "application/json"
  },
  "queryParams": {
    "page": 1,
    "size": 10
  },
  "body": {
    "key": "value"
  },
  "timeout": 30
}
```

#### 2. GET请求

**GET** `/api/http-client/get?url=https://api.example.com/data`

#### 3. POST请求

**POST** `/api/http-client/post?url=https://api.example.com/data`

```json
{
  "name": "test",
  "value": "123"
}
```

#### 4. PUT请求

**PUT** `/api/http-client/put?url=https://api.example.com/data/1`

#### 5. DELETE请求

**DELETE** `/api/http-client/delete?url=https://api.example.com/data/1`

### 异步请求

#### 异步HTTP请求

**POST** `/api/http-client/request/async`

#### 响应式HTTP请求

**POST** `/api/http-client/request/reactive`

### 文件传输

#### 1. 文件上传

**POST** `/api/http-client/upload`

```bash
curl -X POST \
  http://localhost:8080/api/http-client/upload \
  -F 'file=@/path/to/file.txt' \
  -F 'url=https://upload.example.com/files'
```

#### 2. 异步文件上传

**POST** `/api/http-client/upload/async`

#### 3. 文件下载

**GET** `/api/http-client/download?url=https://example.com/file.pdf&savePath=/tmp/downloaded.pdf`

#### 4. 异步文件下载

**GET** `/api/http-client/download/async`

#### 5. 下载到字节数组

**GET** `/api/http-client/download/bytes?url=https://example.com/image.jpg`

## ⚙️ 配置说明

### 应用配置

在 `application.yml` 中可以配置以下参数：

```yaml
http:
  client:
    # 基础连接配置
    connect-timeout: 30        # 连接超时时间（秒）
    read-timeout: 60          # 读取超时时间（秒）
    write-timeout: 60         # 写入超时时间（秒）
    max-idle-connections: 50  # 连接池最大空闲连接数
    keep-alive-duration: 5    # 连接保持活跃时间（分钟）
    max-requests: 200         # 最大请求数
    max-requests-per-host: 20 # 每个主机最大请求数
    
    # SSL/TLS安全配置
    skip-ssl-verification: false      # 是否跳过SSL证书验证（仅开发环境）
    ssl-protocol: TLS                 # SSL协议版本
    enable-hostname-verification: true # 是否启用主机名验证

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

### 环境配置

#### 开发环境配置

使用 `application-dev.yml` 进行开发环境配置：

```bash
# 启动开发环境
java -jar http-client.jar --spring.profiles.active=dev
```

开发环境特性：
- 跳过SSL证书验证（`skip-ssl-verification: true`）
- 关闭主机名验证（`enable-hostname-verification: false`）
- 详细的调试日志
- 较短的超时时间

#### 生产环境配置

使用 `application-prod.yml` 进行生产环境配置：

```bash
# 启动生产环境
java -jar http-client.jar --spring.profiles.active=prod
```

生产环境特性：
- 严格的SSL证书验证（`skip-ssl-verification: false`）
- 启用主机名验证（`enable-hostname-verification: true`）
- 使用TLSv1.3协议
- 优化的连接池配置
- 生产级别的日志配置

## 编程式使用

### 注入服务

```java
@Autowired
private HttpClientService httpClientService;

@Autowired
private FileTransferService fileTransferService;
```

### HTTP请求示例

```java
// GET请求
HttpResponse<String> response = httpClientService.get("https://api.example.com/data", String.class);

// POST请求
Map<String, Object> requestBody = Map.of("name", "test", "value", 123);
HttpResponse<Object> response = httpClientService.post("https://api.example.com/data", requestBody, Object.class);

// 自定义请求
HttpRequest request = new HttpRequest("https://api.example.com/data", HttpRequest.HttpMethod.GET)
    .addHeader("Authorization", "Bearer token")
    .addQueryParam("page", 1)
    .setTimeout(30);
    
HttpResponse<Object> response = httpClientService.execute(request, Object.class);

// 异步请求
CompletableFuture<HttpResponse<Object>> futureResponse = 
    httpClientService.executeAsync(request, Object.class);

// 响应式请求
Mono<HttpResponse<Object>> monoResponse = 
    httpClientService.executeReactive(request, Object.class);
```

### 文件传输示例

```java
// 文件上传
FileUploadRequest uploadRequest = new FileUploadRequest("https://upload.example.com/files", multipartFile)
    .addHeader("Authorization", "Bearer token")
    .addFormField("description", "Test file");
    
HttpResponse<String> uploadResponse = fileTransferService.uploadFile(uploadRequest);

// 文件下载
HttpResponse<String> downloadResponse = fileTransferService.downloadFile(
    "https://example.com/file.pdf", 
    "/tmp/downloaded.pdf"
);

// 下载到字节数组
HttpResponse<byte[]> bytesResponse = fileTransferService.downloadFileToBytes(
    "https://example.com/image.jpg"
);
```

## 响应格式

### 标准HTTP响应

```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "headers": {
    "Content-Type": "application/json",
    "Content-Length": "1234"
  },
  "body": {
    "data": "response data"
  },
  "rawBody": "{\"data\": \"response data\"}",
  "responseTime": 150,
  "timestamp": "2024-01-01T12:00:00",
  "success": true,
  "contentType": "application/json",
  "contentLength": 1234
}
```

### 错误响应

```json
{
  "statusCode": 404,
  "statusMessage": "Not Found",
  "success": false,
  "errorMessage": "Resource not found",
  "responseTime": 100,
  "timestamp": "2024-01-01T12:00:00"
}
```

### SSL/TLS安全配置详解

#### ⚠️ 安全警告

**跳过SSL验证功能仅用于开发和测试环境！**

在生产环境中跳过SSL验证会带来严重的安全风险：
- 中间人攻击
- 数据泄露
- 身份伪造

#### SSL配置选项说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `skip-ssl-verification` | `false` | 是否跳过SSL证书验证 |
| `ssl-protocol` | `TLS` | SSL/TLS协议版本 |
| `enable-hostname-verification` | `true` | 是否启用主机名验证 |

#### 使用场景

**开发环境**：
```yaml
http:
  client:
    skip-ssl-verification: true
    enable-hostname-verification: false
```

**测试环境**：
```yaml
http:
  client:
    skip-ssl-verification: true  # 仅用于自签名证书测试
    enable-hostname-verification: true
```

**生产环境**：
```yaml
http:
  client:
    skip-ssl-verification: false  # 必须为false
    ssl-protocol: TLSv1.3        # 使用最新协议
    enable-hostname-verification: true  # 必须为true
```

## 🔧 性能优化

### 连接池优化

- **连接复用**：使用OkHttp连接池，减少连接建立开销
- **HTTP/2支持**：自动支持HTTP/2协议，提升性能
- **连接保持**：配置合适的keep-alive时间
- **并发控制**：限制最大并发请求数，避免资源耗尽
- **SSL优化**：在生产环境使用有效证书，避免SSL握手开销

## 性能优化建议

### 1. 连接池配置
- 根据并发需求调整 `max-idle-connections`
- 设置合适的 `keep-alive-duration`
- 调整 `max-requests-per-host` 避免单点压力

### 2. 超时设置
- 根据网络环境调整超时时间
- 对于大文件传输，增加 `write-timeout`
- 使用异步请求处理高并发场景

### 3. 内存管理
- 大文件下载使用流式处理
- 及时释放响应资源
- 监控内存使用情况

## 监控和运维

### 健康检查端点
- `/actuator/health` - 应用健康状态
- `/actuator/metrics` - 应用指标
- `/actuator/info` - 应用信息

### 日志配置
应用使用SLF4J + Logback进行日志记录，支持以下日志级别：
- `com.example.httpclient` - 应用日志
- `okhttp3` - HTTP客户端日志
- `org.springframework.web.reactive.function.client` - WebClient日志

## 故障排查

### 常见问题

1. **连接超时**
   - 检查网络连接
   - 调整 `connect-timeout` 配置
   - 确认目标服务可达性

2. **读取超时**
   - 增加 `read-timeout` 配置
   - 检查目标服务响应时间
   - 考虑使用异步请求

3. **文件上传失败**
   - 检查文件大小限制
   - 确认 `max-file-size` 配置
   - 验证目标服务支持的文件格式

4. **内存溢出**
   - 检查大文件处理逻辑
   - 使用流式处理
   - 调整JVM内存参数

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 更新日志

### v1.0.0
- 初始版本发布
- 支持基础HTTP请求功能
- 支持文件上传下载
- 支持异步和响应式编程
- 完整的单元测试覆盖