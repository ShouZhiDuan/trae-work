#!/bin/bash

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "Docker未安装，请先安装Docker"
    exit 1
fi

# 停止并删除已存在的容器
docker rm -f seaweedfs_master seaweedfs_volume 2>/dev/null

# 创建SeaweedFS主节点
docker run -d \
  --name seaweedfs_master \
  -p 9333:9333 \
  -p 19333:19333 \
  -v /Users/shouzhi/temp/seaweedfs/master:/data \
  chrislusf/seaweedfs master

# 创建SeaweedFS卷服务器
docker run -d \
  --name seaweedfs_volume \
  -p 8080:8080 \
  -p 18080:18080 \
  -v /Users/shouzhi/temp/seaweedfs/volume:/data \
  chrislusf/seaweedfs volume -mserver="seaweedfs_master:9333" -port=8080

echo "SeaweedFS已成功部署"
echo "Master管理界面: http://localhost:9333"
echo "Volume服务器: http://localhost:8080"