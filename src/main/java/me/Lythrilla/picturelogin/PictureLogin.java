package me.Lythrilla.picturelogin;

import me.Lythrilla.picturelogin.listeners.JoinListener;
import me.Lythrilla.picturelogin.listeners.QuitListener;
import me.Lythrilla.picturelogin.commands.BaseCommand;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.MessageUtil;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.Lythrilla.picturelogin.util.Translate;
import me.Lythrilla.picturelogin.util.Updater;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

public class PictureLogin extends JavaPlugin {
	private ConfigManager configManager;
	private PictureUtil pictureUtil;
	private BukkitAudiences adventure;

	@Override
	public void onEnable() {
		displayStartupArt();
		
		// 初始化Adventure API
		this.adventure = BukkitAudiences.create(this);

		// load config & languages file
		configManager = new ConfigManager(this);
		
		// 初始化翻译类
		Translate.init(this);

		// Register Plugin Hooks
		new Hooks(getServer().getPluginManager(), configManager, getLogger());

		// 初始化MiniMessage工具
		MessageUtil.init(adventure);

		// Initialize Picture Utility
		pictureUtil = new PictureUtil(this);

		// register Listeners
		getServer().getPluginManager().registerEvents(new JoinListener(this), this);

		// (only register leave listener if enabled in config)
		if (configManager.getBoolean("show-leave-message", false))
			getServer().getPluginManager().registerEvents(new QuitListener(this), this);

		// register /picturelogin command
		getCommand("picturelogin").setExecutor(new BaseCommand(this));

		// Update Checker
		if (configManager.getBoolean("update-check", true))
			new Updater(getLogger(), getDescription().getVersion());

		// bStats integration
		if (configManager.getBoolean("metrics", true)) {
			// bStats ID: 25388 (Lythrilla的官方ID)
			new Metrics(this, 25388);
		}
			
		getLogger().info("PictureLogin v" + getDescription().getVersion() + " 已启动!");
	}

	private void displayStartupArt() {
		String[] asciiArt = {
			"",
			" _____  _      _                     _                _       ",
			"|  __ \\(_)    | |                   | |              (_)      ",
			"| |__) |  ___| |_ _   _ _ __ ___   | |     ___   __ _ _ _ __ ",
			"|  ___/ |/ __| __| | | | '__/ _ \\  | |    / _ \\ / _` | | '_ \\",
			"| |   | | (__| |_| |_| | | |  __/  | |___| (_) | (_| | | | | |",
			"|_|   |_|\\___|\\__|\\__,_|_|  \\___|  |______\\___/ \\___|_|_| |_|",
			"",
			"PictureLogin R2 - 头像登陆重制版",
			"By Lythrilla | Original by NathanG",
			"GitHub: https://github.com/Lythrilla/PictureLogin-master",
			""
		};
		
		for (String line : asciiArt) {
			getLogger().info(line);
		}
	}

	@Override
	public void onDisable() {
		// 关闭Adventure API资源
		if(this.adventure != null) {
			MessageUtil.shutdown();
			this.adventure.close();
			this.adventure = null;
		}
		
		getLogger().info("PictureLogin 已停用!");
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public PictureUtil getPictureUtil() {
		return pictureUtil;
	}
	
	public BukkitAudiences adventure() {
		if(this.adventure == null) {
			throw new IllegalStateException("尝试在插件未初始化或已关闭时访问Adventure API");
		}
		return this.adventure;
	}
}
