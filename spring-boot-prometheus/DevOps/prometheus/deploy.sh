#!/usr/bin/env bash
#
# Prometheus 自动化部署脚本
#
# 用途：下载指定版本的 Prometheus，安装到 /opt/prometheus，注册 systemd 服务并启动
# 使用：以 root 或具有 sudo 权限的用户执行，执行前请根据实际环境修改配置中的占位符

set -euo pipefail

# ================================ 可配置参数 ================================

# Prometheus 版本号（从 GitHub Releases 获取）
readonly PROMETHEUS_VERSION="3.13.0"

# 安装根目录
readonly INSTALL_DIR="/opt/prometheus"

# 配置文件安装目录
readonly CONFIG_DIR="${INSTALL_DIR}/config"

# 数据存储目录
readonly DATA_DIR="${INSTALL_DIR}/data"

# systemd 服务文件路径
readonly SYSTEMD_DIR="/etc/systemd/system"

# 临时下载与解压目录（脚本结束后自动清理）
readonly TMP_DIR="/tmp/prometheus-${PROMETHEUS_VERSION}"

# 下载地址
readonly DOWNLOAD_URL="https://github.com/prometheus/prometheus/releases/download/v${PROMETHEUS_VERSION}/prometheus-${PROMETHEUS_VERSION}.linux-amd64.tar.gz"

# 脚本所在目录（用于获取同目录下的 prometheus.yml 和 prometheus.service 路径）
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ================================ 前置检查 ================================

echo "====> [1/7] 执行前置检查"

# 检查是否为 root 或具备 sudo 权限
if [[ "$(id -u)" -ne 0 ]]; then
    echo "错误：此脚本需要以 root 用户执行"
    exit 1
fi

# 检查必需工具是否存在
for cmd in curl tar cut; do
    if ! command -v "${cmd}" &> /dev/null; then
        echo "错误：缺少必需命令 ${cmd}，请先安装"
        exit 1
    fi
done

# 检查同目录下是否存在配置与服务文件
if [[ ! -f "${SCRIPT_DIR}/prometheus.yml" ]]; then
    echo "错误：${SCRIPT_DIR}/prometheus.yml 不存在，部署中止"
    exit 1
fi
if [[ ! -f "${SCRIPT_DIR}/prometheus.service" ]]; then
    echo "错误：${SCRIPT_DIR}/prometheus.service 不存在，部署中止"
    exit 1
fi

echo "前置检查通过"

# ================================ 创建用户与目录 ================================

echo "====> [2/7] 创建用户与目录"

# 创建 prometheus 系统用户（如果不存在）
if ! id prometheus &> /dev/null; then
    useradd --system --no-create-home --shell /bin/false prometheus
    echo "已创建 prometheus 系统用户"
else
    echo "prometheus 用户已存在，跳过"
fi

# 创建必要目录
mkdir -p "${CONFIG_DIR}" "${DATA_DIR}"
echo "已创建目录：${CONFIG_DIR} ${DATA_DIR}"

# ================================ 下载 Prometheus ================================

echo "====> [3/7] 下载 Prometheus ${PROMETHEUS_VERSION}"

# 创建临时目录
mkdir -p "${TMP_DIR}"

# 下载压缩包
echo "正在从 ${DOWNLOAD_URL} 下载..."
curl --fail --location --show-error --silent \
    "${DOWNLOAD_URL}" \
    --output "${TMP_DIR}/prometheus.tar.gz"

echo "下载完成"

# ================================ 解压与复制 ================================

echo "====> [4/7] 解压并安装"

# 解压
tar -xzf "${TMP_DIR}/prometheus.tar.gz" -C "${TMP_DIR}"
echo "已解压"

# 停止旧服务（如果存在）
if systemctl is-active --quiet prometheus 2> /dev/null; then
    echo "正在停止旧 Prometheus 服务..."
    systemctl stop prometheus
fi

# 复制可执行文件
cp "${TMP_DIR}/prometheus-${PROMETHEUS_VERSION}.linux-amd64/prometheus" "${INSTALL_DIR}/prometheus"
chmod +x "${INSTALL_DIR}/prometheus"
echo "已复制 prometheus 可执行文件到 ${INSTALL_DIR}/"

# 复制配置文件和 systemd 服务文件
echo "====> [5/7] 部署配置与服务文件"

cp "${SCRIPT_DIR}/prometheus.yml" "${CONFIG_DIR}/prometheus.yml"
echo "已复制 prometheus.yml 到 ${CONFIG_DIR}/"

cp "${SCRIPT_DIR}/prometheus.service" "${SYSTEMD_DIR}/prometheus.service"
echo "已复制 prometheus.service 到 ${SYSTEMD_DIR}/"

# 递归设置 /opt/prometheus 目录属主，确保数据和日志可写入
chown -R prometheus:prometheus "${INSTALL_DIR}"
echo "已设置 ${INSTALL_DIR} 目录属主为 prometheus"

# ================================ 启动服务 ================================

echo "====> [6/7] 重载 systemd 并启动服务"

# 重新加载 systemd 配置
systemctl daemon-reload
echo "已重载 systemd 配置"

# 设置开机自启
systemctl enable prometheus
echo "已设置 prometheus 开机自启"

# 启动服务
systemctl start prometheus
echo "已启动 prometheus 服务"

# ================================ 验证 ================================

echo "====> [7/7] 验证部署结果"

sleep 2

# 检查服务状态
if systemctl is-active --quiet prometheus; then
    echo "✓ Prometheus 服务运行中"
else
    echo "✗ Prometheus 服务未运行，请执行 journalctl -u prometheus -f 查看日志"
    exit 1
fi

# 检查日志中没有明显的启动错误
if ! journalctl -u prometheus --since "1 minute ago" --no-pager 2> /dev/null | grep -q "Server is ready"; then
    echo "⚠ 未在日志中检测到 'Server is ready'，请手动验证：journalctl -u prometheus --no-pager"
else
    echo "✓ 日志确认 Prometheus 已就绪"
fi

# ================================ 清理 ================================

echo "====> 清理临时文件"

rm -rf "${TMP_DIR}"
echo "已清理 ${TMP_DIR}"

echo ""
echo "部署完成！Prometheus 正在 http://localhost:9090 运行"
echo "指标端点：http://localhost:9090/metrics"
