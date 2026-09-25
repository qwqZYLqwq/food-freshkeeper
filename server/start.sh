#!/bin/bash
# 鲜食记 (Food FreshKeeper) - 同步服务启动脚本

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

PORT="${PORT:-8099}"
HOST="${HOST:-0.0.0.0}"
PID_FILE="$SCRIPT_DIR/server.pid"
LOG_FILE="$SCRIPT_DIR/server.log"

# 检查是否已有运行中的实例
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE" 2>/dev/null || true)
    if [ -n "$OLD_PID" ] && kill -0 "$OLD_PID" 2>/dev/null; then
        echo "服务已在运行中 (PID: $OLD_PID)。若需重启，请先执行 ./stop.sh"
        exit 0
    fi
fi

# 检查并清理遗留在该端口上的旧进程
OCCUPIED_PID=$(lsof -t -i :"$PORT" 2>/dev/null || fuser "$PORT"/tcp 2>/dev/null || true)
if [ -n "$OCCUPIED_PID" ]; then
    echo "端口 $PORT 已被进程 $OCCUPIED_PID 占用，尝试终止..."
    kill -9 "$OCCUPIED_PID" 2>/dev/null || true
    sleep 1
fi

echo "正在启动鲜食记同步服务端..."
echo "监听地址: $HOST:$PORT"
echo "日志文件: $LOG_FILE"

nohup python3 server.py --host "$HOST" --port "$PORT" > "$LOG_FILE" 2>&1 &
SERVER_PID=$!
echo "$SERVER_PID" > "$PID_FILE"

# 等待一秒以验证进程是否稳定存活
sleep 1

if kill -0 "$SERVER_PID" 2>/dev/null; then
    echo "鲜食记同步服务端已成功启动并在后台运行 (PID: $SERVER_PID)"
    echo "健康检查端点: http://127.0.0.1:$PORT/api/health"
    echo "可通过 ./status.sh 查看运行状态"
else
    echo "服务启动失败，日志输出如下 ($LOG_FILE):"
    tail -n 20 "$LOG_FILE"
    exit 1
fi
