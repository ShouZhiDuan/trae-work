#!/bin/bash

# S3-DuckDB 查询工具启动脚本

echo "🚀 启动 S3-DuckDB 查询工具..."

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请确保已安装Java 11或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ 错误: Java版本过低，需要Java 11或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven，请确保已安装Maven"
    exit 1
fi

# 检查配置文件
if [ ! -f "src/main/resources/application.yml" ]; then
    echo "❌ 错误: 未找到配置文件 application.yml"
    exit 1
fi

echo "✅ 环境检查通过"

# 编译项目
echo "📦 编译项目..."
if ! mvn clean compile -q; then
    echo "❌ 编译失败"
    exit 1
fi

echo "✅ 编译完成"

# 启动应用
echo "🌟 启动应用..."
echo "📱 Web界面: http://localhost:8080"
echo "🔌 API文档: http://localhost:8080/api/query/health"
echo "⏹️  按 Ctrl+C 停止应用"
echo ""

mvn spring-boot:run