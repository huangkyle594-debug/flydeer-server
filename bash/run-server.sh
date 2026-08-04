#!/usr/bin/env bash
# 云服务器启动脚本（不依赖 Docker）
# 前提：机器上已安装 JDK 21，以及可连的 MySQL / Redis（本机或云托管均可）
#
# 用法：
#   # 开发：复制并填写 application-dev.yml（默认 profile=dev）
#   cp flydeer-controller/src/main/resources/application-dev.yml.example \
#      flydeer-controller/src/main/resources/application-dev.yml
#   ./bash/run-server.sh
#
#   # 生产：
#   cp flydeer-controller/src/main/resources/application-prod.yml.example \
#      flydeer-controller/src/main/resources/application-prod.yml
#   SPRING_PROFILES_ACTIVE=prod ./bash/run-server.sh
#
#   ./bash/run-server.sh              # 默认 profile=dev
#   ./bash/run-server.sh --skip-build # 使用已有 jar，不重新编译
#   ./bash/run-server.sh stop
#   ./bash/run-server.sh status
#   ./bash/run-server.sh restart
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

CONTROLLER_MODULE="flydeer-controller"
CONTROLLER_JAR="${ROOT}/${CONTROLLER_MODULE}/target/${CONTROLLER_MODULE}-0.0.1-SNAPSHOT.jar"
PID_DIR="${ROOT}/bash/.run"
CONTROLLER_PID_FILE="${PID_DIR}/controller.pid"
LOG_DIR="${PID_DIR}"

SKIP_BUILD=0
ACTION="start"
PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
CONTROLLER_PORT=8080
MYSQL_HOST=localhost
MYSQL_PORT=3306
REDIS_HOST=localhost
REDIS_PORT=6379

log() { printf '[run-server] %s\n' "$*"; }
die() { printf '[run-server] 错误：%s\n' "$*" >&2; exit 1; }

require_profile() {
  local ctrl_yml="${ROOT}/${CONTROLLER_MODULE}/src/main/resources/application-${PROFILE}.yml"
  local ctrl_example="${ctrl_yml}.example"
  if [[ "${PROFILE}" == "dev" || "${PROFILE}" == "docker" ]]; then
    log "profile=${PROFILE}"
    return 0
  fi
  if [[ -f "${ctrl_yml}" ]]; then
    log "profile=${PROFILE} (${ctrl_yml})"
    return 0
  fi
  if [[ -f "${ctrl_example}" ]]; then
    die "未找到 ${ctrl_yml}，请先: cp ${ctrl_example} ${ctrl_yml}"
  fi
  die "未找到 profile 配置: application-${PROFILE}.yml"
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
  pkill -f "${CONTROLLER_MODULE}-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
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
  if curl -sf "http://127.0.0.1:${CONTROLLER_PORT}/actuator/health" >/dev/null 2>&1; then
    log "health: $(curl -sS "http://127.0.0.1:${CONTROLLER_PORT}/actuator/health")"
  fi
  [[ "$ok" -eq 1 ]] || return 1
}

build_jars() {
  if [[ "$SKIP_BUILD" -eq 1 ]]; then
    log "跳过构建 (--skip-build)"
    [[ -f "$CONTROLLER_JAR" ]] || die "未找到 ${CONTROLLER_JAR}，请先构建或去掉 --skip-build"
    return 0
  fi
  [[ -x "${ROOT}/mvnw" ]] || die "未找到 mvnw"
  log "构建 ${CONTROLLER_MODULE}..."
  ./mvnw -pl "${CONTROLLER_MODULE}" -am package -DskipTests -q
  [[ -f "$CONTROLLER_JAR" ]] || die "构建失败：${CONTROLLER_JAR}"
}

start_apps() {
  mkdir -p "$PID_DIR"
  cmd_stop

  log "启动 controller (:${CONTROLLER_PORT}, profile=${PROFILE})..."
  nohup java -jar "$CONTROLLER_JAR" \
    --spring.profiles.active="${PROFILE}" \
    --spring.docker.compose.enabled=false \
    >"${LOG_DIR}/controller.log" 2>&1 &
  echo $! >"$CONTROLLER_PID_FILE"

  local i
  for i in $(seq 1 90); do
    if curl -sf "http://127.0.0.1:${CONTROLLER_PORT}/actuator/health" >/dev/null 2>&1; then
      break
    fi
    sleep 1
  done

  if ! curl -sf "http://127.0.0.1:${CONTROLLER_PORT}/actuator/health" >/dev/null 2>&1; then
    log "controller 启动失败，最近日志："
    tail -n 50 "${LOG_DIR}/controller.log" || true
    die "见 ${LOG_DIR}/controller.log"
  fi

  log "health: $(curl -sS "http://127.0.0.1:${CONTROLLER_PORT}/actuator/health")"
  log "日志: ${LOG_DIR}/"
}

cmd_start() {
  require_profile
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
      --skip-build) SKIP_BUILD=1 ;;
      -h|--help)
        sed -n '2,18p' "$0"
        exit 0
        ;;
      *) die "未知参数: ${arg}" ;;
    esac
  done

  case "$ACTION" in
    start) cmd_start ;;
    stop) cmd_stop ;;
    status) cmd_status ;;
    restart) require_profile; setup_java; cmd_stop; SKIP_BUILD=1; build_jars; start_apps ;;
  esac
}

main "$@"
