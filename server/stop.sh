#!/bin/bash
# 鲜食记 (Food FreshKeeper) - 同步服务停止脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$SCRIPT_DIR/server.pid"
PORT="${PORT:-8099}"

STOPPED=0

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE" 2>/dev/null || true)
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
        echo "正在停止服务 (PID: $PID)..."
        kill "$PID" 2>/dev/null || true
        for i in {1..10}; do
            if ! kill -0 "$PID" 2>/dev/null; then
                break
            fi
            sleep 0.5
        done
        if kill -0 "$PID" 2>/dev/null; then
            echo "服务响应超时，强制终止 (PID: $PID)..."
            kill -9 "$PID" 2>/dev/null || true
        fi
        STOPPED=1
    fi
    rm -f "$PID_FILE"
fi

# 检查端口占用是否已被释放
PORT_PID=$(lsof -t -i :"$PORT" 2>/dev/null || fuser "$PORT"/tcp 2>/dev/null || true)
if [ -n "$PORT_PID" ]; then
    echo "正在释放端口 $PORT 占用的进程 (PID: $PORT_PID)..."
    kill -9 "$PORT_PID" 2>/dev/null || true
    STOPPED=1
fi

# 备用进程名清理
MATCH_PIDS=$(pgrep -f "python3.*server.py.*$PORT" 2>/dev/null || true)
if [ -n "$MATCH_PIDS" ]; then
    echo "清理匹配的服务进程: $MATCH_PIDS"
    kill -9 $MATCH_PIDS 2>/dev/null || true
    STOPPED=1
fi

if [ "$STOPPED" -eq 1 ]; then
    echo "鲜食记同步服务已成功停止。"
else
    echo "未发现正在运行的鲜食记同步服务进程。"
fi
