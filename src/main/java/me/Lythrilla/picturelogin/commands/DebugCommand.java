package me.Lythrilla.picturelogin.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.PermissionManager.PermissionGroup;
import me.Lythrilla.picturelogin.config.UserManager.UserSettings;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.util.MessageUtil;

/**
 * 处理PictureLogin插件的调试命令
 */
public class DebugCommand {
    private final PictureLogin plugin;
    
    public DebugCommand(PictureLogin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 执行调试命令
     * 
     * @param sender 命令发送者
     * @param args 命令参数
     * @return 命令是否成功执行
     */
    public boolean execute(CommandSender sender, String[] args) {
        // 检查权限
        if (!sender.hasPermission("picturelogin.debug")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("no_permission")));
            return true;
        }
        
        // 检查参数
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("debug_command_help")));
            return true;
        }
        
        // 获取消息类型
        String messageType = args[1].toLowerCase();
        if (!isValidMessageType(messageType)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("debug_invalid_type")
                    .replace("%type%", messageType)));
            return true;
        }
        
        // 在模拟之前清除屏幕
        if (sender instanceof Player && plugin.getConfigManager().getBoolean("clear-chat", false)) {
            plugin.getPictureUtil().clearChat((Player)sender);
        }
        
        // 获取目标类型
        String targetType = "global";
        if (args.length >= 3) {
            targetType = args[2].toLowerCase();
        }
        
        // 处理请求
        switch (targetType) {
            case "global":
                previewGlobalMessages(sender, messageType);
                break;
            case "user":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            plugin.getConfigManager().getMessage("debug_command_help")));
                    return true;
                }
                previewUserMessages(sender, messageType, args[3]);
                break;
            case "perm":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                            plugin.getConfigManager().getMessage("debug_command_help")));
                    return true;
                }
                previewPermissionMessages(sender, messageType, args[3]);
                break;
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        plugin.getConfigManager().getMessage("debug_command_help")));
                break;
        }
        
        return true;
    }
    
    /**
     * 判断消息类型是否有效
     * 
     * @param type 消息类型
     * @return 是否是有效的消息类型
     */
    private boolean isValidMessageType(String type) {
        return type.equals("login") || type.equals("leave") || 
               type.equals("firstjoin") || type.equals("all");
    }
    
    /**
     * 预览全局消息
     * 
     * @param sender 命令发送者
     * @param messageType 消息类型
     */
    private void previewGlobalMessages(CommandSender sender, String messageType) {
        List<String> messages = null;
        String debugHeader = plugin.getConfigManager().getMessage("debug_message_header")
                .replace("%type%", getTypeName(messageType))
                .replace("%target%", "全局");
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', debugHeader));
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // 根据消息类型选择消息列表
            if (messageType.equals("login") || messageType.equals("all")) {
                messages = plugin.getConfigManager().getMessages();
                // 发送登录消息模拟
                previewMessageWithAvatar(player, messages, "登录");
            }
            
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                messages = plugin.getConfigManager().getFirstJoinMessages();
                // 发送首次加入消息模拟
                previewMessageWithAvatar(player, messages, "首次加入");
            }
            
            if (messageType.equals("leave") || messageType.equals("all")) {
                messages = plugin.getConfigManager().getLeaveMessages();
                // 发送离开消息模拟
                previewMessageWithAvatar(player, messages, "离开");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行以查看完整效果");
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessage("debug_message_footer")));
    }
    
    /**
     * 预览用户自定义消息
     * 
     * @param sender 命令发送者
     * @param messageType 消息类型
     * @param playerName 玩家名称
     */
    private void previewUserMessages(CommandSender sender, String messageType, String playerName) {
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("debug_user_not_found")
                    .replace("%player%", playerName)));
            return;
        }
        
        UserSettings userSettings = plugin.getUserManager().getUserSettings(targetPlayer);
        if (userSettings == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("debug_user_not_found")
                    .replace("%player%", playerName)));
            return;
        }
        
        String debugHeader = plugin.getConfigManager().getMessage("debug_message_header")
                .replace("%type%", getTypeName(messageType))
                .replace("%target%", playerName);
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', debugHeader));
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // 根据消息类型选择消息列表
            if (messageType.equals("login") || messageType.equals("all")) {
                List<String> messages = userSettings.getMessages();
                // 如果用户没有自定义消息，使用其权限组消息
                if (messages.isEmpty() && plugin.getConfigManager().getBoolean("enable-permission-messages", true)) {
                    PermissionGroup group = plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer);
                    if (group != null && group.hasCustomMessages()) {
                        messages = group.getMessages();
                    }
                }
                // 如果仍然没有消息，使用全局消息
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getMessages();
                }
                
                // 发送登录消息模拟
                previewMessageWithAvatar(player, messages, "登录 (" + playerName + ")", targetPlayer);
            }
            
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                List<String> messages = userSettings.getFirstJoinMessages();
                // 如果用户没有自定义消息，使用其权限组消息
                if (messages.isEmpty() && plugin.getConfigManager().getBoolean("enable-permission-messages", true)) {
                    PermissionGroup group = plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer);
                    if (group != null && group.hasCustomFirstJoinMessages()) {
                        messages = group.getFirstJoinMessages();
                    }
                }
                // 如果仍然没有消息，使用全局消息
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getFirstJoinMessages();
                }
                
                // 发送首次加入消息模拟
                previewMessageWithAvatar(player, messages, "首次加入 (" + playerName + ")", targetPlayer);
            }
            
            if (messageType.equals("leave") || messageType.equals("all")) {
                List<String> messages = userSettings.getLeaveMessages();
                // 如果用户没有自定义消息，使用其权限组消息
                if (messages.isEmpty() && plugin.getConfigManager().getBoolean("enable-permission-messages", true)) {
                    PermissionGroup group = plugin.getPermissionManager().getPlayerPermissionGroup(targetPlayer);
                    if (group != null && group.hasCustomLeaveMessages()) {
                        messages = group.getLeaveMessages();
                    }
                }
                // 如果仍然没有消息，使用全局消息
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getLeaveMessages();
                }
                
                // 发送离开消息模拟
                previewMessageWithAvatar(player, messages, "离开 (" + playerName + ")", targetPlayer);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行以查看完整效果");
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessage("debug_message_footer")));
    }
    
    /**
     * 预览权限组自定义消息
     * 
     * @param sender 命令发送者
     * @param messageType 消息类型
     * @param groupName 权限组名称
     */
    private void previewPermissionMessages(CommandSender sender, String messageType, String groupName) {
        List<PermissionGroup> groups = plugin.getPermissionManager().getPermissionGroups();
        PermissionGroup targetGroup = null;
        
        for (PermissionGroup group : groups) {
            if (group.getName().equalsIgnoreCase(groupName)) {
                targetGroup = group;
                break;
            }
        }
        
        if (targetGroup == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                    plugin.getConfigManager().getMessage("debug_group_not_found")
                    .replace("%group%", groupName)));
            return;
        }
        
        String debugHeader = plugin.getConfigManager().getMessage("debug_message_header")
                .replace("%type%", getTypeName(messageType))
                .replace("%target%", "权限组: " + targetGroup.getName());
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', debugHeader));
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessage("debug_permission_message")
                .replace("%group%", targetGroup.getName())
                .replace("%permission%", targetGroup.getPermission())
                .replace("%priority%", String.valueOf(targetGroup.getPriority()))));
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // 根据消息类型选择消息列表
            if (messageType.equals("login") || messageType.equals("all")) {
                List<String> messages = targetGroup.getMessages();
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getMessages();
                }
                // 发送登录消息模拟
                previewMessageWithAvatar(player, messages, "登录 (权限组: " + targetGroup.getName() + ")");
            }
            
            if (messageType.equals("firstjoin") || messageType.equals("all")) {
                List<String> messages = targetGroup.getFirstJoinMessages();
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getFirstJoinMessages();
                }
                // 发送首次加入消息模拟
                previewMessageWithAvatar(player, messages, "首次加入 (权限组: " + targetGroup.getName() + ")");
            }
            
            if (messageType.equals("leave") || messageType.equals("all")) {
                List<String> messages = targetGroup.getLeaveMessages();
                if (messages.isEmpty()) {
                    messages = plugin.getConfigManager().getLeaveMessages();
                }
                // 发送离开消息模拟
                previewMessageWithAvatar(player, messages, "离开 (权限组: " + targetGroup.getName() + ")");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "该命令只能由玩家执行以查看完整效果");
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessage("debug_message_footer")));
    }
    
    /**
     * 使用头像预览消息效果
     * 
     * @param player 接收预览的玩家
     * @param messages 消息列表
     * @param typeLabel 类型标签
     */
    private void previewMessageWithAvatar(Player player, List<String> messages, String typeLabel) {
        previewMessageWithAvatar(player, messages, typeLabel, player);
    }
    
    /**
     * 使用头像预览消息效果
     * 
     * @param player 接收预览的玩家
     * @param messages 消息列表
     * @param typeLabel 类型标签
     * @param targetPlayer 目标玩家（用于获取头像和占位符）
     */
    private void previewMessageWithAvatar(Player player, List<String> messages, String typeLabel, Player targetPlayer) {
        if (messages == null || messages.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "  (" + typeLabel + "无消息配置)");
            return;
        }
        
        // 发送分隔符
        player.sendMessage(ChatColor.GOLD + "▶ " + typeLabel + "消息预览:");
        
        // 创建并发送图像消息
        ImageMessage imageMessage = plugin.getPictureUtil().createPictureMessage(targetPlayer, messages);
        if (imageMessage != null) {
            imageMessage.sendToPlayer(player);
        } else {
            player.sendMessage(ChatColor.RED + "无法创建头像图片消息，请检查配置");
        }
    }
    
    /**
     * 获取消息类型的名称
     * 
     * @param type 类型代码
     * @return 类型名称
     */
    private String getTypeName(String type) {
        switch (type) {
            case "login":
                return plugin.getConfigManager().getMessage("message_type_login");
            case "firstjoin":
                return plugin.getConfigManager().getMessage("message_type_firstjoin");
            case "leave":
                return plugin.getConfigManager().getMessage("message_type_leave");
            case "all":
                return "所有消息";
            default:
                return type;
        }
    }
} 