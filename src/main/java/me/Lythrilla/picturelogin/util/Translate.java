package me.Lythrilla.picturelogin.util;

import org.bukkit.ChatColor;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;

public class Translate {
    private static PictureLogin plugin;

    private Translate() {
        throw new IllegalStateException("Utility Class");
    }
    
    public static void init(PictureLogin plugin) {
        Translate.plugin = plugin;
    }

    /**
     * 获取翻译文本
     * 
     * @param key 翻译键
     * @return 翻译文本
     */
    public static String tl(String key) {
        return translate(key);
    }
    
    /**
     * 获取格式化的翻译文本
     * 
     * @param key 翻译键
     * @param args 格式化参数
     * @return 格式化后的翻译文本
     */
    public static String tl(String key, Object... args) {
        String message = translate(key);
        
        // 替换 %s 参数
        for (Object arg : args) {
            message = message.replaceFirst("%s", String.valueOf(arg));
        }
        
        return message;
    }

    private static String translate(String key) {
        if (plugin == null) {
            return "Plugin not initialized";
        }
        
        try {
            LanguageManager langManager = plugin.getConfigManager().getLanguageManager();
            if (langManager == null) {
                return key + " (LanguageManager未初始化)";
            }
            
            String message = langManager.getMessage(key);
            
            // 检查翻译结果是否与键名相同，这通常表示未找到翻译
            if (message.equals(key)) {
                plugin.getLogger().warning("找不到翻译键: " + key);
            }
            
            // 处理前缀和换行
            if (message.contains("%prefix%")) {
                String prefix = langManager.getMessage("prefix");
                message = message.replace("%prefix%", prefix);
            }
            
            message = message.replace("%new_line%", "\n");
            
            return color(message);
        } catch (Exception e) {
            plugin.getLogger().warning("翻译时出错: " + e.getMessage());
            return key + " (翻译错误)";
        }
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
