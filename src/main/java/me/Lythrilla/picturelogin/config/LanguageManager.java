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
    private List<String> availableLanguages = new ArrayList<>();
    private boolean languagesLoaded = false;

    public LanguageManager(PictureLogin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        
        // 确保语言文件目录存在
        ensureLanguageFilesExist();
        
        // 加载语言列表
        loadLanguages();
        
        // 设置当前语言
        String language = configManager.getString("language", "en_US");
        setLanguage(language);
        
        plugin.getLogger().info(getLogMessage("log_language_manager_init", "%language%", currentLanguage));
    }

    public void reloadLanguage() {
        try {
            // 重新加载语言列表
            this.availableLanguages = getAvailableLanguages();
            
            // 重新加载当前语言
            String language = configManager.getString("language", "en_US");
            setLanguage(language);
            
            plugin.getLogger().info(getLogMessage("log_language_changed", "%language%", currentLanguage));
        } catch (Exception e) {
            plugin.getLogger().severe("重新加载语言文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 确保语言文件目录存在，并包含默认语言文件
     */
    private void ensureLanguageFilesExist() {
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
            plugin.getLogger().info(getLogMessage("log_creating_lang_dir", "%path%", langDir.getPath()));
        }

        // 复制内置语言文件
        File enFile = new File(langDir, "en_US.yml");
        File zhFile = new File(langDir, "zh_CN.yml");

        if (!enFile.exists()) {
            plugin.saveResource("lang/en_US.yml", false);
            plugin.getLogger().info(getLogMessage("log_creating_en_file", "%path%", enFile.getPath()));
        }

        if (!zhFile.exists()) {
            plugin.saveResource("lang/zh_CN.yml", false);
            plugin.getLogger().info(getLogMessage("log_creating_zh_file", "%path%", zhFile.getPath()));
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
            plugin.getLogger().log(Level.WARNING, getLogMessage("log_language_not_found", 
                new String[] {"%language%", "%default%"}, 
                new String[] {lang, "en_US"}));
            
            // 尝试提取默认语言文件
            ensureLanguageFilesExist();
            
            // 如果不是请求默认语言，则回退到默认语言
            if (!lang.equals("en_US")) {
                return getLanguageConfig("en_US");
            }
        }

        // 加载语言文件
        YamlConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        plugin.getLogger().fine("已加载语言文件: " + lang); // 使用fine级别减少日志输出
        return config;
    }

    /**
     * 设置当前语言
     * 
     * @param language 语言代码
     */
    public void setLanguage(String language) {
        // 检查语言是否改变，如果没有改变则不重新加载
        if (this.currentLanguage != null && this.currentLanguage.equals(language)) {
            plugin.getLogger().info("语言未改变，仍为: " + language);
            return;
        }
        
        // 检查语言是否可用
        List<String> availableLanguages = getAvailableLanguages();
        if (!availableLanguages.contains(language)) {
            plugin.getLogger().warning(getLogMessage("log_language_not_found", 
                new String[] {"%language%", "%default%"}, 
                new String[] {language, "en_US"}));
            language = "en_US";
        }
        
        // 保存新的语言设置到配置文件
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            config.set("language", language);
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().warning("无法保存语言设置: " + e.getMessage());
        }
        
        // 更新当前语言和配置
        this.currentLanguage = language;
        this.langConfig = getLanguageConfig(language);
        
        // 确保语言列表已加载
        loadLanguages();
        
        plugin.getLogger().info(getLogMessage("log_language_changed", "%language%", language));
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getMessage(String key) {
        // 防止无限递归，如果langConfig为null则使用英文默认消息
        if (langConfig == null) {
            // 直接加载默认英文配置而不是调用reloadLanguage
            langConfig = getLanguageConfig("en_US");
            if (langConfig == null) {
                // 如果仍然不能加载，返回键名以避免进一步的问题
                return key;
            }
        }

        // 从当前语言配置获取消息
        String message = langConfig.getString(key);
        
        // 如果在当前语言中找不到，尝试从英语中获取
        if (message == null && !currentLanguage.equals("en_US")) {
            YamlConfiguration enConfig = getLanguageConfig("en_US");
            message = enConfig.getString(key);
        }
        
        // 如果仍然为null，记录警告并返回键名
        if (message == null) {
            plugin.getLogger().warning("找不到语言键: " + key + " 在语言 " + currentLanguage);
            return key;
        }
        
        // 处理前缀变量替换
        if (message.contains("%prefix%")) {
            String prefix = langConfig.getString("prefix");
            if (prefix != null) {
                message = message.replace("%prefix%", prefix);
            }
        }
        
        // 处理其他基本变量
        message = message.replace("%new_line%", "\n");
        
        return message;
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

    /**
     * 检查指定的语言是否可用
     * 
     * @param language 要检查的语言代码
     * @return 如果语言可用则返回true，否则返回false
     */
    public boolean isLanguageAvailable(String language) {
        return availableLanguages.contains(language);
    }

    public void loadLanguages() {
        if (!languagesLoaded) {
            this.availableLanguages = getAvailableLanguages();
            languagesLoaded = true;
            plugin.getLogger().info(getLogMessage("log_languages_loaded", "%languages%", this.availableLanguages.toString()));
        }
    }
    
    /**
     * 获取本地化的日志消息
     * 
     * @param key 消息键
     * @param placeholder 占位符
     * @param value 替换值
     * @return 本地化的消息
     */
    private String getLogMessage(String key, String placeholder, String value) {
        // 防止无限递归
        if (langConfig == null) {
            // 如果未加载，使用硬编码的消息返回
            return key + ": " + value;
        }
        
        // 直接从配置获取消息而不调用getMessage
        String message = langConfig.getString(key);
        if (message == null) {
            // 如果找不到消息，返回简单格式
            return key + ": " + value;
        }
        
        return message.replace(placeholder, value);
    }
    
    /**
     * 获取本地化的日志消息（多参数版本）
     * 
     * @param key 消息键
     * @param placeholders 占位符数组
     * @param values 替换值数组
     * @return 本地化的消息
     */
    private String getLogMessage(String key, String[] placeholders, String[] values) {
        if (placeholders.length != values.length) {
            return key;
        }
        
        // 防止无限递归
        if (langConfig == null) {
            // 如果未加载，构建简单的消息
            StringBuilder result = new StringBuilder(key);
            result.append(": ");
            for (int i = 0; i < placeholders.length; i++) {
                result.append(placeholders[i]).append("=").append(values[i]);
                if (i < placeholders.length - 1) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
        
        // 直接从配置获取消息而不调用getMessage
        String message = langConfig.getString(key);
        if (message == null) {
            // 如果找不到消息，返回简单格式
            StringBuilder result = new StringBuilder(key);
            result.append(": ");
            for (int i = 0; i < placeholders.length; i++) {
                result.append(placeholders[i]).append("=").append(values[i]);
                if (i < placeholders.length - 1) {
                    result.append(", ");
                }
            }
            return result.toString();
        }
        
        for (int i = 0; i < placeholders.length; i++) {
            message = message.replace(placeholders[i], values[i]);
        }
        return message;
    }
}
