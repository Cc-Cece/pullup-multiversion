# PullUp Multiversion

持续维护中的 `PullUp` 多版本、多加载器分支。

这是一个为 Minecraft 鞘翅飞行提供可自定义语音警报的模组。你可以基于本地条件集、服务端同步条件集，或 PullUp 云端编辑器生成的条件集，定义不同的触发逻辑、HUD 文本和播放音效。

## 项目状态

- 当前版本：`2.1.1`
- 已验证版本：

| Minecraft | Loader |
| --- | --- |
| `1.18.2` | `Fabric`、`Forge` |
| `1.20.1` | `Fabric`、`Forge` |
| `1.20.2` | `Fabric`、`NeoForge` |
| `26.1` | `Fabric`、`NeoForge` |

## 主要特性

- 支持按表达式检测鞘翅飞行状态，并播放自定义警报音效
- 支持 HUD 文本提示
- 支持本地条件集、服务端条件集同步、云端编辑器导入
- 支持通过 GUI 管理启用的条件集

## 快速开始

1. 将对应版本的模组 Jar 放入游戏或服务端的 `mods` 目录。
2. 首次启动后，模组会自动生成：
   - `config/pullup/config.json`
   - `config/pullup/conditions/example.json`
3. 客户端可使用 `/pullupclient load` 或 `/pullupclient gui` 加载条件集。
4. 多人游戏中，如果服务端也安装了 PullUp，客户端可以通过 `/pullupclient grab` 拉取服务端当前条件集。

## 命令

客户端根命令：`/pullupclient`

客户端别名：`/pullup`、`/puc`

| 命令 | 说明 |
| --- | --- |
| `/pullupclient gui` | 打开条件集管理界面 |
| `/pullupclient load <文件名>` | 加载指定条件集文件 |
| `/pullupclient load default` | 加载内置默认条件集 |
| `/pullupclient enable` | 开启警报 |
| `/pullupclient disable` | 关闭警报 |
| `/pullupclient enableserver` | 允许加载服务端同步的条件集 |
| `/pullupclient disableserver` | 禁止加载服务端同步的条件集 |
| `/pullupclient grab` | 主动请求服务端发送当前条件集 |
| `/pullupclient status` | 查看当前启用状态、已加载条件数量与云端状态 |
| `/pullupclient cloud edit` | 基于当前已配置条件集创建云端编辑会话 |
| `/pullupclient cloud import <短码>` | 从云端导入条件集，保存到本地并立即切换 |
| `/pullupclient cloud import <短码> temp` | 仅临时导入到内存，不写入本地文件 |
| `/pullupclient cloud status` | 查看最近一次云端导入状态 |

服务端根命令：`/pullupserver`

服务端别名：`/pus`

| 命令 | 说明 |
| --- | --- |
| `/pullupserver load <文件名>` | 加载指定条件集文件 |
| `/pullupserver load default` | 加载内置默认条件集 |
| `/pullupserver enablesend` | 允许向客户端广播当前服务端条件集 |
| `/pullupserver disablesend` | 禁止向客户端广播当前服务端条件集 |
| `/pullupserver status` | 查看当前服务端同步与云端状态 |
| `/pullupserver cloud import <短码>` | 从云端导入条件集，保存到本地并立即切换 |
| `/pullupserver cloud import <短码> temp` | 仅临时导入到服务端内存，不写入本地文件 |
| `/pullupserver cloud status` | 查看最近一次服务端云端导入状态 |

## 文件与目录

| 路径 | 说明 |
| --- | --- |
| `config/pullup/config.json` | 主配置文件 |
| `config/pullup/conditions/` | 本地条件集目录 |
| `config/pullup/conditions/example.json` | 自动生成的示例条件集 |
| `config/pullup/conditions/cloud-*.json` | 云端导入后自动保存的条件集 |

说明：

- 条件集文件名支持子目录，例如 `shared/test.json`

## 配置项

配置文件路径：`config/pullup/config.json`

| 键名 | 类型 | 说明 |
| --- | --- | --- |
| `enable` | `boolean` | 是否启用 PullUp 警报 |
| `loadServer` | `boolean` | 是否允许客户端加载服务端同步的条件集 |
| `sendServer` | `boolean` | 是否允许服务端向客户端发送当前条件集 |
| `maxDistance` | `int` | 保留配置项，用于部分距离参数的上限 |
| `sendDelay` | `int` | 服务端响应客户端请求的最小间隔 |
| `enabledSets` | `string[]` | 当前配置要加载的条件集列表 |
| `hudTextDisplayX` | `float` | HUD 文本横向位置 |
| `hudTextDisplayY` | `float` | HUD 文本纵向位置 |
| `cloudBaseUrl` | `string` | PullUp 云端编辑服务地址 |

## 条件集格式

条件集文件是一个 JSON 数组，数组中的每个对象代表一条警报规则。

