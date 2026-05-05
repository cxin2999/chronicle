#!/usr/bin/env bash
# Chronicle 快速部署脚本
# 用法: ./deploy.sh
# 前提: 本机已配置 SSH 密钥登录，ubuntu 用户在服务器上具有 sudo NOPASSWD 权限

set -euo pipefail

# ============================================================
# 可配置项 — 根据实际情况修改以下变量
# ============================================================
SERVER_IP="106.55.224.189"
SSH_USER="ubuntu"
SSH_PORT="22"
FRONTEND_DEPLOY_DIR="/opt/1panel/www/sites/chronicle/index"
BACKEND_DEPLOY_DIR="/project/chronicle-backend"
LOCAL_FRONTEND_DIST="chronicle-frontend/dist"
JAR_NAME="chronicle-0.0.1-SNAPSHOT.jar"
LOCAL_JAR="target/${JAR_NAME}"
BACKUP_KEEP=5
# ============================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO ][$(date '+%Y-%m-%d %H:%M:%S')]${NC} $*"; }
log_warn()  { echo -e "${YELLOW}[WARN ][$(date '+%Y-%m-%d %H:%M:%S')]${NC} $*"; }
log_error() { echo -e "${RED}[ERROR][$(date '+%Y-%m-%d %H:%M:%S')]${NC} $*" >&2; }

trap 'log_error "脚本在第 ${LINENO} 行发生错误，已终止执行。"' ERR

# ============================================================
# 检查本地依赖
# ============================================================
check_deps() {
  log_info "检查本地依赖..."
  for cmd in ssh scp; do
    if ! command -v "$cmd" &>/dev/null; then
      log_error "本机缺少命令: $cmd，请安装后重试。"
      exit 1
    fi
  done
  log_info "本地依赖检查通过（ssh / scp）"
}

