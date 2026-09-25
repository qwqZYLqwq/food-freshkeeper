#!/bin/bash
# 鲜食记 (Food FreshKeeper) - 同步服务状态与探活脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$SCRIPT_DIR/server.pid"
PORT="${PORT:-8099}"

echo "=========================================="
echo " 鲜食记 (Food FreshKeeper) 服务状态检查"
echo "=========================================="

RUNNING=0
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE" 2>/dev/null || true)
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
        echo "进程状态: 正在运行 (PID: $PID)"
        RUNNING=1
    else
        echo "进程状态: 未运行 (存在遗留的 PID 文件: $PID)"
    fi
else
    MATCH_PID=$(pgrep -f "python3.*server.py.*$PORT" 2>/dev/null | head -n 1 || true)
    if [ -n "$MATCH_PID" ]; then
        echo "进程状态: 正在运行 (PID: $MATCH_PID, 无 PID 文件)"
        RUNNING=1
    else
        echo "进程状态: 未运行"
    fi
fi

if [ "$RUNNING" -eq 1 ]; then
    echo "正在测试健康检查端点 (http://127.0.0.1:$PORT/api/health)..."
    RESPONSE=""
    if command -v curl >/dev/null 2>&1; then
        RESPONSE=$(curl -s -m 3 "http://127.0.0.1:$PORT/api/health" 2>/dev/null || true)
    elif command -v python3 >/dev/null 2>&1; then
        RESPONSE=$(python3 -c "import urllib.request; print(urllib.request.urlopen('http://127.0.0.1:$PORT/api/health', timeout=3).read().decode())" 2>/dev/null || true)
    fi

    if [ -n "$RESPONSE" ]; then
        echo "健康状态: [正常 200 OK]"
        echo "响应内容: $RESPONSE"
    else
        echo "健康状态: [异常] 无法获取端点响应，请检查服务日志。"
    fi
fi

if [ -f "$SCRIPT_DIR/server.log" ]; then
    echo "------------------------------------------"
    echo "最新日志 (末尾 5 行):"
    tail -n 5 "$SCRIPT_DIR/server.log"
fi
echo "=========================================="
