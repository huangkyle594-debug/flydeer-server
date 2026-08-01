#!/usr/bin/env bash
# 云服务器启动脚本（不依赖 Docker）
# 前提：机器上已安装 JDK 21，以及可连的 MySQL / Redis（本机或云托管均可）
#
# 用法：
#   cp .env.example .env   # 按服务器实际修改数据库/Redis
#   ./bash/run-server.sh              # 构建并启动 controller
#   ./bash/run-server.sh --with-task  # 同时启动 task
#   ./bash/run-server.sh --skip-build # 使用已有 jar，不重新编译
#   ./bash/run-server.sh stop
#   ./bash/run-server.sh status
#   ./bash/run-server.sh restart
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
LOG_DIR="${PID_DIR}"

WITH_TASK=0
SKIP_BUILD=0
ACTION="start"

log() { printf '[run-server] %s\n' "$*"; }
die() { printf '[run-server] 错误：%s\n' "$*" >&2; exit 1; }

load_env() {
  if [[ -f "${ROOT}/.env" ]]; then
    set -a
    # shellcheck disable=SC1091
    source "${ROOT}/.env"
    set +a
    log "已加载 .env"
  else
    log "未找到 .env，使用默认配置（可复制 .env.example）"
  fi
  export MYSQL_HOST="${MYSQL_HOST:-localhost}"
  export MYSQL_PORT="${MYSQL_PORT:-3306}"
  export MYSQL_DATABASE="${MYSQL_DATABASE:-flydeer}"
  export MYSQL_USER="${MYSQL_USER:-flydeer}"
  export MYSQL_PASSWORD="${MYSQL_PASSWORD:-flydeer}"
  export REDIS_HOST="${REDIS_HOST:-localhost}"
  export REDIS_PORT="${REDIS_PORT:-6379}"
  export SERVER_PORT="${SERVER_PORT:-8080}"
  export TASK_SERVER_PORT="${TASK_SERVER_PORT:-8081}"
}

setup_java() {
  local candidate=""
  if [[ -x /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home/bin/java ]]; then
    candidate="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
  elif [[ -x /usr/lib/jvm/java-21-openjdk/bin/java ]]; then
    candidate="/usr/lib/jvm/java-21-openjdk"
  elif [[ -x /usr/lib/jvm/java-21-openjdk-amd64/bin/java ]]; then
    candidate="/usr/lib/jvm/java-21-openjdk-amd64"
  elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
    candidate="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  elif [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    candidate="${JAVA_HOME}"
  fi
  [[ -n "${candidate}" && -x "${candidate}/bin/java" ]] || die "未找到 JDK 21，请安装并设置 JAVA_HOME"
  export JAVA_HOME="${candidate}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  local ver
  ver="$(java -version 2>&1 | head -n1)"
  log "Java: ${ver}"
  java -version 2>&1 | grep -q 'version "21' || die "需要 JDK 21，当前: ${ver}"
}

check_port() {
  local host="$1" port="$2" name="$3"
  if command -v nc >/dev/null 2>&1; then
    nc -z -w 3 "$host" "$port" >/dev/null 2>&1 || die "${name} 不可达: ${host}:${port}"
  elif command -v timeout >/dev/null 2>&1; then
    timeout 3 bash -c "echo >/dev/tcp/${host}/${port}" 2>/dev/null \
      || die "${name} 不可达: ${host}:${port}"
  else
    log "跳过 ${name} 端口检测（无 nc/timeout）"
    return 0
  fi
  log "${name}: ${host}:${port} ok"
}

stop_pid_file() {
  local file="$1" name="$2"
  if [[ -f "$file" ]]; then
    local pid
    pid="$(cat "$file" 2>/dev/null || true)"
    if [[ -n "${pid}" ]] && kill -0 "$pid" 2>/dev/null; then
      log "停止 ${name} (pid=${pid})"
      kill "$pid" 2>/dev/null || true
      for _ in $(seq 1 20); do
        kill -0 "$pid" 2>/dev/null || break
        sleep 0.5
      done
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$file"
  fi
}

cmd_stop() {
  stop_pid_file "$CONTROLLER_PID_FILE" "controller"
  stop_pid_file "$TASK_PID_FILE" "task"
  pkill -f "${CONTROLLER_MODULE}-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
  pkill -f "${TASK_MODULE}-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
  log "已停止"
}

