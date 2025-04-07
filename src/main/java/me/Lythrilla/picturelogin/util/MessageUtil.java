package me.Lythrilla.picturelogin.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * 处理Adventure MiniMessage格式化
 */
public class MessageUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static BukkitAudiences adventure;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    // 禁止直接实例化
    private MessageUtil() {
        throw new IllegalStateException("工具类不应被实例化");
    }

    /**
     * 初始化Adventure API
     * 
     * @param audiences BukkitAudiences实例
     */
    public static void init(BukkitAudiences audiences) {
        adventure = audiences;
    }

    /**
     * 关闭Adventure API
     */
    public static void shutdown() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    /**
     * 将传统的颜色格式转换为MiniMessage格式
     * 
     * @param text 待转换的文本
     * @return 转换后的文本
     */
    public static String legacyToMiniMessage(String text) {
        if (text == null) return "";
        
        // 如果已经是MiniMessage格式，直接返回
        if (text.startsWith("<") && text.contains(">")) {
            return text;
        }
        
        // 处理Hex颜色
        text = HEX_PATTERN.matcher(text).replaceAll("<#$1>");
        
        // 替换&为§以便稍后处理
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        // 替换传统颜色代码为MiniMessage格式
        text = text.replace("§0", "<black>");
        text = text.replace("§1", "<dark_blue>");
        text = text.replace("§2", "<dark_green>");
        text = text.replace("§3", "<dark_aqua>");
        text = text.replace("§4", "<dark_red>");
        text = text.replace("§5", "<dark_purple>");
        text = text.replace("§6", "<gold>");
        text = text.replace("§7", "<gray>");
        text = text.replace("§8", "<dark_gray>");
        text = text.replace("§9", "<blue>");
        text = text.replace("§a", "<green>");
        text = text.replace("§b", "<aqua>");
        text = text.replace("§c", "<red>");
        text = text.replace("§d", "<light_purple>");
        text = text.replace("§e", "<yellow>");
        text = text.replace("§f", "<white>");
        
        // 替换格式代码
        text = text.replace("§l", "<bold>");
        text = text.replace("§m", "<strikethrough>");
        text = text.replace("§n", "<underlined>");
        text = text.replace("§o", "<italic>");
        text = text.replace("§k", "<obfuscated>");
        text = text.replace("§r", "<reset>");
        
        return text;
    }

    /**
     * 使用MiniMessage格式化文本并添加PlaceholderAPI变量
     * 
     * @param player 玩家
     * @param message 消息内容
     * @return 格式化后的组件
     */
    public static Component formatMessage(Player player, String message) {
        if (message == null) return Component.empty();
        
        // 检查是否为MiniMessage格式
        boolean isMiniMessage = message.startsWith("<") && message.contains(">");
        
        // 处理占位符
        if (Hooks.PLACEHOLDER_API) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }
        
        // 处理基本变量
        message = message.replace("%pname%", player.getName());
        message = message.replace("%uuid%", player.getUniqueId().toString());
        message = message.replace("%online%", String.valueOf(player.getServer().getOnlinePlayers().size()));
        message = message.replace("%max%", String.valueOf(player.getServer().getMaxPlayers()));
        message = message.replace("%motd%", player.getServer().getMotd());
        message = message.replace("%displayname%", player.getDisplayName());
        
        // 如果不是MiniMessage格式，转换为MiniMessage格式
        if (!isMiniMessage) {
            message = legacyToMiniMessage(message);
        }
        
        // 解析MiniMessage
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            // 如果解析失败，返回纯文本组件
            return Component.text(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    /**
     * 向玩家发送格式化消息
     * 
     * @param player 玩家
     * @param message 消息内容
     */
    public static void sendMessage(Player player, String message) {
        if (adventure == null || message == null || message.isEmpty()) return;
        
        Component component = formatMessage(player, message);
        adventure.player(player).sendMessage(component);
    }
} 