# ============================================================
# 前端部署
# ============================================================
deploy_frontend() {
  log_info "====== 开始部署前端 ======"

  # 1. 校验本地 dist 目录
  if [[ ! -d "${LOCAL_FRONTEND_DIST}" ]]; then
    log_error "前端 dist 目录不存在: ${LOCAL_FRONTEND_DIST}"
    log_error "请先在 chronicle-frontend/ 目录执行 npm run build 后再部署。"
    exit 1
  fi
  log_info "本地 dist 目录校验通过: ${LOCAL_FRONTEND_DIST}"

  TIMESTAMP=$(date '+%Y%m%d%H%M%S')
  BAK_DIR="bak_${TIMESTAMP}"
  FRONT_KEEP_PLUS1=$((BACKUP_KEEP + 1))

  # 2. 远端创建备份目录，将当前非备份文件移入备份目录
  log_info "远端创建备份目录: ${FRONTEND_DEPLOY_DIR}/${BAK_DIR}"
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
sudo mkdir -p "${FRONTEND_DEPLOY_DIR}/${BAK_DIR}"
cd "${FRONTEND_DEPLOY_DIR}"
shopt -s nullglob
files=()
for item in *; do
  [[ "\$item" == bak_* ]] && continue
  files+=("\$item")
done
if [[ \${#files[@]} -gt 0 ]]; then
  sudo mv "\${files[@]}" "${BAK_DIR}/"
  echo "已将 \${#files[@]} 个文件/目录备份到 ${BAK_DIR}/"
else
  echo "当前部署目录为空，无需备份文件"
fi
EOF

  # 3. 清理旧备份，保留最近 BACKUP_KEEP 个
  log_info "远端清理旧备份，最多保留 ${BACKUP_KEEP} 个..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
cd "${FRONTEND_DEPLOY_DIR}"
to_del=\$(ls -dt bak_* 2>/dev/null | tail -n +${FRONT_KEEP_PLUS1})
if [[ -n "\$to_del" ]]; then
  echo "\$to_del" | xargs -r sudo rm -rf
  echo "已清理旧备份目录: \$to_del"
else
  echo "备份数量未超出 ${BACKUP_KEEP} 个，无需清理"
fi
EOF

  # 4. 上传前端文件到远端临时目录
  log_info "上传前端文件到远端临时目录 /tmp/chronicle_front_tmp/ ..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" \
    "rm -rf /tmp/chronicle_front_tmp && mkdir -p /tmp/chronicle_front_tmp"
  scp -P "${SSH_PORT}" -r "${LOCAL_FRONTEND_DIST}/." \
    "${SSH_USER}@${SERVER_IP}:/tmp/chronicle_front_tmp/"
  log_info "前端文件上传完成"

  # 5. 将临时目录内容复制到部署目录并设置权限
  log_info "远端部署文件并设置权限（0755 root:root）..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
sudo cp -r /tmp/chronicle_front_tmp/. "${FRONTEND_DEPLOY_DIR}/"
sudo chown -R root:root "${FRONTEND_DEPLOY_DIR}"
sudo chmod -R 0755 "${FRONTEND_DEPLOY_DIR}"
sudo rm -rf /tmp/chronicle_front_tmp
echo "前端文件权限设置完成"
EOF

  log_info "====== 前端部署完成 ======"
}

# ============================================================
# 后端部署
# ============================================================
deploy_backend() {
  log_info "====== 开始部署后端 ======"

  # 1. 校验本地 JAR
  if [[ ! -f "${LOCAL_JAR}" ]]; then
    log_error "本地 JAR 不存在: ${LOCAL_JAR}"
    log_error "请先在工程根目录执行 mvn package -DskipTests 后再部署。"
    exit 1
  fi
  log_info "本地 JAR 校验通过: ${LOCAL_JAR}"

  # 2. 【先上传】新 JAR 到远端临时目录（服务仍在运行，降低停机时间）
  log_info "上传 JAR 到远端临时目录 /tmp/chronicle_jar_tmp/ ..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" \
    "rm -rf /tmp/chronicle_jar_tmp && mkdir -p /tmp/chronicle_jar_tmp"
  scp -P "${SSH_PORT}" "${LOCAL_JAR}" \
    "${SSH_USER}@${SERVER_IP}:/tmp/chronicle_jar_tmp/${JAR_NAME}"
  log_info "JAR 上传完成，准备停机替换"

  # 3. 优雅停止远端后端进程
  log_info "检查并优雅停止远端后端进程..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
PID=\$(pgrep -f "${JAR_NAME}" || true)
if [[ -n "\$PID" ]]; then
  echo "发现运行中的进程 PID=\$PID，发送 SIGTERM..."
  sudo kill -15 \$PID
  WAIT=0
  while kill -0 \$PID 2>/dev/null; do
    sleep 1
    WAIT=\$((WAIT + 1))
    if [[ \$WAIT -ge 30 ]]; then
      echo "进程 30s 内未退出，发送 SIGKILL..."
      sudo kill -9 \$PID || true
      echo "已强制终止进程"
      break
    fi
  done
  echo "后端进程已停止（等待 \${WAIT}s）"
else
  echo "未发现运行中的后端进程，跳过停止步骤"
fi
EOF

  # 4. 备份旧 JAR
  TIMESTAMP=$(date '+%Y%m%d%H%M%S')
  BAK_SUFFIX=".bak${TIMESTAMP}"
  BACK_KEEP_PLUS1=$((BACKUP_KEEP + 1))
  log_info "备份远端旧 JAR（如存在）..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
if [[ -f "${BACKEND_DEPLOY_DIR}/${JAR_NAME}" ]]; then
  sudo mv "${BACKEND_DEPLOY_DIR}/${JAR_NAME}" "${BACKEND_DEPLOY_DIR}/${JAR_NAME}${BAK_SUFFIX}"
  echo "已备份为: ${JAR_NAME}${BAK_SUFFIX}"
else
  echo "未发现旧 JAR，跳过备份"
fi
EOF

  # 5. 清理旧 JAR 备份，保留最近 BACKUP_KEEP 个
  log_info "远端清理旧 JAR 备份，最多保留 ${BACKUP_KEEP} 个..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
cd "${BACKEND_DEPLOY_DIR}"
to_del=\$(ls -t ${JAR_NAME}.bak* 2>/dev/null | tail -n +${BACK_KEEP_PLUS1})
if [[ -n "\$to_del" ]]; then
  echo "\$to_del" | xargs -r sudo rm -f
  echo "已清理旧 JAR 备份: \$to_del"
else
  echo "JAR 备份数量未超出 ${BACKUP_KEEP} 个，无需清理"
fi
EOF

  # 6. 将临时目录中的新 JAR 移入部署目录并设置权限
  log_info "远端部署新 JAR 并设置权限（0755 root:root）..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" bash <<EOF
set -e
sudo mkdir -p "${BACKEND_DEPLOY_DIR}"
sudo mv "/tmp/chronicle_jar_tmp/${JAR_NAME}" "${BACKEND_DEPLOY_DIR}/${JAR_NAME}"
sudo chown root:root "${BACKEND_DEPLOY_DIR}/${JAR_NAME}"
sudo chmod 0755 "${BACKEND_DEPLOY_DIR}/${JAR_NAME}"
sudo rm -rf /tmp/chronicle_jar_tmp
echo "JAR 权限设置完成"
EOF

  # 7. 启动后端服务（以 root 权限执行）
  log_info "启动后端服务（以 root 权限执行）..."
  ssh -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" \
    "sudo bash -c 'nohup java -jar ${BACKEND_DEPLOY_DIR}/${JAR_NAME} --spring.profiles.active=prod > ${BACKEND_DEPLOY_DIR}/app.log 2>&1 &'"
  log_info "后端启动命令已执行（以 root 用户运行）"

  log_info "====== 后端部署完成 ======"
  log_warn "即将实时查看后端启动日志（Ctrl+C 退出日志查看不会停止远端进程）"
  sleep 2
  ssh -t -p "${SSH_PORT}" "${SSH_USER}@${SERVER_IP}" \
    "tail -f '${BACKEND_DEPLOY_DIR}/app.log'"
}

# ============================================================
# 主流程
# ============================================================
main() {
  # 切换到脚本所在目录，确保相对路径正确
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  cd "${SCRIPT_DIR}"

  check_deps

  echo ""
  echo -e "${BLUE}================================================${NC}"
  echo -e "${BLUE}         Chronicle 快速部署脚本 v               ${NC}"
  echo -e "${BLUE}  服务器: ${SERVER_IP}   用户: ${SSH_USER}      ${NC}"
  echo -e "${BLUE}================================================${NC}"
  echo ""
  echo "请选择部署模式："
  echo "  1) 仅部署前端"
  echo "  2) 仅部署后端"
  echo "  3) 前端 + 后端（先部署前端，再部署后端）"
  echo ""
  read -rp "请输入选项 [1/2/3]: " choice

  case "$choice" in
    1)
      log_info "选择：仅部署前端"
      deploy_frontend
      ;;
    2)
      log_info "选择：仅部署后端"
      deploy_backend
      ;;
    3)
      log_info "选择：前端 + 后端（先前端后后端）"
      deploy_frontend
      deploy_backend
      ;;
    *)
      log_error "无效选项: \"$choice\"，请输入 1、2 或 3"
      exit 1
      ;;
  esac

  log_info "所有部署流程结束。"
}

main
