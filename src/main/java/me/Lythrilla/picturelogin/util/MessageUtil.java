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
     * @param legacyText 传统颜色格式文本
     * @return MiniMessage格式文本
     */
    public static String legacyToMiniMessage(String legacyText) {
        if (legacyText == null) return "";
        
        // 处理Hex颜色
        legacyText = HEX_PATTERN.matcher(legacyText).replaceAll("<#$1>");
        
        // 替换&为§以便稍后处理
        legacyText = ChatColor.translateAlternateColorCodes('&', legacyText);
        
        // 替换传统颜色代码为MiniMessage格式
        legacyText = legacyText.replace("§0", "<black>");
        legacyText = legacyText.replace("§1", "<dark_blue>");
        legacyText = legacyText.replace("§2", "<dark_green>");
        legacyText = legacyText.replace("§3", "<dark_aqua>");
        legacyText = legacyText.replace("§4", "<dark_red>");
        legacyText = legacyText.replace("§5", "<dark_purple>");
        legacyText = legacyText.replace("§6", "<gold>");
        legacyText = legacyText.replace("§7", "<gray>");
        legacyText = legacyText.replace("§8", "<dark_gray>");
        legacyText = legacyText.replace("§9", "<blue>");
        legacyText = legacyText.replace("§a", "<green>");
        legacyText = legacyText.replace("§b", "<aqua>");
        legacyText = legacyText.replace("§c", "<red>");
        legacyText = legacyText.replace("§d", "<light_purple>");
        legacyText = legacyText.replace("§e", "<yellow>");
        legacyText = legacyText.replace("§f", "<white>");
        
        // 替换格式代码
        legacyText = legacyText.replace("§l", "<bold>");
        legacyText = legacyText.replace("§m", "<strikethrough>");
        legacyText = legacyText.replace("§n", "<underlined>");
        legacyText = legacyText.replace("§o", "<italic>");
        legacyText = legacyText.replace("§k", "<obfuscated>");
        legacyText = legacyText.replace("§r", "<reset>");
        
        return legacyText;
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
        
        // 首先处理PlaceholderAPI变量
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
        
        // 转换为MiniMessage格式
        String miniMessageText = legacyToMiniMessage(message);
        
        // 解析MiniMessage
        return miniMessage.deserialize(miniMessageText);
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