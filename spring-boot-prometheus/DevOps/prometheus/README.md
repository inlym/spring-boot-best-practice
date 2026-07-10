# Prometheus 运维资源

本目录集中管理 Prometheus 服务端运维相关的配置文件、systemd 服务定义以及自动化部署脚本。

## 文件说明

| 文件                   | 说明                                    |
|----------------------|---------------------------------------|
| `prometheus.yml`     | Prometheus Server 配置文件，定义抓取目标与远程写入配置  |
| `prometheus.service` | systemd 服务注册文件，用于托管 Prometheus 进程生命周期 |
| `deploy.sh`          | 自动化部署脚本，一键完成下载、安装、注册服务与启动             |
| `README.md`          | 本说明文件                                 |

## prometheus.yml 配置要点

配置文件包含三个主要部分：

1. **全局配置**：抓取间隔（15 秒）、规则评估间隔（15 秒）
2. **抓取目标**：
   - `prometheus`：Prometheus 自身监控指标（localhost:9090）
   - `spring-boot-app`：Spring Boot 应用的 Actuator Prometheus 端点（`/actuator/prometheus`），默认监听 localhost:8080
3. **Remote Write**：将指标远程写入阿里云 Prometheus 托管服务，需替换以下占位符：
   - `<prometheus-instance-id>`：阿里云 Prometheus 实例 ID
   - `<region>`：实例所在地域（如 `cn-hangzhou`）
   - `<your-username>`：认证用户名（通常为 AccessKey ID 或实例用户名）
   - `<your-password>`：认证密码（通常为 AccessKey Secret 或实例密码）

## 部署流程

### 前置条件

- 目标机器运行 Linux（amd64 架构）
- 已安装 `curl`、`tar`、`cut` 等基础工具
- 具备 root 或 sudo 权限

### 执行步骤

1. 将本目录（`DevOps/prometheus/`）完整复制到目标机器
2. 编辑 `prometheus.yml`，将 Remote Write 部分的占位符替换为实际的阿里云 Prometheus 凭证
3. 以 root 用户执行部署脚本：

```bash
chmod +x deploy.sh
sudo ./deploy.sh
```

脚本会自动完成以下操作：

- 创建 `prometheus` 系统用户
- 创建 `/opt/prometheus` 工作目录（含 `config` 和 `data` 子目录）
- 从 GitHub Releases 下载 Prometheus v3.13.0（linux-amd64）
- 解压并复制可执行文件到 `/opt/prometheus/`
- 复制 `prometheus.yml` 到 `/opt/prometheus/config/`
- 注册 systemd 服务并启动

### 手动管理命令

```bash
# 查看服务状态
systemctl status prometheus

# 查看实时日志
journalctl -u prometheus -f

# 重启服务
systemctl restart prometheus

# 停止服务
systemctl stop prometheus

# 禁用开机自启
systemctl disable prometheus
```

### 访问地址

- Prometheus Web UI：`http://<host>:9090`
- 自身指标端点：`http://<host>:9090/metrics`

## 数据保留

默认在 `/opt/prometheus/data` 存储时间序列数据，保留 30 天（通过 `--storage.tsdb.retention.time=30d` 参数配置）。如需调整，修改 `prometheus.service` 中的对应参数后重启服务。

## 版本信息

当前部署 Prometheus v3.13.0。如需升级，修改 `deploy.sh` 开头的 `PROMETHEUS_VERSION` 变量并重新执行脚本。
