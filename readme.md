# PictureLogin

<div align="center">


![许可证](https://img.shields.io/github/license/ItsNathanG/PictureLogin)


</div>

## 📝 简介

PictureLogin是一个增强版的Spigot/Bukkit插件，当玩家登录服务器时，会展示包含玩家皮肤图像的自定义消息。本分支在原插件的基础上扩展了众多额外功能并改进了性能。

> **原作者**: Nathan Glover (NathanG/ItsNathanG)  
> **分支作者**: Lythrilla

## ✨ 功能特色

### 🚀 核心功能
* 在聊天栏中显示玩家皮肤的ASCII艺术图像
* 可自定义的欢迎消息
* 玩家首次加入的特殊消息
* 离开服务器消息
* PlaceholderAPI集成
* AuthMe集成

### 🔥 增强功能
* **多语言支持** - 内置支持多种语言（默认包含英语和中文）
* **在线预览** - 无需重新连接即可在游戏中直接预览登录消息
* **基于权限的消息** - 根据权限组分配不同的登录消息
* **玩家特定消息** - 为特定玩家设置独特的消息
* **命令控制** - 通过命令启用/禁用功能
* **MiniMessage支持** - 使用Adventure API的MiniMessage格式进行现代文本格式化
* **SkinRestorer集成** - 正确显示来自SkinRestorer的自定义皮肤
* **账户类型检测** - 自动检测并处理正版/离线Minecraft账户
* **PAPI变量** - 支持头像PlaceholderAPI变量
* **Legacy颜色支持** - 与传统颜色代码向后兼容

## 📋 要求

* Java 17或更高版本
* Spigot/Paper 1.20或更高版本
* (可选) PlaceholderAPI - 用于使用其他插件的变量
* (可选) AuthMe - 用于登录集成
* (可选) SkinRestorer - 用于自定义皮肤支持
* (可选) PremiumVanish - 用于玩家隐身兼容

## 📥 安装

1. 从[Releases](https://github.com/Lythrilla/PictureLogin/releases)下载最新的JAR文件
2. 将JAR文件放入服务器的`plugins`文件夹
3. 重启服务器或重新加载插件
4. 在`plugins/PictureLogin/config.yml`中配置插件

## 🔑 权限

权限 | 描述
--- | ---
`picturelogin.use` | 允许使用PictureLogin命令（默认: 所有人）
`picturelogin.show` | 允许玩家看到登录消息（默认: 所有人）
`picturelogin.reload` | 允许重载PictureLogin插件配置（默认: OP）
`picturelogin.language` | 允许更改插件语言（默认: OP）
`picturelogin.debug` | 允许使用调试命令（默认: OP）
`picturelogin.group.vip` | VIP玩家的自定义消息权限（默认: 否）
`picturelogin.group.admin` | 管理员的自定义消息权限（默认: OP）
`picturelogin.effect.rainbow` | 彩虹特效权限（默认: 否）
`picturelogin.effect.gradient` | 渐变特效权限（默认: 否）

## 🖥️ 命令

命令 | 描述
--- | ---
`/picturelogin reload` | 重新加载配置
`/picturelogin language [lang]` | 查看或更改语言
`/picturelogin version` | 显示版本信息
`/picturelogin help` | 显示帮助信息
`/picturelogin debug <login\|leave\|firstjoin\|all> [global\|user\|perm] [名称]` | 调试不同类型的消息

**别名**: `/piclogin`, `/plogin`, `/pl`

## 🌐 多语言支持

本插件支持多语言系统，语言文件位于`plugins/PictureLogin/lang/`目录。默认提供以下语言：

* 英文 (en_US.yml)
* 中文 (zh_CN.yml)

您可以复制这些文件并创建自己的翻译。

## 🔌 PlaceholderAPI变量

本插件提供以下PlaceholderAPI变量用于在其他插件中显示玩家头像：

### 当前玩家头像变量
```
%picturelogin_avatar_1% - 当前玩家头像第1行
%picturelogin_avatar_2% - 当前玩家头像第2行
%picturelogin_avatar_3% - 当前玩家头像第3行
%picturelogin_avatar_4% - 当前玩家头像第4行
%picturelogin_avatar_5% - 当前玩家头像第5行
%picturelogin_avatar_6% - 当前玩家头像第6行
%picturelogin_avatar_7% - 当前玩家头像第7行
%picturelogin_avatar_8% - 当前玩家头像第8行
```

### 指定玩家头像变量
```
%picturelogin_player_avatar_玩家名_1% - 指定玩家头像第1行
%picturelogin_player_avatar_玩家名_2% - 指定玩家头像第2行
%picturelogin_player_avatar_玩家名_3% - 指定玩家头像第3行
%picturelogin_player_avatar_玩家名_4% - 指定玩家头像第4行
%picturelogin_player_avatar_玩家名_5% - 指定玩家头像第5行
%picturelogin_player_avatar_玩家名_6% - 指定玩家头像第6行
%picturelogin_player_avatar_玩家名_7% - 指定玩家头像第7行
%picturelogin_player_avatar_玩家名_8% - 指定玩家头像第8行
```

## 🇨🇳 中英文配置文件

插件默认使用英文配置文件`config.yml`。如果您想使用中文配置，可以将`config_cn.yml`的内容复制到`config.yml`中

```yaml
language: "zh_CN"
```

配置文件中的注释说明也会相应地使用中文或英文。

## 🎨 MiniMessage支持

本插件支持Adventure的MiniMessage格式，这是一种现代、灵活的文本格式化系统。您可以在消息中使用所有MiniMessage标签。

示例：

```
- '<yellow>欢迎来到 <gradient:green:blue:red>服务器</gradient>!'
- '<rainbow>这是彩虹文字</rainbow>'
```

## 🎭 Legacy颜色支持

对于使用旧版插件或配置的服务器，也支持传统颜色代码：

```
- '&e欢迎来到 &a服务器&e!'
- '&c这是带颜色的文字'
```

## 🔧 构建

要从源代码构建此插件：

```bash
git clone https://github.com/Lythrilla/PictureLogin.git
cd PictureLogin
./gradlew build
```

构建后的jar将位于`build/libs/`目录中。

## 📜 许可证

本项目采用MIT许可证 - 详见[LICENSE](https://github.com/ItsNathanG/PictureLogin/blob/master/LICENSE)文件。

## 🙏 致谢

特别感谢Nathan Glover (ItsNathanG)创建了这个优秀的原始插件。

---

<div align="center">

[GitHub仓库](https://github.com/Lythrilla/PictureLogin) | [问题反馈](https://github.com/Lythrilla/PictureLogin/issues) | [原始插件](https://github.com/ItsNathanG/PictureLogin)

</div>

