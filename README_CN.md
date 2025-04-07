# PictureLogin

<div align="center">

<div align="center">
  <img src="https://i.imgur.com/kNW94py.png">
</div>

[English](README.md) | [中文](README_CN.md)

</div>

## 📝 简介

PictureLogin 是一个 Spigot/Bukkit 插件，当玩家登录服务器时，会展示包含玩家皮肤图像的自定义消息。本分支在原插件的基础上扩展了众多额外功能并改进了性能。

> **原作者**: Nathan Glover (NathanG/ItsNathanG)  
> **分支作者**: Lythrilla

## ✨ 功能特色

### 🚀 核心功能
* 在聊天栏中显示玩家皮肤的ASCII艺术图像
* 可自定义的欢迎消息
* 玩家首次加入的特殊消息
* 离开服务器消息
* PlaceholderAPI 集成
* AuthMe 集成

### 🔥 增强功能
* **多语言支持** - 内置支持多种语言（默认包含英语和中文）
* **在线预览** - 无需重新连接即可在游戏中直接预览登录消息
* **基于权限的消息** - 根据权限组分配不同的登录消息
* **玩家特定消息** - 为特定玩家设置独特的消息
* **命令控制** - 通过命令启用/禁用功能
* **MiniMessage支持** - 使用 Adventure API 的 MiniMessage 格式进行现代文本格式化
* **SkinRestorer集成** - 正确显示来自 SkinRestorer 的自定义皮肤
* **账户类型检测** - 自动检测并处理正版/离线 Minecraft 账户
* **PAPI变量** - 支持头像 PlaceholderAPI 变量
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
4. 在 `plugins/PictureLogin/config.yml` 中配置插件

## 🔑 权限

权限 | 描述
--- | ---
`picturelogin.use` | 允许使用 PictureLogin 命令（默认: 所有人）
`picturelogin.show` | 允许玩家看到登录消息（默认: 所有人）
`picturelogin.reload` | 允许重载 PictureLogin 插件配置（默认: OP）
`picturelogin.language` | 允许更改插件语言（默认: OP）
`picturelogin.debug` | 允许使用调试命令（默认: OP）
`picturelogin.group.vip` | VIP 玩家的自定义消息权限（默认: 否）
`picturelogin.group.admin` | 管理员的自定义消息权限（默认: OP）

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

本插件支持多语言系统，语言文件位于 `plugins/PictureLogin/lang/` 目录。默认提供以下语言：

* 英文 (en_US.yml)
* 中文 (zh_CN.yml)

您可以复制这些文件并创建自己的翻译。

## 🔌 PlaceholderAPI变量

本插件提供以下 PlaceholderAPI 变量用于在其他插件中显示玩家头像：

### 当前玩家头像变量
```
%picturelogin_avatar_1% - 当前玩家头像第 1 行
%picturelogin_avatar_2% - 当前玩家头像第 2 行
%picturelogin_avatar_3% - 当前玩家头像第 3 行
%picturelogin_avatar_4% - 当前玩家头像第 4 行
```

### 其他玩家头像变量
```
%picturelogin_avatar_1_<玩家名>% - 指定玩家头像第 1 行
%picturelogin_avatar_2_<玩家名>% - 指定玩家头像第 2 行
%picturelogin_avatar_3_<玩家名>% - 指定玩家头像第 3 行
%picturelogin_avatar_4_<玩家名>% - 指定玩家头像第 4 行
```

## 🎨 消息格式化

本插件支持多种文本格式化方法：

### MiniMessage格式
```
<gradient:red:blue>这是渐变文本</gradient>
<rainbow>这是彩虹文本</rainbow>
<color:gold>这是金色文本</color>
```

### Legacy颜色代码
```
&6这是金色文本
&b这是天蓝色文本
```

## ⚙️ 配置

插件配置位于 `plugins/PictureLogin/config.yml` 。您可以自定义：

* 消息格式
* 头像外观
* 特效设置
* 权限组消息
* 玩家特定消息

## 🤝 贡献

欢迎贡献！请随时提交Pull Request。

## 📄 许可证

本项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件。

<div align="center">

[GitHub仓库](https://github.com/Lythrilla/PictureLogin) | [问题反馈](https://github.com/Lythrilla/PictureLogin/issues) | [原始插件](https://github.com/ItsNathanG/PictureLogin)

</div>