```json
[
  {
    "name": "terrain_ahead",
    "sound": "pullup:terrain_ahead",
    "loop_play": true,
    "play_delay": 20,
    "check_delay": 5,
    "hud_text": {
      "key": "text.flighthud.pullup.terrain_ahead",
      "color": {
        "red": 255,
        "green": 0,
        "blue": 0,
        "alpha": 127
      }
    },
    "arguments": {
      "a": "pullup:distance_ahead",
      "b": "pullup:pitch"
    },
    "expressions": [
      "(abs(a) < 20) & (abs(a) > 5) & (abs(b) < 20)"
    ]
  }
]
```

字段说明：

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `name` | 是 | 条件名，会与文件名一起组成最终 ID |
| `sound` | 是 | 播放的音效 ID，例如 `pullup:pullup` |
| `arguments` | 是 | 表达式变量名到内置参数 ID 的映射 |
| `expressions` | 是 | 检测表达式列表；全部表达式结果都要 `>= 0` 才算触发 |
| `loop_play` | 否 | 是否循环播放，默认 `true` |
| `play_delay` | 否 | 循环播放间隔 tick，默认 `40` |
| `check_delay` | 否 | 检测间隔 tick，默认 `5` |
| `hud_text` | 否 | HUD 文本配置，不配置则不显示 |
| `hud_text.key` | 否 | 文本翻译键 |
| `hud_text.color` | 否 | 文本颜色，支持 RGBA |

建议：

- 文件使用 UTF-8 编码
- 变量名尽量使用小写字母、数字和下划线
- 自定义音效需要对应的资源包或模组资源中存在同名声音资源

## 内置参数

当前代码中已注册的参数如下：

| 参数 ID | 含义 |
| --- | --- |
| `pullup:absolute_height` | 玩家当前 Y 坐标 |
| `pullup:relative_height` | 玩家与下方地面的相对高度 |
| `pullup:speed` | 总速度 |
| `pullup:horizontal_speed` | 水平速度 |
| `pullup:vertical_speed` | 垂直速度 |
| `pullup:yaw` | 朝向水平角 |
| `pullup:delta_yaw` | 水平角变化量 |
| `pullup:pitch` | 朝向俯仰角 |
| `pullup:delta_pitch` | 俯仰角变化量 |
| `pullup:distance_ahead` | 正前方碰撞距离 |
| `pullup:distance_horizontal` | 水平方向碰撞距离 |
| `pullup:flight_ticks` | 本次飞行持续 tick 数 |

## 表达式规则

PullUp 使用 `exp4j` 解析表达式，并额外扩展了比较和布尔风格运算符。

判定规则：

- 单条条件里的所有表达式结果都必须 `>= 0`
- 比较与逻辑运算返回 `1` 或 `-1`
- 因此可以把“非负”理解为真，把“负数”理解为假

额外运算符：

| 表达式 | 含义 |
| --- | --- |
| `A > B` | 若为真返回 `1`，否则返回 `-1` |
| `A >= B` | 若为真返回 `1`，否则返回 `-1` |
| `A < B` | 若为真返回 `1`，否则返回 `-1` |
| `A <= B` | 若为真返回 `1`，否则返回 `-1` |
| `A == B` | 若为真返回 `1`，否则返回 `-1` |
| `A != B` | 若为真返回 `1`，否则返回 `-1` |
| `A & B` | 当 `A >= 0` 且 `B >= 0` 时返回 `1` |
| `A \| B` | 当 `A >= 0` 或 `B >= 0` 时返回 `1` |

常用函数可直接使用 `exp4j` 默认函数，例如：

- `abs`
- `pow`
- `sqrt`
- `ceil`
- `floor`
- `sin`
- `cos`
- `tan`
- `log`
- `log10`

## 云端编辑器

默认云端服务地址：

`https://pullup.akihito.dpdns.org`

使用方式：

1. 在游戏内执行 `/pullupclient cloud edit`
2. 打开返回的编辑链接
3. 编辑并发布后获取短码
4. 使用 `/pullupclient cloud import <短码>` 或 `/pullupserver cloud import <短码>` 导入

补充说明：

- 普通导入会把条件集保存到 `config/pullup/conditions/cloud-<code>.json`
- `temp` 导入只保存在当前会话内存中，重启后失效
- 如果云端条件集引用了自定义音效，命令反馈会提示是否需要额外资源包

## 构建

### 环境要求

- JDK `25`

命令：

```powershell
./gradlew <Task名称> --no-daemon -PtargetProjects=1.20.1-fabric,[其他子项目]
```

清理构建缓存：

```
./gradlew clean --no-daemon
```

## 上游

- 上游项目：`MUYU_Twilighter/PullUp`

## 许可

本分支当前以 `GPL-3.0-only` 发布，另附上游许可证与说明文件：

- [`LICENSE`](LICENSE)
- [`LICENSE.upstream-MIT`](LICENSE.upstream-MIT)
- [`NOTICE.md`](NOTICE.md)
