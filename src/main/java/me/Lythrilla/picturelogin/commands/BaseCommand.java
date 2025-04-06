package me.Lythrilla.picturelogin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;

import static me.Lythrilla.picturelogin.util.Translate.tl;

public class BaseCommand implements CommandExecutor {
	private final PictureLogin plugin;

	// TODO: Remove plugin
	public BaseCommand(PictureLogin plugin) {
	  this.plugin = plugin;
	}
	  
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		// Permission Check
		if (!s.hasPermission("picturelogin.main")) {
			s.sendMessage(plugin.getConfigManager().getMessage("no_permission"));
			return false;
		}

		// 检查命令是否为 /picturelogin reload
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			plugin.getConfigManager().reloadConfig();
			s.sendMessage(plugin.getConfigManager().getMessage("reload_config"));
			return true;
		}
		
		// 检查命令是否为 /picturelogin language <lang>
		if (args.length >= 1 && args[0].equalsIgnoreCase("language")) {
			LanguageManager languageManager = plugin.getConfigManager().getLanguageManager();
			
			// 显示当前语言和可用语言
			if (args.length == 1) {
				s.sendMessage(ChatColor.GREEN + "当前语言: " + ChatColor.WHITE + languageManager.getCurrentLanguage());
				s.sendMessage(ChatColor.GREEN + "可用语言: " + ChatColor.WHITE + String.join(", ", languageManager.getAvailableLanguages()));
				s.sendMessage(ChatColor.GRAY + "使用 /picturelogin language <lang> 切换语言");
				return true;
			}
			
			// 切换语言
			if (args.length == 2) {
				String newLang = args[1];
				languageManager.setLanguage(newLang);
				
				// 替换变量
				String message = plugin.getConfigManager().getMessage("language_changed")
						.replace("%language%", newLang);
				
				s.sendMessage(message);
				return true;
			}
		}
		
		// 显示版本信息
		String versionInfo = plugin.getConfigManager().getMessage("version_info")
				.replace("%version%", plugin.getDescription().getVersion());
		
		s.sendMessage(versionInfo);
		s.sendMessage(plugin.getConfigManager().getMessage("reload_config_help"));

		return true;
	}
}
