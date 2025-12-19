#!/bin/bash

echo "🚀 开始部署AI多提供商聊天服务..."

# 检查Docker和Docker Compose
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 创建必要的目录
mkdir -p logs
mkdir -p nginx/ssl

# 生成自签名SSL证书（如果不存在）
if [ ! -f "nginx/ssl/cert.pem" ]; then
    echo "📜 生成自签名SSL证书..."
    openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout nginx/ssl/key.pem \
        -out nginx/ssl/cert.pem \
        -subj "/C=CN/ST=State/L=City/O=Organization/CN=localhost"
fi

# 构建并启动服务
echo "🔨 构建Docker镜像..."
docker-compose build

echo "🌟 启动所有服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 显示访问地址
echo ""
echo "✅ 部署完成！"
echo ""
echo "🌐 服务访问地址："
echo "  - 主应用: http://localhost"
echo "  - HTTPS: https://localhost"
echo "  - API文档: http://localhost/swagger-ui.html"
echo "  - 健康检查: http://localhost/health"
echo ""
echo "📊 监控服务："
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin123)"
echo ""
echo "🗄️  数据库服务："
echo "  - Redis: localhost:6379"
echo "  - Qdrant: http://localhost:6333"
echo ""
echo "📋 常用命令："
echo "  - 查看日志: docker-compose logs -f demo-ai"
echo "  - 重启服务: docker-compose restart demo-ai"
echo "  - 停止服务: docker-compose down"
echo "  - 更新服务: docker-compose pull && docker-compose up -d"
echo ""
echo "💡 提示：首次启动可能需要等待2-3分钟服务完全启动"