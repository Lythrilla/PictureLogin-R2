package me.Lythrilla.picturelogin.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.PermissionManager.PermissionGroup;
import me.Lythrilla.picturelogin.config.UserManager.UserSettings;

/**
 * 提供PictureLogin插件命令的Tab补全功能
 */
public class PictureLoginTabCompleter implements TabCompleter {
    
    private final PictureLogin plugin;
    private final List<String> COMMANDS = Arrays.asList(
            "reload", "version", "language", "help", "debug"
    );
    
    // 调试命令的消息类型
    private final List<String> DEBUG_TYPES = Arrays.asList(
            "login", "leave", "firstjoin", "all"
    );
    
    // 调试命令的目标类型
    private final List<String> DEBUG_TARGETS = Arrays.asList(
            "global", "user", "perm"
    );
    
    public PictureLoginTabCompleter(PictureLogin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 如果是第一个参数，提供所有可用的子命令（根据权限过滤）
        if (args.length == 1) {
            List<String> availableCommands = new ArrayList<>();
            
            // 添加所有玩家有权限使用的命令
            for (String cmd : COMMANDS) {
                if (hasPermission(sender, cmd)) {
                    availableCommands.add(cmd);
                }
            }
            
            // 根据已输入内容过滤
            return getMatchingCompletions(availableCommands, args[0]);
        }
        
        // 处理子命令的参数
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            // language子命令的第二个参数是语言代码
            if (subCommand.equals("language") && sender.hasPermission("picturelogin.language")) {
                List<String> languages = plugin.getConfigManager().getLanguageManager().getAvailableLanguages();
                return getMatchingCompletions(languages, args[1]);
            }
            
            // debug子命令的第二个参数是消息类型
            if (subCommand.equals("debug") && sender.hasPermission("picturelogin.debug")) {
                return getMatchingCompletions(DEBUG_TYPES, args[1]);
            }
        }
        
        // debug命令的后续参数
        if (args.length >= 3 && args[0].toLowerCase().equals("debug") && 
                sender.hasPermission("picturelogin.debug")) {
            
            if (args.length == 3) {
                // 第三个参数是目标类型
                return getMatchingCompletions(DEBUG_TARGETS, args[2]);
            }
            
            if (args.length == 4) {
                if (args[2].toLowerCase().equals("user")) {
                    // 提供在线玩家名称列表
                    List<String> playerNames = new ArrayList<>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerNames.add(player.getName());
                    }
                    return getMatchingCompletions(playerNames, args[3]);
                }
                
                if (args[2].toLowerCase().equals("perm")) {
                    // 提供权限组名称列表
                    List<String> groupNames = new ArrayList<>();
                    for (PermissionGroup group : plugin.getPermissionManager().getPermissionGroups()) {
                        groupNames.add(group.getName());
                    }
                    return getMatchingCompletions(groupNames, args[3]);
                }
            }
        }
        
        // 如果前面没有匹配，返回空列表
        return completions;
    }
    
    /**
     * 检查发送者是否有权限执行特定命令
     */
    private boolean hasPermission(CommandSender sender, String command) {
        switch (command) {
            case "reload":
                return sender.hasPermission("picturelogin.reload");
            case "language":
                return sender.hasPermission("picturelogin.language");
            case "debug":
                return sender.hasPermission("picturelogin.debug");
            case "version":
            case "help":
                return true;
            default:
                return false;
        }
    }
    
    /**
     * 获取与输入部分匹配的补全列表
     */
    private List<String> getMatchingCompletions(List<String> options, String input) {
        if (input.isEmpty()) {
            return options;
        }
        
        String lowerInput = input.toLowerCase();
        List<String> result = options.stream()
                .filter(option -> option.toLowerCase().startsWith(lowerInput))
                .collect(Collectors.toList());
        
        Collections.sort(result);
        return result;
    }
} 