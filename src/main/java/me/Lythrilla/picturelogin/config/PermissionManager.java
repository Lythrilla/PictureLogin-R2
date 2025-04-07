package me.Lythrilla.picturelogin.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.Lythrilla.picturelogin.PictureLogin;

/**
 * 管理基于权限的自定义消息配置
 */
public class PermissionManager {
    private final PictureLogin plugin;
    private File permsFile;
    private FileConfiguration permsConfig;
    
    // 缓存权限组配置
    private final Map<String, PermissionGroup> permGroupCache = new HashMap<>();
    
    public PermissionManager(PictureLogin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 加载perms.yml配置文件
     */
    public void loadConfig() {
        if (permsFile == null) {
            permsFile = new File(plugin.getDataFolder(), "perms.yml");
        }
        
        if (!permsFile.exists()) {
            plugin.saveResource("perms.yml", false);
        }
        
        permsConfig = YamlConfiguration.loadConfiguration(permsFile);
        
        // 清空并重新加载缓存
        permGroupCache.clear();
        
        ConfigurationSection groupsSection = permsConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String key : groupsSection.getKeys(false)) {
                if (groupsSection.getBoolean(key + ".enabled", true)) {
                    String permission = groupsSection.getString(key + ".permission");
                    int priority = groupsSection.getInt(key + ".priority", 0);
                    
                    if (permission != null && !permission.isEmpty()) {
                        PermissionGroup group = new PermissionGroup(key, permission, priority);
                        
                        group.setMessages(groupsSection.getStringList(key + ".messages"));
                        group.setFirstJoinMessages(groupsSection.getStringList(key + ".first-join-messages"));
                        group.setLeaveMessages(groupsSection.getStringList(key + ".leave-messages"));
                        
                        // 读取音效配置
                        ConfigurationSection soundSection = groupsSection.getConfigurationSection(key + ".sound");
                        if (soundSection != null) {
                            group.setSoundEnabled(soundSection.getBoolean("enabled", false));
                            group.setSound(soundSection.getString("sound", "ENTITY_PLAYER_LEVELUP"));
                            group.setVolume((float) soundSection.getDouble("volume", 1.0));
                            group.setPitch((float) soundSection.getDouble("pitch", 1.0));
                        }
                        
                        permGroupCache.put(permission, group);
                    }
                }
            }
        }
    }
    
    /**
     * 保存perms.yml配置文件
     */
    public void saveConfig() {
        if (permsFile == null || permsConfig == null) {
            return;
        }
        
        try {
            permsConfig.save(permsFile);
        } catch (Exception e) {
            plugin.getLogger().severe(plugin.getConfigManager().getLanguageManager().getMessage("log_permission_save_error").replace("%error%", e.getMessage()));
        }
    }
    
    /**
     * 获取已加载的权限组自定义配置数量
     * 
     * @return 权限组配置数量
     */
    public int getGroupCount() {
        return permGroupCache.size();
    }
    
    /**
     * 获取玩家应使用的权限组
     * 如果玩家拥有多个权限组，返回优先级最高的一个
     * 
     * @param player 玩家
     * @return 权限组，如果玩家没有任何匹配的权限组则返回null
     */
    public PermissionGroup getPlayerPermissionGroup(Player player) {
        if (player == null) {
            return null;
        }
        
        PermissionGroup highestGroup = null;
        
        for (PermissionGroup group : permGroupCache.values()) {
            if (player.hasPermission(group.getPermission())) {
                if (highestGroup == null || group.getPriority() > highestGroup.getPriority()) {
                    highestGroup = group;
                }
            }
        }
        
        return highestGroup;
    }
    
    /**
     * 获取所有权限组
     * 
     * @return 所有已加载的权限组列表
     */
    public List<PermissionGroup> getPermissionGroups() {
        return new ArrayList<>(permGroupCache.values());
    }
    
    /**
     * 权限组类
     */
    public static class PermissionGroup {
        private final String name;
        private final String permission;
        private final int priority;
        
        private List<String> messages = new ArrayList<>();
        private List<String> firstJoinMessages = new ArrayList<>();
        private List<String> leaveMessages = new ArrayList<>();
        
        // 音效配置
        private boolean soundEnabled = false;
        private String sound = "ENTITY_PLAYER_LEVELUP";
        private float volume = 1.0f;
        private float pitch = 1.0f;
        
        public PermissionGroup(String name, String permission, int priority) {
            this.name = name;
            this.permission = permission;
            this.priority = priority;
        }
        
        public String getName() {
            return name;
        }
        
        public String getPermission() {
            return permission;
        }
        
        public int getPriority() {
            return priority;
        }
        
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