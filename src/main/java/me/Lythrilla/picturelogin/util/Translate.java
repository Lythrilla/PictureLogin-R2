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

    public static String tl(String key) {
        return translate(key);
    }

    private static String translate(String key) {
        if (plugin == null) {
            return "Plugin not initialized";
        }
        
        LanguageManager langManager = plugin.getConfigManager().getLanguageManager();
        String message = langManager.getMessage(key);
        
        // 处理前缀和换行
        if (message.contains("%prefix%")) {
            String prefix = langManager.getMessage("prefix");
            message = message.replace("%prefix%", prefix);
        }
        
        message = message.replace("%new_line%", "\n");
        
        return color(message);
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
