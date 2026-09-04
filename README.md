# SimpleMiner

**轻量、强大、玩家友好的 Fabric 连锁挖掘模组**

---

## 📦 概述

SimpleMiner 是一款为 Fabric 设计的连锁挖掘模组，专注于**性能优化**和**用户体验**。它不仅提供多种挖掘形状和策略配置，更以**完整的撤销系统**和**服务端配置变更感知**为核心差异点，让连锁挖掘不再是"服主说了算"，而是"每个玩家都能按自己习惯来"。

---

## ✨ 核心特性

### 🔨 三种挖掘形状

- **Shapeless**：基于 BFS 的漫游式挖掘，适合复杂结构
- **Linear**：沿玩家朝向的线状挖掘，支持调整宽度和高度
- **FullChunk**：基于区块的多范围扫描，支持独立配置 X/Z 半径

### ⚙️ 灵活的破坏与收集策略

- **破坏策略**：`PUREAPI`（手动实现）/ `VANILLA`（原版交互）
- **收集策略**：`PUREAPI`（手动掉落）/ `INTERCEPT`（拦截实体）
- 所有策略均可在配置中切换，无需重启服务器

### ↩️ 撤销系统

- 多槽位历史记录（内存保留最近 5 条，磁盘最多 50 条）
- 图形化撤销列表 GUI
- 支持单条撤销、全部撤销、删除单条记录
- 撤销时自动归还物品到玩家背包
- 服务器配置变更时，玩家会收到通知并可一键跟随

### 🖱️ 右键交互模式

- `VANILLA`：完全原版右键行为
- `NOBLOCKITEM`：忽略方块物品右键
- `CROP_HARVEST`：自动收割并重置作物年龄（支持 AGE 属性）

### 🎛️ 配置系统

- **服务端配置**：全局上限、策略选择、黑白名单
- **个体配置**：个人上限、形状参数、是否接收通知
- 基于 YACL，所有配置支持网络同步，修改即生效

### 🧊 数据持久化

- 虚拟线程异步 IO，不阻塞主线程
- 分层存储：内存 + 磁盘（每玩家上限可分别配置）
- 支持 `BlockState` 和 `ItemStack` 的完整序列化

### 🚀 滚轮调整

- 按住 `Left Alt` 调整 Linear 形状的宽度/高度
- 按住 `Right Alt` 调整 FullChunk 形状的 X/Z 半径
- 实时反馈当前参数值

---

## 🔒 权限控制（暂未实现）

服主可通过 `ServerConfig` 禁止特定玩家使用连锁挖掘：

- **黑名单模式**：禁止列表中的玩家使用
- **白名单模式**：只允许列表中的玩家使用

权限检查统一在入口处完成，覆盖所有业务路径。

---

## 🛠️ 安装要求

- **Minecraft**：1.21.x（Fabric）
- **Fabric Loader**：≥ 0.15.0
- **Fabric API**：≥ 0.92.0
- **YACL**：≥ 3.6.1
- **Java**：21+

---
 ## 📁 文件结构

```

<世界目录>/
└── simpleminer/
├── serverconfig.json          # 服务端全局配置
├── individualconfig/          # 每个玩家的个体配置
│   └── <玩家UUID>.json
└── undohistory/               # 撤销记录（按玩家分目录）
└── <玩家UUID>/
└── <undoUUID>.dat     # 每条撤销记录独立存储

```

## ⌨️ 默认快捷键

| 按键 | 功能 |
|------|------|
| `Grave（反引号）` | 按住启动连锁挖掘 |
| `Up / Down（上下箭头）` | 切换挖掘形状（需按住） |
| `Left Alt + 滚轮` | 调整 Linear 形状参数 |
| `Right Alt + 滚轮` | 调整 FullChunk 半径 |
| `Ctrl + Z` | 打开撤销列表 GUI |

## ⚙️ 配置选项

### 服务端配置

| 选项 | 描述 |
|------|------|
| `maxSize` | 全局单次连锁最大方块数 |
| `maxUndoRecords` | 每玩家磁盘撤销记录上限 |
| `collectStrategy` | `PUREAPI` / `INTERCEPT` |
| `blockBreakStrategy` | `PUREAPI` / `VANILLA` |
| `rightClickHandler` | `VANILLA` / `NOBLOCKITEM` |
| `allowUndo` | 是否启用撤销功能 |
| `blockFamilies` | 方块家族匹配规则（支持标签和具体方块） |

### 个体配置

| 选项 | 描述 |
|------|------|
| `personalMaxSize` | 个人单次连锁上限（`-1` 表示跟随服务端） |
| `linearShapeInfos` | Linear 形状的宽度和高度 |
| `fullChunkShapeInfos` | FullChunk 形状的 X/Z 半径 |
| `receiveMaxSizeUpdate` | 是否接收服务端上限变更通知 |
| `maxUndoRecords` | 个人撤销记录上限（受服务端上限约束） |

## 🐛 已知问题

- 渲染预览目前使用朴素线框模式（性能优先，视觉略有重叠）
- FullChunk 形状在客户端与服务器间可能存在轻微延迟（受网络影响）
- 撤销区块加载检查会遍历目标方块列表（在可接受范围内）

## 📄 许可证

本项目使用 **GPL 许可证**，详见 [LICENSE](LICENSE) 文件。

## 🤝 贡献

欢迎提交 Issue 和 PR，建议先开 Issue 讨论功能方向。

## 🙏 致谢

- Fabric 和 YACL 团队提供的基础设施

---

> **SimpleMiner：让连锁挖掘更简单，让每个玩家都能掌控自己的挖掘体验。**
