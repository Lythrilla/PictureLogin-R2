# PictureLogin

![Build Status](https://github.com/Lythrilla/PictureLogin-master/actions/workflows/build.yml/badge.svg)
![Spigot Version](https://img.shields.io/badge/Spigot-1.21.5-orange.svg)
![License](https://img.shields.io/github/license/Lythrilla/PictureLogin-master)

## 简介

PictureLogin是一个Spigot/Bukkit插件，当玩家登录服务器时，会展示包含玩家皮肤图像的自定义消息。

> **原作者**: [Nathan Glover (NathanG/itsnathang)](https://github.com/itsnathang)  
> **FORK**: [Lythrilla](https://github.com/Lythrilla)

### 功能特色

- 显示玩家的皮肤图像作为ASCII艺术
- 支持自定义欢迎消息
- 支持首次加入服务器的特殊消息
- 支持离开服务器的消息
- 支持PlaceholderAPI变量
- 支持AuthMe集成
- 支持MiniMessage格式化（Adventure API）
- 支持多语言（默认提供英文和中文）

## 要求

- Java 17或更高版本
- Spigot/Paper 1.21.5或更高版本
- (可选) PlaceholderAPI - 用于使用其他插件的变量
- (可选) AuthMe - 用于登录集成

## 安装

1. 从[Releases](https://github.com/Lythrilla/PictureLogin-master/releases)下载最新版本的JAR文件
2. 将JAR文件放入服务器的`plugins`文件夹
3. 重启服务器或使用插件管理器重新加载插件
4. 配置`plugins/PictureLogin/config.yml`文件

## 权限

- `picturelogin.show` - 允许玩家看到登录消息
- `picturelogin.main` - 允许使用管理命令

## 命令

- `/picturelogin reload` - 重新加载配置
- `/picturelogin language [lang]` - 查看或更改语言
- `/picturelogin version` - 显示版本信息

## 多语言支持

本插件支持多语言系统，语言文件位于`plugins/PictureLogin/lang/`目录。
目前默认提供以下语言：
- 英文 (en_US.yml)
- 中文 (zh_CN.yml)

您可以复制这些文件并创建自己的翻译。

## MiniMessage支持

本插件支持Adventure的MiniMessage格式，这是一种现代、灵活的文本格式化系统。您可以在消息中使用所有MiniMessage标签。

例如：
```
- '<yellow>欢迎来到 <gradient:green:blue:red>服务器</gradient>!'
- '<rainbow>这是彩虹文字</rainbow>'
```

## 构建

要从源代码构建此插件：

```bash
git clone https://github.com/Lythrilla/PictureLogin-master.git
cd PictureLogin-master
./gradlew build
```

构建后的jar将位于`build/libs/`目录中。

## 开源协议

本项目采用MIT许可证 - 详见[LICENSE](LICENSE)文件

## 致谢

特别感谢原作者[Nathan Glover (NathanG/itsnathang)](https://github.com/itsnathang)创建了这个优秀的插件。