package me.Lythrilla.picturelogin.util;

import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.config.PermissionManager.PermissionGroup;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.config.UserManager.UserSettings;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PictureWrapper extends BukkitRunnable {
    private PictureUtil pictureUtil;
    private ConfigManager config;
    private Player player;
    private PictureLogin plugin;

    public PictureWrapper(PictureLogin plugin, Player player) {
        this.pictureUtil = plugin.getPictureUtil();
        this.config      = plugin.getConfigManager();
        this.player      = player;
        this.plugin      = plugin;
    }

    @Override
    public void run() {
        sendImage();
        playSound();
    }

    private boolean checkPermission() {
        if (!config.getBoolean("require-permission", true))
            return true;

        return player.hasPermission("picturelogin.show");
    }

    private ImageMessage getMessage() {
        String msgType;

        // if it's a player's first time and feature is enabled, show different message
        if (config.getBoolean("show-first-join", true) && !player.hasPlayedBefore())
            msgType = "first-join-messages";
        else
            msgType = "messages";

        return pictureUtil.createPictureMessage(player, config.getStringList(msgType));
    }

    private void sendImage() {
        // only show message for players with picturelogin.show permission
        if(!checkPermission()) return;

        ImageMessage pictureMessage = getMessage();

        if (pictureMessage == null) return;

        // send only to the player that joined?
        if (config.getBoolean("player-only", true)) {
            if (config.getBoolean("clear-chat", false))
                pictureUtil.clearChat(player);

            pictureMessage.sendToPlayer(player);
            return;
        }

        pictureUtil.sendOutPictureMessage(pictureMessage);
    }

    /**
     * 播放登录音效
     */
    private void playSound() {
        // 判断是否启用音效
        if (!config.isPlayLoginSound()) return;
        
        try {
            // 1. 首先检查用户自定义音效 (最高优先级)
            UserManager userManager = plugin.getConfigManager().getUserManager();
            if (userManager != null && config.getBoolean("enable-user-messages", true)) {
                UserSettings userSettings = userManager.getUserSettings(player);
                if (userSettings != null && userSettings.isSoundEnabled()) {
                    // 使用用户自定义音效
                    playLoginSound(userSettings.getSound(), userSettings.getVolume(), userSettings.getPitch());
                    return;
                }
            }
            
            // 2. 然后检查权限组自定义音效 (中等优先级)
            PermissionManager permManager = plugin.getConfigManager().getPermissionManager();
            if (permManager != null && config.getBoolean("enable-permission-messages", true)) {
                PermissionGroup permGroup = permManager.getPlayerPermissionGroup(player);
                if (permGroup != null && permGroup.isSoundEnabled()) {
                    // 使用权限组自定义音效
                    playLoginSound(permGroup.getSound(), permGroup.getVolume(), permGroup.getPitch());
                    return;
                }
            }
            
            // 3. 最后使用默认音效配置 (最低优先级)
            playLoginSound(config.getLoginSound(), config.getLoginSoundVolume(), config.getLoginSoundPitch());
        } catch (Exception e) {
            plugin.getLogger().warning("播放登录音效时出错: " + e.getMessage());
        }
    }
    
    /**
     * 播放指定的音效
     * 
     * @param soundName 音效名称
     * @param volume 音量
     * @param pitch 音调
     */
    private void playLoginSound(String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的音效名称: " + soundName);
        }
    }
}
