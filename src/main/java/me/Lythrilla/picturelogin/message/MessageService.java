package me.Lythrilla.picturelogin.message;

import java.util.List;

import org.bukkit.entity.Player;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.PermissionManager.PermissionGroup;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.config.UserManager.UserSettings;

/**
 * 消息服务，协调不同的消息配置
 */
public class MessageService {
    private final PictureLogin plugin;
    private final ConfigManager configManager;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    
    private final boolean enableUserMessages;
    private final boolean enablePermissionMessages;
    
    public MessageService(PictureLogin plugin, ConfigManager configManager, 
                          UserManager userManager, PermissionManager permissionManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        
        // 从配置文件中读取是否启用自定义消息系统
        this.enableUserMessages = configManager.getBoolean("enable-user-messages", true);
        this.enablePermissionMessages = configManager.getBoolean("enable-permission-messages", true);
    }
    
    /**
     * 获取玩家的登录消息
     * 
     * @param player 玩家
     * @return 登录消息列表
     */
    public List<String> getLoginMessages(Player player) {
        // 1. 检查玩家是否有自定义消息
        if (enableUserMessages) {
            UserSettings userSettings = userManager.getUserSettings(player);
            if (userSettings != null && userSettings.hasCustomMessages()) {
                return userSettings.getMessages();
            }
        }
        
        // 2. 检查玩家是否有权限组消息
        if (enablePermissionMessages) {
            PermissionGroup group = permissionManager.getPlayerPermissionGroup(player);
            if (group != null && group.hasCustomMessages()) {
                return group.getMessages();
            }
        }
        
        // 3. 返回默认消息
        return configManager.getMessages();
    }
    
    /**
     * 获取玩家的首次登录消息
     * 
     * @param player 玩家
     * @return 首次登录消息列表
     */
    public List<String> getFirstJoinMessages(Player player) {
        // 1. 检查玩家是否有自定义消息
        if (enableUserMessages) {
            UserSettings userSettings = userManager.getUserSettings(player);
            if (userSettings != null && userSettings.hasCustomFirstJoinMessages()) {
                return userSettings.getFirstJoinMessages();
            }
        }
        
        // 2. 检查玩家是否有权限组消息
        if (enablePermissionMessages) {
            PermissionGroup group = permissionManager.getPlayerPermissionGroup(player);
            if (group != null && group.hasCustomFirstJoinMessages()) {
                return group.getFirstJoinMessages();
            }
        }
        
        // 3. 返回默认消息
        return configManager.getFirstJoinMessages();
    }
    
    /**
     * 获取玩家的离开消息
     * 
     * @param player 玩家
     * @return 离开消息列表
     */
    public List<String> getLeaveMessages(Player player) {
        // 1. 检查玩家是否有自定义消息
        if (enableUserMessages) {
            UserSettings userSettings = userManager.getUserSettings(player);
            if (userSettings != null && userSettings.hasCustomLeaveMessages()) {
                return userSettings.getLeaveMessages();
            }
        }
        
        // 2. 检查玩家是否有权限组消息
        if (enablePermissionMessages) {
            PermissionGroup group = permissionManager.getPlayerPermissionGroup(player);
            if (group != null && group.hasCustomLeaveMessages()) {
                return group.getLeaveMessages();
            }
        }
        
        // 3. 返回默认消息
        return configManager.getLeaveMessages();
    }
    
    /**
     * 重新加载所有消息配置
     */
    public void reloadAll() {
        userManager.loadConfig();
        permissionManager.loadConfig();
    }
} 