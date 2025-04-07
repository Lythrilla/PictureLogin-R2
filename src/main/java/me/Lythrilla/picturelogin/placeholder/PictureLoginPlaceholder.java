package me.Lythrilla.picturelogin.placeholder;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * 为PictureLogin提供占位符支持
 * 提供以下占位符：
 * %picturelogin_avatar_1% - 当前玩家头像第1行
 * %picturelogin_avatar_2% - 当前玩家头像第2行
 * ...
 * %picturelogin_avatar_8% - 当前玩家头像第8行
 * 
 * 也可以获取指定玩家的头像：
 * %picturelogin_player_avatar_玩家名_1% - 指定玩家头像第1行
 * %picturelogin_player_avatar_玩家名_2% - 指定玩家头像第2行
 * ...
 * %picturelogin_player_avatar_玩家名_8% - 指定玩家头像第8行
 */
public class PictureLoginPlaceholder extends PlaceholderExpansion {
    
    private final PictureLogin plugin;
    private final PictureUtil pictureUtil;
    
    public PictureLoginPlaceholder(PictureLogin plugin) {
        this.plugin = plugin;
        this.pictureUtil = plugin.getPictureUtil();
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String getAuthor() {
        return "Lythrilla";
    }
    
    @Override
    public String getIdentifier() {
        return "picturelogin";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        
        // 解析avatar_X格式的占位符，其中X是行号（1-8）
        if (identifier.startsWith("avatar_")) {
            try {
                int lineNumber = Integer.parseInt(identifier.substring(7));
                String line = pictureUtil.getAvatarLine(player, lineNumber);
                // 确保颜色代码被正确解析
                return org.bukkit.ChatColor.translateAlternateColorCodes('&', line);
            } catch (NumberFormatException e) {
                return "";
            }
        }
        
        // 解析player_avatar_玩家名_行号格式的占位符
        if (identifier.startsWith("player_avatar_")) {
            try {
                // 去掉前缀 "player_avatar_"
                String remainingIdentifier = identifier.substring(14);
                
                // 分割剩余的标识符，获取玩家名和行号
                String[] parts = remainingIdentifier.split("_");
                if (parts.length != 2) {
                    return "";
                }
                
                String targetPlayerName = parts[0];
                int lineNumber = Integer.parseInt(parts[1]);
                
                // 使用异步方法获取指定玩家的头像行，它会自动处理缓存和异步加载
                String line = pictureUtil.getAvatarLineByName(targetPlayerName, lineNumber);
                
                // 确保颜色代码被正确解析
                if (line != null && !line.isEmpty()) {
                    return org.bukkit.ChatColor.translateAlternateColorCodes('&', line);
                }
                return "";
            } catch (Exception e) {
                return "&c错误&r";
            }
        }
        
        return null;
    }
} 