cmd_status() {
  local ok=0
  if [[ -f "$CONTROLLER_PID_FILE" ]] && kill -0 "$(cat "$CONTROLLER_PID_FILE")" 2>/dev/null; then
    log "controller: running (pid=$(cat "$CONTROLLER_PID_FILE"))"
    ok=1
  else
    log "controller: stopped"
  fi
  if [[ -f "$TASK_PID_FILE" ]] && kill -0 "$(cat "$TASK_PID_FILE")" 2>/dev/null; then
    log "task: running (pid=$(cat "$TASK_PID_FILE"))"
    ok=1
  else
    log "task: stopped"
  fi
  if curl -sf "http://127.0.0.1:${SERVER_PORT:-8080}/actuator/health" >/dev/null 2>&1; then
    log "health: $(curl -sS "http://127.0.0.1:${SERVER_PORT:-8080}/actuator/health")"
  fi
  [[ "$ok" -eq 1 ]] || return 1
}

build_jars() {
  if [[ "$SKIP_BUILD" -eq 1 ]]; then
    log "跳过构建 (--skip-build)"
    [[ -f "$CONTROLLER_JAR" ]] || die "未找到 ${CONTROLLER_JAR}，请先构建或去掉 --skip-build"
    if [[ "$WITH_TASK" -eq 1 ]]; then
      [[ -f "$TASK_JAR" ]] || die "未找到 ${TASK_JAR}，请先构建或去掉 --skip-build"
    fi
    return 0
  fi
  [[ -x "${ROOT}/mvnw" ]] || die "未找到 mvnw"
  log "构建 ${CONTROLLER_MODULE}..."
  ./mvnw -pl "${CONTROLLER_MODULE}" -am package -DskipTests -q
  [[ -f "$CONTROLLER_JAR" ]] || die "构建失败：${CONTROLLER_JAR}"
  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "构建 ${TASK_MODULE}..."
    ./mvnw -pl "${TASK_MODULE}" -am package -DskipTests -q
    [[ -f "$TASK_JAR" ]] || die "构建失败：${TASK_JAR}"
  fi
}

start_apps() {
  mkdir -p "$PID_DIR"
  cmd_stop

  log "启动 controller (:${SERVER_PORT})..."
  nohup java -jar "$CONTROLLER_JAR" \
    --spring.docker.compose.enabled=false \
    >"${LOG_DIR}/controller.log" 2>&1 &
  echo $! >"$CONTROLLER_PID_FILE"

  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "启动 task (:${TASK_SERVER_PORT})..."
    nohup java -jar "$TASK_JAR" \
      --spring.docker.compose.enabled=false \
      >"${LOG_DIR}/task.log" 2>&1 &
    echo $! >"$TASK_PID_FILE"
  fi

  local i
  for i in $(seq 1 90); do
    if curl -sf "http://127.0.0.1:${SERVER_PORT}/actuator/health" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done

  if ! curl -sf "http://127.0.0.1:${SERVER_PORT}/actuator/health" >/dev/null 2>&1; then
    log "controller 启动失败，最近日志："
    tail -n 50 "${LOG_DIR}/controller.log" || true
    die "见 ${LOG_DIR}/controller.log"
  fi

  log "health: $(curl -sS "http://127.0.0.1:${SERVER_PORT}/actuator/health")"
  if [[ "$WITH_TASK" -eq 1 ]]; then
    log "task health: $(curl -sS "http://127.0.0.1:${TASK_SERVER_PORT}/actuator/health" || echo '未就绪，见 bash/.run/task.log')"
  fi
  log "日志: ${LOG_DIR}/"
}

cmd_start() {
  load_env
  setup_java
  check_port "$MYSQL_HOST" "$MYSQL_PORT" "MySQL"
  check_port "$REDIS_HOST" "$REDIS_PORT" "Redis"
  build_jars
  start_apps
}

main() {
  local arg
  for arg in "$@"; do
    case "$arg" in
      start) ACTION="start" ;;
      stop) ACTION="stop" ;;
      status) ACTION="status" ;;
      restart) ACTION="restart" ;;
      --with-task) WITH_TASK=1 ;;
      --skip-build) SKIP_BUILD=1 ;;
      -h|--help)
        sed -n '2,14p' "$0"
        exit 0
        ;;
      *) die "未知参数: ${arg}" ;;
    esac
  done

  case "$ACTION" in
    start) cmd_start ;;
    stop) load_env; cmd_stop ;;
    status) load_env; cmd_status ;;
    restart) load_env; setup_java; cmd_stop; SKIP_BUILD=1; build_jars; start_apps ;;
  esac
}

main "$@"
