package me.Lythrilla.picturelogin.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Lythrilla.picturelogin.PictureLogin;

public class LanguageManager {
    private final PictureLogin plugin;
    private final ConfigManager configManager;
    private YamlConfiguration langConfig;
    private String currentLanguage;

    public LanguageManager(PictureLogin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void reloadLanguage() {
        currentLanguage = configManager.getString("language", "en_US");
        // 确保语言文件目录存在并包含默认语言文件
        ensureLanguageFilesExist();
        langConfig = getLanguageConfig(currentLanguage);
    }

    /**
     * 确保语言文件目录存在，并包含默认语言文件
     */
    private void ensureLanguageFilesExist() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
            plugin.getLogger().info("创建语言文件目录: " + langDir.getPath());
        }

        // 复制内置语言文件
        File enFile = new File(langDir, "en_US.yml");
        File zhFile = new File(langDir, "zh_CN.yml");

        if (!enFile.exists()) {
            plugin.saveResource("lang/en_US.yml", false);
            plugin.getLogger().info("创建英文语言文件: " + enFile.getPath());
        }

        if (!zhFile.exists()) {
            plugin.saveResource("lang/zh_CN.yml", false);
            plugin.getLogger().info("创建中文语言文件: " + zhFile.getPath());
        }
    }

    private YamlConfiguration getLanguageConfig(String lang) {
        // 首先检查缓存
        if (langConfig != null && currentLanguage != null && currentLanguage.equals(lang)) {
            return langConfig;
        }

        // 检查语言文件是否存在
        File langFile = new File(plugin.getDataFolder(), "lang" + File.separator + lang + ".yml");
        
        if (!langFile.exists()) {
            plugin.getLogger().log(Level.WARNING, "找不到语言文件: " + lang + ".yml, 使用英语(en_US)作为默认语言");
            
            // 尝试提取默认语言文件
            ensureLanguageFilesExist();
            
            // 如果不是请求默认语言，则回退到默认语言
            if (!lang.equals("en_US")) {
                return getLanguageConfig("en_US");
            }
        }

        // 加载语言文件
        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        plugin.getLogger().info("已加载语言文件: " + lang);
        return config;
    }

    public void setLanguage(String lang) {
        // 检查语言是否存在
        YamlConfiguration config = getLanguageConfig(lang);
        if (config != null) {
            currentLanguage = lang;
            langConfig = config;
            
            // 保存到配置文件
            FileConfiguration pluginConfig = plugin.getConfig();
            pluginConfig.set("language", lang);
            try {
                pluginConfig.save(new File(plugin.getDataFolder(), "config.yml"));
                plugin.getLogger().info("语言已更改为: " + lang);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "无法保存配置文件", e);
            }
        } else {
            plugin.getLogger().warning("找不到语言: " + lang);
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getMessage(String key) {
        if (langConfig == null) {
            reloadLanguage();
        }

        String message = langConfig.getString(key);
        
        // 如果在当前语言中找不到，尝试从英语中获取
        if (message == null && !currentLanguage.equals("en_US")) {
            YamlConfiguration enConfig = getLanguageConfig("en_US");
            message = enConfig.getString(key);
        }
        
        // 如果仍然为null，返回键名
        return message != null ? message : key;
    }

    public List<String> getAvailableLanguages() {
        List<String> languages = new ArrayList<>();
        File langDir = new File(plugin.getDataFolder(), "lang");
        
        if (langDir.exists() && langDir.isDirectory()) {
            File[] files = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
            
            if (files != null) {
                for (File file : files) {
                    String langCode = file.getName().replace(".yml", "");
                    languages.add(langCode);
                }
            }
        }
        
        if (languages.isEmpty()) {
            // 至少添加默认语言
            languages.add("en_US");
        }
        
        return languages;
    }
}
