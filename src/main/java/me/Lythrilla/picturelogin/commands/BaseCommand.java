package me.Lythrilla.picturelogin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;
import me.Lythrilla.picturelogin.config.ConfigManager;

public class BaseCommand implements CommandExecutor {
	private final PictureLogin plugin;

	// TODO: Remove plugin
	public BaseCommand(PictureLogin plugin) {
	  this.plugin = plugin;
	}
	  
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// 处理帮助命令
		if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
			showHelp(sender);
			return true;
		}
		
		String subCommand = args[0].toLowerCase();
		
		switch (subCommand) {
			case "reload":
				// 检查权限
				if (!sender.hasPermission("picturelogin.reload")) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("no_permission")));
					return true;
				}
				
				// 执行重载
				plugin.getConfigManager().reloadConfig();
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("reload_config")));
				return true;
				
			case "version":
				// 显示版本信息
				String versionInfo = plugin.getConfigManager().getMessage("version_info")
						.replace("%version%", plugin.getDescription().getVersion());
				
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', versionInfo));
				
				// 检查更新
				if (plugin.getConfigManager().getBoolean("update-check", true)) {
					// 这里可以添加更新检查的逻辑
				}
				return true;
				
			case "language":
				handleLanguageCommand(sender, args);
				return true;
				
			case "debug":
				// 检查是否启用了debug命令
				if (!plugin.getConfig().getBoolean("settings.enable_commands.debug", true)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("command_disabled")));
					return true;
				}
				
				// 执行debug命令
				DebugCommand debugCommand = new DebugCommand(plugin);
				return debugCommand.execute(sender, args);
				
			default:
				// 未知命令，显示帮助
				showHelp(sender);
				return true;
		}
	}
	
	/**
	 * 显示帮助信息
	 * 
	 * @param sender 命令发送者
	 */
	private void showHelp(CommandSender sender) {
		String prefix = plugin.getConfigManager().getMessage("prefix");
		String helpHeader = plugin.getConfigManager().getMessage("command_help_header");
		
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', helpHeader));
		
		// 显示版本命令帮助
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
				prefix + plugin.getConfigManager().getMessage("command_version_syntax") + " - " + 
				plugin.getConfigManager().getMessage("command_version_desc")));
		
		// 显示重载命令帮助（如果有权限）
		if (sender.hasPermission("picturelogin.reload")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
					prefix + plugin.getConfigManager().getMessage("command_reload_syntax") + " - " + 
					plugin.getConfigManager().getMessage("command_reload_desc")));
		}
		
		// 显示语言命令帮助（如果有权限）
		if (sender.hasPermission("picturelogin.language")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
					prefix + plugin.getConfigManager().getMessage("command_language_syntax") + " - " + 
					plugin.getConfigManager().getMessage("command_language_desc")));
		}
		
		// 显示debug命令帮助（如果有权限）
		if (sender.hasPermission("picturelogin.debug")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
					prefix + plugin.getConfigManager().getMessage("command_debug_syntax") + " - " + 
					plugin.getConfigManager().getMessage("command_debug_desc")));
		}
	}

	/**
	 * 处理language命令
	 * 
	 * @param sender 命令发送者
	 * @param args 命令参数
	 */
	private void handleLanguageCommand(CommandSender sender, String[] args) {
		// 检查权限
		if (!sender.hasPermission("picturelogin.language")) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getMessage("no_permission")));
			return;
		}
		
		LanguageManager languageManager = plugin.getConfigManager().getLanguageManager();
		
		// 显示当前语言和可用语言
		if (args.length == 1) {
			ConfigManager configManager = plugin.getConfigManager();
			
			// 获取本地化消息
			String header = ChatColor.translateAlternateColorCodes('&', configManager.getMessage("language_list_header"));
			String currentLang = ChatColor.translateAlternateColorCodes('&', configManager.getMessage("current_language")
					.replace("%language%", languageManager.getCurrentLanguage()));
			String availableLangs = ChatColor.translateAlternateColorCodes('&', configManager.getMessage("available_languages")
					.replace("%languages%", String.join(", ", languageManager.getAvailableLanguages())));
			String usage = ChatColor.translateAlternateColorCodes('&', configManager.getMessage("language_usage"));
			String footer = ChatColor.translateAlternateColorCodes('&', configManager.getMessage("language_list_footer"));
			
			// 发送消息
			sender.sendMessage(header);
			sender.sendMessage(currentLang);
			sender.sendMessage(availableLangs);
			sender.sendMessage(usage);
			sender.sendMessage(footer);
			
			return;
		}
		
		// 切换语言
		if (args.length == 2) {
			String newLang = args[1];
			
			// 检查语言是否可用
			if (!languageManager.getAvailableLanguages().contains(newLang)) {
				String invalidLangMsg = plugin.getConfigManager().getMessage("invalid_language")
						.replace("%language%", newLang)
						.replace("%languages%", String.join(", ", languageManager.getAvailableLanguages()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidLangMsg));
				return;
			}
			
			// 先获取消息模板，因为语言切换后模板会改变
			String messageTemplate = plugin.getConfigManager().getMessage("language_changed");
			
			// 设置新语言
			languageManager.setLanguage(newLang);
			
			// 完全重新加载配置
			plugin.reloadPlugin();
			
			// 使用已保存的消息模板，替换变量
			String message = messageTemplate.replace("%language%", newLang);
			
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			
			// 再发送一次当前语言的版本信息
			String currentVersionInfo = plugin.getConfigManager().getMessage("version_info")
					.replace("%version%", plugin.getDescription().getVersion());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', currentVersionInfo));
			
			return;
		}
		
		// 错误的用法
		String usageMessage = plugin.getConfigManager().getMessage("command_usage")
				.replace("%s", "/picturelogin language <语言代码>");
		sender.sendMessage(usageMessage);
	}
}
