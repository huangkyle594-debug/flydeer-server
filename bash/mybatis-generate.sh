#!/usr/bin/env bash
# 运行 MyBatis Generator（覆盖 repository 下 entity/mapper/xml）
# 前提：MySQL 已启动且已执行 doc/sql 建表
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

PROPS="${ROOT}/flydeer-repository/src/main/resources/generatorJdbc.properties"
EXAMPLE="${PROPS}.example"

if [[ ! -f "$PROPS" ]]; then
  cp "$EXAMPLE" "$PROPS"
  echo "[mybatis-generate] 已创建 generatorJdbc.properties，请按需修改"
fi

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
export PATH="${JAVA_HOME}/bin:${PATH}"
if [[ -S "${HOME}/.colima/default/docker.sock" ]]; then
  export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
fi

./mvnw -pl flydeer-repository mybatis-generator:generate
echo "[mybatis-generate] 完成"
