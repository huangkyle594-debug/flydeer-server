#!/usr/bin/env bash
# 一键启动：Compose (MySQL/Redis) + controller HTTP 服务
# 用法：
#   ./bash/run.sh              # 启动依赖并运行 controller (8080)
#   ./bash/run.sh --with-task  # 同时启动 task (8081)
#   ./bash/run.sh stop         # 停止应用进程（保留 Compose）
#   ./bash/run.sh down         # 停止应用并 docker compose down
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

CONTROLLER_MODULE="flydeer-struct-mind-controller"
TASK_MODULE="flydeer-struct-mind-task"
CONTROLLER_JAR="${ROOT}/${CONTROLLER_MODULE}/target/${CONTROLLER_MODULE}-0.0.1-SNAPSHOT.jar"
TASK_JAR="${ROOT}/${TASK_MODULE}/target/${TASK_MODULE}-0.0.1-SNAPSHOT.jar"
PID_DIR="${ROOT}/bash/.run"
CONTROLLER_PID_FILE="${PID_DIR}/controller.pid"
TASK_PID_FILE="${PID_DIR}/task.pid"
WITH_TASK=0

log() { printf '[run] %s\n' "$*"; }
die() { printf '[run] 错误：%s\n' "$*" >&2; exit 1; }

setup_java() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    :
  elif [[ -x /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java ]]; then
    export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
  fi
  [[ -n "${JAVA_HOME:-}" ]] || die "未找到 JDK，请安装 JDK 21 并设置 JAVA_HOME"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  local ver
  ver="$(java -version 2>&1 | head -n1)"
  log "Java: ${ver}"
}

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

wait_compose_healthy() {
  local i status
  log "等待 MySQL / Redis healthy..."
  for i in $(seq 1 60); do
    status="$(docker compose ps --format '{{.Service}} {{.Status}}' 2>/dev/null || true)"
    if printf '%s\n' "$status" | grep -q 'mysql.*(healthy)' \
      && printf '%s\n' "$status" | grep -q 'redis.*(healthy)'; then
      log "依赖已就绪"
      return 0
    fi
    sleep 2
  done
  docker compose ps || true
  die "依赖未在超时时间内就绪"
}

stop_pid_file() {
  local file="$1" name="$2"
  if [[ -f "$file" ]]; then
    local pid
    pid="$(cat "$file" 2>/dev/null || true)"
    if [[ -n "${pid}" ]] && kill -0 "$pid" 2>/dev/null; then
      log "停止 ${name} (pid=${pid})"
      kill "$pid" 2>/dev/null || true
      wait "$pid" 2>/dev/null || true
    fi
    rm -f "$file"
  fi
}

cmd_stop() {
  stop_pid_file "$CONTROLLER_PID_FILE" "controller"
  stop_pid_file "$TASK_PID_FILE" "task"
  # 兜底：按 jar 名杀残留
  pkill -f "${CONTROLLER_MODULE}-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
  pkill -f "${TASK_MODULE}-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
  log "应用已停止（Compose 仍在运行）"
}

cmd_down() {
  cmd_stop
  setup_docker
  log "docker compose down..."
  docker compose down
  log "完成"
}

build_and_run() {
  mkdir -p "$PID_DIR"
  cmd_stop

  log "构建 ${CONTROLLER_MODULE}..."
  ./mvnw -pl "${CONTROLLER_MODULE}" -am package -DskipTests -q
  [[ -f "$CONTROLLER_JAR" ]] || die "未找到 ${CONTROLLER_JAR}"

  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "构建 ${TASK_MODULE}..."
    ./mvnw -pl "${TASK_MODULE}" -am package -DskipTests -q
    [[ -f "$TASK_JAR" ]] || die "未找到 ${TASK_JAR}"
  fi

  log "启动 controller (8080)..."
  nohup java -jar "$CONTROLLER_JAR" \
    --spring.docker.compose.enabled=false \
    >"${PID_DIR}/controller.log" 2>&1 &
  echo $! >"$CONTROLLER_PID_FILE"

  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "启动 task (8081)..."
    nohup java -jar "$TASK_JAR" \
      --spring.docker.compose.enabled=false \
      >"${PID_DIR}/task.log" 2>&1 &
    echo $! >"$TASK_PID_FILE"
  fi

  local i
  for i in $(seq 1 60); do
    if curl -sf "http://localhost:8080/api/v1/ping" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done

  if ! curl -sf "http://localhost:8080/api/v1/ping" >/dev/null 2>&1; then
    log "controller 日志："
    tail -n 40 "${PID_DIR}/controller.log" || true
    die "controller 启动失败，见 bash/.run/controller.log"
  fi

  log "ping:  $(curl -sS http://localhost:8080/api/v1/ping)"
  log "health: $(curl -sS http://localhost:8080/actuator/health)"
  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "task health: $(curl -sS http://localhost:8081/actuator/health || echo '未就绪，见 bash/.run/task.log')"
  fi
  log "日志目录: bash/.run/"
  log "停止应用: ./bash/run.sh stop"
}

main() {
  local arg
  for arg in "$@"; do
    case "$arg" in
      stop) cmd_stop; exit 0 ;;
      down) cmd_down; exit 0 ;;
      --with-task) WITH_TASK=1 ;;
      -h|--help)
        sed -n '2,8p' "$0"
        exit 0
        ;;
      *) die "未知参数: ${arg}（支持 --with-task | stop | down）" ;;
    esac
  done

  setup_java
  setup_docker
  log "启动 Compose..."
  docker compose up -d
  wait_compose_healthy
  build_and_run
}

main "$@"
