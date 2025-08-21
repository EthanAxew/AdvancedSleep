# AdvancedSleep - 智能睡眠管理插件一个高性能、低资源占用的 Minecraft 睡眠管理插件，支持多世界、GUI 界面和高度自定义配置。

## 功能特点

- ⚡ **极低资源占用** - 优化的算法和零冗余日志输出
- 🎨 **美观的 GUI 界面** - 实时显示睡眠进度和玩家状态
- 🌍 **多世界支持** - 为不同世界配置独立的睡眠规则
- ⚙️ **高度可配置** - 通过 YAML 文件自定义所有消息和行为
- 📊 **智能跳过机制** - 支持百分比和固定人数两种跳过条件

## 安装

1. 下载最新版本的 [AdvancedSleep.jar](https://github.com/your-username/AdvancedSleep/releases)
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/AdvancedSleep/config.yml` 进行自定义配置

## 命令和权限

### 命令
- `/sleep` - 打开睡眠GUI菜单
- `/sleep reload` - 重载配置文件 (需要权限)
- `/sleep stats` - 查看当前睡眠状态

### 权限
- `advancedsleep.use` - 允许使用睡眠功能
- `advancedsleep.admin` - 管理员权限 (重载等)
- `advancedsleep.bypass` - 豁免睡眠计算权限

## 开发构建

1. 克隆仓库: `git clone https://github.com/your-username/AdvancedSleep.git`
2. 导入到 IntelliJ IDEA
3. 使用 Maven 构建: `mvn clean package`
4. 输出文件在 `target/AdvancedSleep.jar`
