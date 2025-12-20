# PoemChallenge - 古诗填空挑战插件

**版本:** 1.0.0
**兼容服务器:** Purpur/Paper/Spigot 1.20.1+
**作者:** MEMZGBL

## 简介

PoemChallenge 是一款为 Minecraft 服务器设计的古诗词填空挑战插件。它会定时在聊天框中发布一个古诗填空题，玩家通过指令回答，回答正确即可获得随机奖励。

## 功能特性

*   **定时挑战:** 每隔固定时间（默认为 2 分钟）自动发布新的古诗填空挑战。
*   **随机填空:** 随机选择古诗的某一句作为填空内容。
*   **奖励机制:** 玩家回答正确后，执行预设的服务器指令（默认为经济奖励）。
*   **富文本消息:** 使用 MiniMessage 格式，支持颜色、粗体、斜体等丰富的聊天样式。
*   **管理员控制:** 管理员可以手动触发挑战。

## 安装与使用

1.  将 `PoemChallenge.jar` 文件放入服务器的 `plugins` 文件夹。
2.  **重要：** 确保您的服务器运行环境为 **Java 17 或更高版本**并且提前安装了**"Vault,PlaceholderAPI"**等前置插件
3.  重启或重载服务器。
4.  插件将自动生成默认配置文件 `config.yml`。

## 指令与权限

| 指令 | 描述 | 权限节点 | 默认权限 |
| :--- | :--- | :--- | :--- |
| `/pmans <答案>` | 回答当前的古诗填空挑战。 | `poem.ans` | 默认玩家 (true) |
| `/poem admin trigger` | 管理员手动触发一次古诗填空挑战。 | `poem.admin` | OP (op) |

## 配置 (`config.yml`)

插件的配置文件位于 `plugins/PoemChallenge/config.yml`。

```yaml
# 挑战定时器间隔 (单位: 秒) 时间过快可能导致ip被限制
challenge-interval: 600

# 古诗 API 地址
# 默认使用今日诗词 API，请确保您的服务器可以访问此地址
api-url: "https://v2.jinrishici.com/one.json"

# 奖励配置
reward:
  # 奖励金额最小值
  min: 10
  # 奖励金额最大值
  max: 1000
  # 奖励执行的服务器命令。
  # %player% 会被替换为回答正确的玩家名字。
  # %amount% 会被替换为随机生成的奖励金额。
  # 默认使用 /money give 命令，请确保您的服务器安装了经济插件。
  command: "money give %player% %amount%"

# 消息配置 (使用 MiniMessage 格式)
# 您可以使用 MiniMessage 格式来自定义消息的颜色和样式。
# 格式参考: <red>红色</red>, <bold>粗体</bold>, <italic>斜体</italic>, <gold>金色</gold> 等。
messages:
  # 消息前缀，用于所有广播消息
  prefix: "<gold>【古诗挑战】</gold> "
  # 挑战格式，%question% 为填空题
  challenge-format: "<white>%question%</white> <gray>请使用 /pmans <答案> 回答</gray>"
  # 回答正确消息，%player% 为玩家名，%amount% 为奖励金额
  correct-answer: "<green>恭喜你，%player%！</green> 回答正确！奖励 <yellow>%amount%</yellow> 已发放。"
  # 回答错误消息，%player% 为玩家名
  incorrect-answer: "<red>很遗憾，%player%，</red> 回答错误。别灰心，下次一定！"
  # 当前没有挑战消息
  no-active-challenge: "<red>当前没有正在进行的古诗挑战。</red>"
  # 管理员手动触发成功消息
  admin-trigger-success: "<green>管理员已手动触发古诗挑战。</green>"
  # 权限不足消息
  admin-no-permission: "<red>你没有权限执行此命令。</red>"
```

## 兼容性说明

*   **服务器版本:** 插件基于 Paper API 1.20.1 开发，兼容 Purpur 1.20.1 及其他基于 Paper 的 1.20.1+ 服务端。
*   **Java 版本:** 插件使用 **Java 17** 编译，服务器运行环境需为 **Java 17 或更高版本**。
*   **经济插件:** 奖励命令默认兼容 EssentialsX 等支持 `/money give` 命令的经济插件。如需使用其他经济插件，请修改 `config.yml` 中的 `reward.command`。

## 故障排除

*   **插件无法加载:** 请检查您的服务器 Java 版本是否为 **Java 17 或更高版本**。
*   **无法获取古诗:** 请检查您的服务器网络连接，确保可以访问 `api-url` 中配置的 API 地址。
*   **奖励命令无效:** 请检查 `config.yml` 中 `reward.command` 配置是否正确，并确保您的服务器安装了相应的经济插件。
*   **填空题生成错误:** 插件已优化填空逻辑，但如果 API 返回的诗句格式过于特殊，仍可能导致填空题生成失败。
