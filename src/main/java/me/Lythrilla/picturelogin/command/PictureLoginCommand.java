package me.Lythrilla.picturelogin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.message.MessageService;

import static me.Lythrilla.picturelogin.util.Translate.tl;

/**
 * PictureLogin插件命令处理器
 */
public class PictureLoginCommand implements CommandExecutor {
    private final PictureLogin plugin;
    private final UserManager userManager;
    private final MessageService messageService;
    
    public PictureLoginCommand(PictureLogin plugin, UserManager userManager, MessageService messageService) {
        this.plugin = plugin;
        this.userManager = userManager;
        this.messageService = messageService;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("picturelogin.reload")) {
                    sender.sendMessage(ChatColor.RED + tl("no_permission"));
                    return true;
                }
                
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + tl("config_reloaded"));
                return true;
                
            case "set":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin set <login|firstjoin|leave> [玩家名]"));
                    return true;
                }
                
                if (!sender.hasPermission("picturelogin.set")) {
                    sender.sendMessage(ChatColor.RED + tl("no_permission"));
                    return true;
                }
                
                handleSetCommand(sender, args);
                return true;
                
            case "clear":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin clear <login|firstjoin|leave> [玩家名]"));
                    return true;
                }
                
                if (!sender.hasPermission("picturelogin.clear")) {
                    sender.sendMessage(ChatColor.RED + tl("no_permission"));
                    return true;
                }
                
                handleClearCommand(sender, args);
                return true;
                
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "PictureLogin 命令帮助:");
        sender.sendMessage(ChatColor.GRAY + "/picturelogin reload - " + ChatColor.WHITE + "重新加载配置文件");
        sender.sendMessage(ChatColor.GRAY + "/picturelogin set <login|firstjoin|leave> [玩家名] - " + ChatColor.WHITE + "设置自定义消息");
        sender.sendMessage(ChatColor.GRAY + "/picturelogin clear <login|firstjoin|leave> [玩家名] - " + ChatColor.WHITE + "清除自定义消息");
    }
    
    private void handleSetCommand(CommandSender sender, String[] args) {
        String type = args[1].toLowerCase();
        Player targetPlayer = null;
        
        if (args.length >= 3) {
            // 设置指定玩家的消息
            String playerName = args[2];
            targetPlayer = plugin.getServer().getPlayer(playerName);
            
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + tl("player_not_found", playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // 设置自己的消息
            targetPlayer = (Player) sender;
        } else {
            // 控制台需要指定玩家
            sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin set <login|firstjoin|leave> <玩家名>"));
            return;
        }
        
        String messageType;
        switch (type) {
            case "login":
                messageType = "messages";
                break;
            case "firstjoin":
                messageType = "first-join-messages";
                break;
            case "leave":
                messageType = "leave-messages";
                break;
            default:
                sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin set <login|firstjoin|leave> [玩家名]"));
                return;
        }
        
        List<String> currentMessages = getCurrentMessages(type, targetPlayer);
        
        // 将消息展示给用户，告知他们当前的消息已被保存
        sender.sendMessage(ChatColor.GREEN + tl("messages_set", targetPlayer.getName(), type));
        for (String line : currentMessages) {
            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + line);
        }
        
        // 保存消息
        userManager.setPlayerMessages(targetPlayer, currentMessages, messageType);
    }
    
    private List<String> getCurrentMessages(String type, Player player) {
        switch (type) {
            case "login":
                return messageService.getLoginMessages(player);
            case "firstjoin":
                return messageService.getFirstJoinMessages(player);
            case "leave":
                return messageService.getLeaveMessages(player);
            default:
                return new ArrayList<>();
        }
    }
    
    private void handleClearCommand(CommandSender sender, String[] args) {
        String type = args[1].toLowerCase();
        Player targetPlayer = null;
        
        if (args.length >= 3) {
            // 清除指定玩家的消息
            String playerName = args[2];
            targetPlayer = plugin.getServer().getPlayer(playerName);
            
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + tl("player_not_found", playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // 清除自己的消息
            targetPlayer = (Player) sender;
        } else {
            // 控制台需要指定玩家
            sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin clear <login|firstjoin|leave> <玩家名>"));
            return;
        }
        
        String messageType;
        switch (type) {
            case "login":
                messageType = "messages";
                break;
            case "firstjoin":
                messageType = "first-join-messages";
                break;
            case "leave":
                messageType = "leave-messages";
                break;
            default:
                sender.sendMessage(ChatColor.RED + tl("command_usage", "/picturelogin clear <login|firstjoin|leave> [玩家名]"));
                return;
        }
        
        // 清除消息
        userManager.setPlayerMessages(targetPlayer, new ArrayList<>(), messageType);
        sender.sendMessage(ChatColor.GREEN + tl("messages_cleared", targetPlayer.getName(), type));
    }
} 