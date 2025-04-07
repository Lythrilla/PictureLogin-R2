package me.Lythrilla.picturelogin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.Lythrilla.picturelogin.PictureLogin;

/**
 * 管理玩家自定义消息配置
 */
public class UserManager {
    private final PictureLogin plugin;
    private File usersFile;
    private FileConfiguration usersConfig;
    
    // 缓存玩家配置
    private final Map<String, UserSettings> userSettingsCache = new HashMap<>();
    
    public UserManager(PictureLogin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 加载users.yml配置文件
     */
    public void loadConfig() {
        if (usersFile == null) {
            usersFile = new File(plugin.getDataFolder(), "users.yml");
        }
        
        if (!usersFile.exists()) {
            plugin.saveResource("users.yml", false);
        }
        
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
        
        // 清空并重新加载缓存
        userSettingsCache.clear();
        
        ConfigurationSection playersSection = usersConfig.getConfigurationSection("players");
        if (playersSection != null) {
            for (String key : playersSection.getKeys(false)) {
                if (playersSection.getBoolean(key + ".enabled", true)) {
                    UserSettings settings = new UserSettings();
                    
                    settings.setMessages(playersSection.getStringList(key + ".messages"));
                    settings.setFirstJoinMessages(playersSection.getStringList(key + ".first-join-messages"));
                    settings.setLeaveMessages(playersSection.getStringList(key + ".leave-messages"));
                    
                    // 读取音效配置
                    ConfigurationSection soundSection = playersSection.getConfigurationSection(key + ".sound");
                    if (soundSection != null) {
                        settings.setSoundEnabled(soundSection.getBoolean("enabled", false));
                        settings.setSound(soundSection.getString("sound", "ENTITY_PLAYER_LEVELUP"));
                        settings.setVolume((float) soundSection.getDouble("volume", 1.0));
                        settings.setPitch((float) soundSection.getDouble("pitch", 1.0));
                    }
                    
                    userSettingsCache.put(key.toLowerCase(), settings);
                }
            }
        }
    }
    
    /**
     * 保存users.yml配置文件
     */
    public void saveConfig() {
        if (usersFile == null || usersConfig == null) {
            return;
        }
        
        try {
            usersConfig.save(usersFile);
        } catch (Exception e) {
            plugin.getLogger().severe(plugin.getConfigManager().getLanguageManager().getMessage("log_user_save_error").replace("%error%", e.getMessage()));
        }
    }
    
    /**
     * 获取玩家的自定义消息设置
     * 
     * @param player 玩家
     * @return 玩家的自定义消息设置，如果没有则返回null
     */
    public UserSettings getUserSettings(Player player) {
        if (player == null) {
            return null;
        }
        
        // 先尝试通过UUID获取
        String uuid = player.getUniqueId().toString().toLowerCase();
        if (userSettingsCache.containsKey(uuid)) {
            return userSettingsCache.get(uuid);
        }
        
        // 再尝试通过玩家名获取
        String name = player.getName().toLowerCase();
        return userSettingsCache.get(name);
    }
    
    /**
     * 为玩家设置自定义消息
     * 
     * @param player 玩家
     * @param messages 消息列表
     * @param type 消息类型: "messages", "first-join-messages", "leave-messages"
     */
    public void setPlayerMessages(Player player, List<String> messages, String type) {
        String uuid = player.getUniqueId().toString();
        
        // 更新配置
        if (!usersConfig.contains("players." + uuid)) {
            usersConfig.set("players." + uuid + ".enabled", true);
        }
        
        usersConfig.set("players." + uuid + "." + type, messages);
        
        // 更新缓存
        UserSettings settings = userSettingsCache.getOrDefault(uuid.toLowerCase(), new UserSettings());
        
        switch (type) {
            case "messages":
                settings.setMessages(messages);
                break;
            case "first-join-messages":
                settings.setFirstJoinMessages(messages);
                break;
            case "leave-messages":
                settings.setLeaveMessages(messages);
                break;
        }
        
        userSettingsCache.put(uuid.toLowerCase(), settings);
        
        // 保存配置
        saveConfig();
    }
    
    /**
     * 移除玩家的自定义消息设置
     * 
     * @param player 玩家
     */
    public void removePlayerSettings(Player player) {
        String uuid = player.getUniqueId().toString();
        
        // 更新配置
        usersConfig.set("players." + uuid, null);
        
        // 更新缓存
        userSettingsCache.remove(uuid.toLowerCase());
        userSettingsCache.remove(player.getName().toLowerCase());
        
        // 保存配置
        saveConfig();
    }
    
    /**
     * 获取已加载的玩家自定义配置数量
     * 
     * @return 玩家自定义配置数量
     */
    public int getUserCount() {
        return userSettingsCache.size();
    }
    
    /**
     * 为玩家设置自定义音效
     * 
     * @param player 玩家
     * @param enabled 是否启用
     * @param sound 音效名称
     * @param volume 音量
     * @param pitch 音调
     */
    public void setPlayerSound(Player player, boolean enabled, String sound, float volume, float pitch) {
        String uuid = player.getUniqueId().toString();
        
        // 更新配置
        if (!usersConfig.contains("players." + uuid)) {
            usersConfig.set("players." + uuid + ".enabled", true);
        }
        
        usersConfig.set("players." + uuid + ".sound.enabled", enabled);
        usersConfig.set("players." + uuid + ".sound.sound", sound);
        usersConfig.set("players." + uuid + ".sound.volume", volume);
        usersConfig.set("players." + uuid + ".sound.pitch", pitch);
        
        // 更新缓存
        UserSettings settings = userSettingsCache.getOrDefault(uuid.toLowerCase(), new UserSettings());
        settings.setSoundEnabled(enabled);
        settings.setSound(sound);
        settings.setVolume(volume);
        settings.setPitch(pitch);
        
        userSettingsCache.put(uuid.toLowerCase(), settings);
        
        // 保存配置
        saveConfig();
    }
    
    /**
     * 玩家自定义消息设置类
     */
    public static class UserSettings {
        private List<String> messages = new ArrayList<>();
        private List<String> firstJoinMessages = new ArrayList<>();
        private List<String> leaveMessages = new ArrayList<>();
        
        // 音效配置
        private boolean soundEnabled = false;
        private String sound = "ENTITY_PLAYER_LEVELUP";
        private float volume = 1.0f;
        private float pitch = 1.0f;
        
        public List<String> getMessages() {
            return messages;
        }
        
        public void setMessages(List<String> messages) {
            this.messages = messages != null ? messages : new ArrayList<>();
        }
        
        public List<String> getFirstJoinMessages() {
            return firstJoinMessages;
        }
        
        public void setFirstJoinMessages(List<String> firstJoinMessages) {
            this.firstJoinMessages = firstJoinMessages != null ? firstJoinMessages : new ArrayList<>();
        }
        
        public List<String> getLeaveMessages() {
            return leaveMessages;
        }
        
        public void setLeaveMessages(List<String> leaveMessages) {
            this.leaveMessages = leaveMessages != null ? leaveMessages : new ArrayList<>();
        }
        
        public boolean isSoundEnabled() {
            return soundEnabled;
        }
        
        public void setSoundEnabled(boolean soundEnabled) {
            this.soundEnabled = soundEnabled;
        }
        
        public String getSound() {
            return sound;
        }
        
        public void setSound(String sound) {
            this.sound = sound;
        }
        
        public float getVolume() {
            return volume;
        }
        
        public void setVolume(float volume) {
            this.volume = volume;
        }
        
        public float getPitch() {
            return pitch;
        }
        
        public void setPitch(float pitch) {
            this.pitch = pitch;
        }
        
        public boolean hasCustomMessages() {
            return !messages.isEmpty();
        }
        
        public boolean hasCustomFirstJoinMessages() {
            return !firstJoinMessages.isEmpty();
        }
        
        public boolean hasCustomLeaveMessages() {
            return !leaveMessages.isEmpty();
        }
    }
} 