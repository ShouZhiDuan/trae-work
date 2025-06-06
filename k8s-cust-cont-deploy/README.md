# Kubernetes双容器部署示例

这个项目包含一个Kubernetes Deployment定义，演示如何在同一个Pod中运行两个容器并通过emptyDir卷共享文件。

## 文件说明

- `deployment.yaml`: Kubernetes Deployment定义文件

## 部署架构

### 容器配置

#### 初始化容器
1. **wait-for-kms (busybox)**
   - 镜像: `busybox:1.35`
   - 功能: 等待KMS服务启动完成
   - 检查方式: TCP连接检查端口9999
   - 挂载点: `/tmp/shared`

#### 主容器
1. **kms (加解密服务)**
   - 镜像: `kms:latest`
   - 端口: 9999
   - 功能: 提供加解密服务
   - 挂载点: `/tmp/shared`
   - 健康检查: TCP端口检查
   - 启动顺序: 第一个启动

2. **cust-cont (自定义容器)**
   - 镜像: `cust-cont:latest`
   - 端口: 8080
   - 功能: 主业务应用，依赖KMS服务
   - 挂载点: `/tmp/shared`
   - 健康检查: HTTP健康检查接口
   - 启动顺序: 等待KMS服务就绪后启动

### 共享存储

- **卷类型**: emptyDir
- **卷名**: shared-data
- **用途**: 两个容器间共享文件和数据

## 部署步骤

1. 确保你有一个运行的Kubernetes集群
2. 应用部署配置:
   ```bash
   kubectl apply -f deployment.yaml
   ```

3. 检查部署状态:
   ```bash
   kubectl get deployments
   kubectl get pods
   ```

4. 查看Pod日志:
   ```bash
   # 查看初始化容器日志
   kubectl logs <pod-name> -c wait-for-kms
   
   # 查看KMS服务日志
   kubectl logs <pod-name> -c kms
   
   # 查看自定义容器日志
   kubectl logs <pod-name> -c cust-cont
   ```

5. 检查容器启动顺序:
   ```bash
   # 查看Pod事件，观察启动顺序
   kubectl describe pod <pod-name>
   
   # 查看容器状态
   kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[*].name}'
   ```

6. 进入容器查看共享文件:
   ```bash
   # 进入KMS容器
   kubectl exec -it <pod-name> -c kms -- /bin/sh
   ls -la /tmp/shared/
   
   # 进入自定义容器
   kubectl exec -it <pod-name> -c cust-cont -- /bin/sh
   ls -la /tmp/shared/
   ```

7. 验证服务依赖:
   ```bash
   # 检查KMS服务端口
   kubectl exec -it <pod-name> -c kms -- netstat -ln | grep 9999
   
   # 检查自定义容器健康状态
   kubectl exec -it <pod-name> -c cust-cont -- curl -f http://localhost:8080/health
   ```

## 验证容器启动依赖和共享文件

部署成功后，你可以验证以下功能:

### 启动顺序验证
1. **初始化容器**: `wait-for-kms` 首先运行，等待KMS服务可用
2. **KMS服务**: 在初始化容器完成后启动，提供加解密服务
3. **自定义容器**: 最后启动，依赖KMS服务已就绪

### 共享文件验证
在两个容器的 `/tmp/shared` 目录中可以进行文件共享:
- KMS容器可以创建加密密钥文件
- 自定义容器可以读取和使用这些密钥文件
- 两个容器通过emptyDir卷实现数据交换

### 健康检查验证
- **KMS服务**: TCP端口9999健康检查
- **自定义容器**: HTTP `/health` 接口健康检查

## 清理资源

```bash
kubectl delete -f deployment.yaml
```

## 注意事项

### 存储相关
- emptyDir卷的生命周期与Pod相同，Pod删除时数据会丢失
- 如需持久化存储，请考虑使用PersistentVolume
- 确保容器间的文件访问权限正确配置

### 容器启动依赖
- **初始化容器**: 必须成功完成后，主容器才会启动
- **健康检查**: 确保服务真正可用，而不仅仅是容器启动
- **网络检查**: 使用 `nc` 命令检查端口可用性，确保服务监听正常
- **超时设置**: 合理配置健康检查的超时和重试参数

### 镜像要求
- **KMS镜像**: 需要监听9999端口并提供加解密服务
- **自定义容器镜像**: 需要提供 `/health` 健康检查接口
- **启动脚本**: 自定义容器需要包含 `/app/start.sh` 启动脚本

### 故障排查
- 如果Pod一直处于Init状态，检查KMS服务是否正常启动
- 如果自定义容器启动失败，检查KMS服务端口是否可访问
- 使用 `kubectl describe pod` 查看详细的启动事件和错误信息