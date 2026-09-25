#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
鲜食记 (Food FreshKeeper) - 轻量云端同步服务端
Lightweight cloud synchronization server for Food FreshKeeper Android App.

特点:
1. 零外部依赖：纯 Python 3 标准库 (http.server, json, os, sys, time, argparse)
2. 原子化数据写入：临时文件 + os.replace，防止写入中断或数据损坏
3. 高并发支持：基于 ThreadingHTTPServer 处理并发请求
4. CORS 支持：支持跨域，方便调试与 Web 扩展
5. 适配 Linux, Android PRoot/Termux, Docker 等无 systemd 容器环境
"""

import argparse
import http.server
import json
import os
import sys
import tempfile
import time

SERVER_VERSION = "1.3.0"
DEFAULT_HOST = "0.0.0.0"
DEFAULT_PORT = 8099
DEFAULT_DATA_FILENAME = "foods_data.json"


class SyncRequestHandler(http.server.BaseHTTPRequestHandler):
    """处理鲜食记客户端请求的 HTTP 请求处理器"""

    # 屏蔽默认的 stderr 请求日志，使用更清晰的自定义日志格式
    def log_message(self, format, *args):
        now_str = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        sys.stdout.write(f"[{now_str}] {self.client_address[0]} - {format % args}\n")
        sys.stdout.flush()

    def _send_cors_headers(self):
        """发送 CORS 跨域请求头"""
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")

    def _send_json_response(self, status_code, data_dict):
        """发送 JSON 响应"""
        response_body = json.dumps(data_dict, ensure_ascii=False, indent=2).encode("utf-8")
        self.send_response(status_code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(response_body)))
        self._send_cors_headers()
        self.end_headers()
        self.wfile.write(response_body)

    def do_OPTIONS(self):
        """处理预检请求"""
        self.send_response(200)
        self._send_cors_headers()
        self.send_header("Content-Length", "0")
        self.end_headers()

    def do_GET(self):
        """处理 GET 请求"""
        parsed_path = self.path.split("?")[0].rstrip("/")
        if not parsed_path:
            parsed_path = "/"

        # 1. 健康检查端点
        if parsed_path == "/api/health":
            self._send_json_response(200, {
                "status": "ok",
                "service": "food-freshkeeper-sync",
                "version": SERVER_VERSION,
                "timestamp": int(time.time() * 1000)
            })
            return

        # 2. Ping 探活端点 (向后兼容)
        if parsed_path == "/api/ping":
            self._send_json_response(200, {
                "status": "ok",
                "message": "鲜食记同步服务器运行正常",
                "version": SERVER_VERSION
            })
            return

        # 3. 食材数据拉取端点 (支持 /api/foods 与 /api/sync)
        if parsed_path in ("/api/foods", "/api/sync"):
            data_file = self.server.data_file
            if os.path.exists(data_file):
                try:
                    with open(data_file, "r", encoding="utf-8") as f:
                        saved_data = json.load(f)

                    # 规范化输出结构
                    if isinstance(saved_data, dict):
                        foods = saved_data.get("foods", [])
                        updated_at = saved_data.get("updatedAt", saved_data.get("timestamp", 0))
                    elif isinstance(saved_data, list):
                        foods = saved_data
                        updated_at = int(os.path.getmtime(data_file) * 1000)
                    else:
                        foods = []
                        updated_at = 0

                    self._send_json_response(200, {
                        "status": "ok",
                        "foods": foods,
                        "count": len(foods),
                        "updatedAt": updated_at
                    })
                    return
                except Exception as e:
                    self._send_json_response(500, {
                        "status": "error",
                        "message": f"读取本地数据失败: {str(e)}"
                    })
                    return
            else:
                # 兼容查找同目录下的 foods_backup.json
                alt_file = os.path.join(os.path.dirname(data_file), "foods_backup.json")
                if os.path.exists(alt_file):
                    try:
                        with open(alt_file, "r", encoding="utf-8") as f:
                            saved_data = json.load(f)
                        foods = saved_data.get("foods", []) if isinstance(saved_data, dict) else (saved_data if isinstance(saved_data, list) else [])
                        self._send_json_response(200, {
                            "status": "ok",
                            "foods": foods,
                            "count": len(foods),
                            "updatedAt": int(os.path.getmtime(alt_file) * 1000)
                        })
                        return
                    except Exception:
                        pass

                # 数据文件尚不存在，返回空列表
                self._send_json_response(200, {
                    "status": "ok",
                    "foods": [],
                    "count": 0,
                    "updatedAt": 0
                })
                return

        # 4. 根路径欢迎信息
        if parsed_path == "/":
            self._send_json_response(200, {
                "status": "ok",
                "service": "food-freshkeeper-sync",
                "version": SERVER_VERSION,
                "endpoints": [
                    "/api/health",
                    "/api/ping",
                    "/api/foods"
                ],
                "message": "鲜食记 (Food FreshKeeper) 同步服务已就绪"
            })
            return

        # 未知路径返回 404
        self._send_json_response(404, {
            "status": "error",
            "message": f"Not Found: {self.path}"
        })

    def do_POST(self):
        """处理 POST 数据上传"""
        parsed_path = self.path.split("?")[0].rstrip("/")

        if parsed_path in ("/api/foods", "/api/sync"):
            content_length = int(self.headers.get("Content-Length", 0))
            if content_length <= 0:
                self._send_json_response(400, {
                    "status": "error",
                    "message": "请求体为空 (Empty request body)"
                })
                return

            try:
                body_bytes = self.rfile.read(content_length)
                raw_json = body_bytes.decode("utf-8")
                payload = json.loads(raw_json)
            except Exception as e:
                self._send_json_response(400, {
                    "status": "error",
                    "message": f"JSON 解析失败: {str(e)}"
                })
                return

            now_ms = int(time.time() * 1000)

            # 解析并规范化待保存的数据结构
            if isinstance(payload, dict):
                foods = payload.get("foods", [])
                updated_at = payload.get("timestamp", payload.get("updatedAt", now_ms))
            elif isinstance(payload, list):
                foods = payload
                updated_at = now_ms
            else:
                self._send_json_response(400, {
                    "status": "error",
                    "message": "请求格式错误，需要 JSON 对象或数组"
                })
                return

            save_data = {
                "status": "ok",
                "foods": foods,
                "count": len(foods),
                "updatedAt": updated_at
            }

            # 原子化写入文件：先写临时文件，然后使用 os.replace 替换，保证读写并发安全与断电完整性
            data_file = self.server.data_file
            data_dir = os.path.dirname(data_file)
            if not os.path.exists(data_dir):
                try:
                    os.makedirs(data_dir, exist_ok=True)
                except Exception as e:
                    self._send_json_response(500, {
                        "status": "error",
                        "message": f"创建数据目录失败: {str(e)}"
                    })
                    return

            temp_file_path = None
            try:
                with tempfile.NamedTemporaryFile("w", dir=data_dir, delete=False, encoding="utf-8") as tf:
                    temp_file_path = tf.name
                    json.dump(save_data, tf, ensure_ascii=False, indent=2)
                    tf.flush()
                    os.fsync(tf.fileno())

                os.replace(temp_file_path, data_file)
            except Exception as e:
                if temp_file_path and os.path.exists(temp_file_path):
                    try:
                        os.remove(temp_file_path)
                    except Exception:
                        pass
                self._send_json_response(500, {
                    "status": "error",
                    "message": f"保存数据失败: {str(e)}"
                })
                return

            # 返回成功响应
            self._send_json_response(200, {
                "status": "ok",
                "count": len(foods),
                "updatedAt": updated_at,
                "message": "食材数据备份成功"
            })
            return

        # 未知路径返回 404
        self._send_json_response(404, {
            "status": "error",
            "message": f"Not Found: {self.path}"
        })


class FoodSyncServer(http.server.ThreadingHTTPServer):
    """支持数据文件绑定的多线程 HTTP 服务器"""
    def __init__(self, server_address, RequestHandlerClass, data_file):
        super().__init__(server_address, RequestHandlerClass)
        self.data_file = os.path.abspath(data_file)


def parse_args():
    parser = argparse.ArgumentParser(description="鲜食记 (Food FreshKeeper) 轻量同步服务端")
    parser.add_argument(
        "--host",
        default=os.environ.get("HOST", DEFAULT_HOST),
        help=f"监听的主机地址 (默认: {DEFAULT_HOST})"
    )
    parser.add_argument(
        "--port",
        type=int,
        default=int(os.environ.get("PORT", DEFAULT_PORT)),
        help=f"监听的端口号 (默认: {DEFAULT_PORT})"
    )
    parser.add_argument(
        "--data-file",
        default=os.environ.get("DATA_FILE", None),
        help="数据持久化文件路径 (默认: 同目录下的 foods_data.json)"
    )
    return parser.parse_args()


def main():
    args = parse_args()
    script_dir = os.path.dirname(os.path.abspath(__file__))

    if args.data_file:
        data_file_path = os.path.abspath(args.data_file)
    else:
        data_file_path = os.path.join(script_dir, DEFAULT_DATA_FILENAME)

    server_address = (args.host, args.port)
    try:
        httpd = FoodSyncServer(server_address, SyncRequestHandler, data_file=data_file_path)
    except OSError as e:
        sys.stderr.write(f"启动失败: 无法绑定到 {args.host}:{args.port} - {str(e)}\n")
        sys.exit(1)

    print(f"==================================================")
    print(f" 鲜食记 (Food FreshKeeper) 同步服务端 v{SERVER_VERSION}")
    print(f" 运行地址: http://{args.host}:{args.port}")
    print(f" 数据文件: {data_file_path}")
    print(f" 健康检查: http://{args.host}:{args.port}/api/health")
    print(f" 数据接口: http://{args.host}:{args.port}/api/foods")
    print(f"==================================================")
    sys.stdout.flush()

    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n收到退出信号，服务正在安全关闭...")
    finally:
        httpd.server_close()
        print("服务已关闭。")


if __name__ == "__main__":
    main()
