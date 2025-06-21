package me.Lythrilla.picturelogin;

import me.Lythrilla.picturelogin.listeners.JoinListener;
import me.Lythrilla.picturelogin.listeners.QuitListener;
import me.Lythrilla.picturelogin.commands.BaseCommand;
import me.Lythrilla.picturelogin.commands.PictureLoginTabCompleter;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.MessageUtil;
import me.Lythrilla.picturelogin.util.PictureUtil;
import me.Lythrilla.picturelogin.util.Translate;
import me.Lythrilla.picturelogin.util.Updater;
import me.Lythrilla.picturelogin.config.UserManager;
import me.Lythrilla.picturelogin.config.PermissionManager;
import me.Lythrilla.picturelogin.message.MessageService;
import me.Lythrilla.picturelogin.placeholder.PictureLoginPlaceholder;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;
import org.bukkit.scheduler.BukkitTask;

public class PictureLogin extends JavaPlugin {
	private ConfigManager configManager;
	private PictureUtil pictureUtil;
	private BukkitAudiences adventure;
	private UserManager userManager;
	private PermissionManager permissionManager;
	private MessageService messageService;
	private BukkitTask announcerTask;
	private long startTime;
	
	public static SkinsRestorer skinsRestorerAPI;
	
	// 添加静态实例变量和访问器方法
	private static PictureLogin instance;
	
	public static PictureLogin getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		// 设置静态实例
		instance = this;
		
		startTime = System.currentTimeMillis();
		
		displayStartupArt();
		
		// 初始化配置
		this.configManager = new ConfigManager(this);
		
		// 初始化自定义消息系统
		this.userManager = new UserManager(this);
		
		this.permissionManager = new PermissionManager(this);
		
		// 设置引用关系，让ConfigManager能够访问这些管理器
		this.configManager.setUserManager(userManager);
		this.configManager.setPermissionManager(permissionManager);
		
		// 输出日志消息
		this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_players_loaded").replace("%count%", String.valueOf(userManager.getUserCount())));
		this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_permissions_loaded").replace("%count%", String.valueOf(permissionManager.getGroupCount())));
		
		// 初始化Adventure API
		this.adventure = BukkitAudiences.create(this);

		// 初始化翻译类
		Translate.init(this);

		// Register Plugin Hooks
		new Hooks(getServer().getPluginManager(), configManager, getLogger());
		
		// 初始化消息服务
		this.messageService = new MessageService(this, configManager, userManager, permissionManager);
		
		// 初始化SkinsRestorer API
		if (Hooks.SKINSRESTORER) {
			skinsRestorerAPI = SkinsRestorerProvider.get();
		}

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
		// 注册Tab补全器
		getCommand("picturelogin").setTabCompleter(new PictureLoginTabCompleter(this));

		// 注册PlaceholderAPI扩展
		if (Hooks.PLACEHOLDER_API && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PictureLoginPlaceholder(this).register();
			getLogger().info(configManager.getLanguageManager().getMessage("log_papi_registered"));
		}

		// Update Checker
		if (configManager.getBoolean("update-check", true)) {
			this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_update_check_enabled").replace("%version%", getDescription().getVersion()));
			new Updater(getLogger(), getDescription().getVersion());
		} else {
			this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_update_check_disabled").replace("%version%", getDescription().getVersion()));
		}

		// bStats integration
		if (configManager.getBoolean("metrics", true)) {
			new Metrics(this, 25388);
		}
			
		// 输出插件启动信息
		long loadTime = System.currentTimeMillis() - startTime;
		this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_enabled").replace("%version%", getDescription().getVersion()) + " (" + loadTime + "ms)");
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
			"PictureLogin R2",
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
		if (this.adventure != null) {
			MessageUtil.shutdown();
			this.adventure.close();
			this.adventure = null;
		}
		
		// 添加configManager的null检查，防止NPE
		if (this.configManager != null && this.configManager.getLanguageManager() != null) {
			this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_disabled"));
		} else {
			this.getLogger().info("PictureLogin disabled!");
		}
	}

	/**
	 * 重新加载插件配置
	 */
	public void reloadPlugin() {
		// 重新加载主配置
		this.configManager.reloadConfig();
		
		// 重新创建消息服务，确保它使用新的语言设置
		if (this.messageService != null) {
			this.messageService = new MessageService(this, configManager, userManager, permissionManager);
		}
		
		// 重新初始化PictureUtil，确保它使用新的语言设置
		if (this.pictureUtil != null) {
			this.pictureUtil = new PictureUtil(this);
		}
		
		// 清除所有缓存
		if (this.pictureUtil != null) {
			this.pictureUtil.clearAllAvatarCaches();
		}
		
		this.getLogger().info(this.configManager.getLanguageManager().getMessage("log_plugin_reloaded"));
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public PictureUtil getPictureUtil() {
		return pictureUtil;
	}
	
	public BukkitAudiences adventure() {
		if(this.adventure == null) {
			throw new IllegalStateException(this.configManager.getLanguageManager().getMessage("log_adventure_error"));
		}
		return this.adventure;
	}
	
	public MessageService getMessageService() {
		return messageService;
	}
	
	public UserManager getUserManager() {
		return userManager;
	}
	
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}
}
