#!/usr/bin/env bash
# 一键启动：Docker Compose（MySQL + Redis + controller）
# 用法：
#   ./bash/run.sh              # 构建并启动全栈 (8080)
#   ./bash/run.sh reload       # 仅重建 app 容器以重载 .env（不重新编译）
#   ./bash/run.sh stop         # 停止 app 容器（保留 MySQL/Redis）
#   ./bash/run.sh down         # docker compose down（保留数据卷）
#   ./bash/run.sh down -v      # 同时删除数据卷
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

log() { printf '[run] %s\n' "$*"; }
die() { printf '[run] 错误：%s\n' "$*" >&2; exit 1; }

setup_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    die "未找到 docker，请先安装 Docker / Colima"
  fi
  if ! docker info >/dev/null 2>&1; then
    if command -v colima >/dev/null 2>&1; then
      log "Docker 未就绪，尝试启动 Colima..."
      colima start
    fi
  fi
  if [[ -S "${HOME}/.colima/default/docker.sock" ]]; then
    export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
  elif [[ -S "${HOME}/.colima/docker.sock" ]]; then
    export DOCKER_HOST="unix://${HOME}/.colima/docker.sock"
  fi
  docker info >/dev/null 2>&1 || die "Docker 不可用，请先启动 Docker Desktop 或 Colima"
  log "Docker: ok"
}

wait_app_healthy() {
  local i
  log "等待 app healthy..."
  for i in $(seq 1 90); do
    if curl -sf "http://127.0.0.1:8080/actuator/health" >/dev/null 2>&1; then
      log "health: $(curl -sS http://127.0.0.1:8080/actuator/health)"
      return 0
    fi
    sleep 2
  done
  docker compose ps || true
  docker compose logs app --tail=80 || true
  die "app 未在超时时间内就绪"
}

cmd_stop() {
  setup_docker
  log "停止 app 容器..."
  docker compose stop app
  log "MySQL / Redis 仍在运行；全停请用 ./bash/run.sh down"
}

cmd_down() {
  setup_docker
  log "docker compose down $*"
  docker compose down "$@"
  log "完成"
}

cmd_reload() {
  setup_docker
  if [[ ! -f "${ROOT}/.env" ]]; then
    log "未找到 .env（可选 cp .env.example .env）"
  fi
  log "重建 app 容器以重载 .env（不重新编译镜像）..."
  docker compose up -d --force-recreate --no-deps app
  wait_app_healthy
  log "已重载环境变量"
}

cmd_start() {
  setup_docker
  if [[ -f "${ROOT}/.env" ]]; then
    log "将通过 compose env_file 加载 .env（容器内 MYSQL/REDIS 仍用 compose 内网地址）"
  else
    log "未找到 .env，仅使用 compose 默认环境（可 cp .env.example .env）"
  fi
  log "构建并启动 Compose（mysql / redis / app）..."
  # --force-recreate：改 .env 后重新注入环境变量
  docker compose up -d --build --force-recreate app
  wait_app_healthy
  log "MySQL=flydeer@localhost:3306  Redis=localhost:6379  App=http://localhost:8080"
  log "停止 app: ./bash/run.sh stop"
  log "重载 .env: ./bash/run.sh reload"
  log "全部停止: ./bash/run.sh down"
}

main() {
  case "${1:-}" in
    reload|env) cmd_reload; exit 0 ;;
    stop) cmd_stop; exit 0 ;;
    down) shift; cmd_down "$@"; exit 0 ;;
    -h|--help)
      sed -n '2,9p' "$0"
      exit 0
      ;;
    ""|--with-task)
      # --with-task 暂未容器化 task；全栈仍启动 controller
      if [[ "${1:-}" == "--with-task" ]]; then
        log "提示: task 尚未纳入 Compose，当前仅启动 controller"
      fi
      cmd_start
      ;;
    *) die "未知参数: ${1}（支持 reload | stop | down | --with-task）" ;;
  esac
}

main "$@"
