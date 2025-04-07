package me.Lythrilla.picturelogin.config;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

import com.bobacadodl.imgmessage.ImageChar;
import me.Lythrilla.picturelogin.util.ImageMessage;
import me.Lythrilla.picturelogin.PictureLogin;

public class ConfigManager {
	private final PictureLogin plugin;
	private LanguageManager languageManager;
	private YamlConfiguration config;
	private UserManager userManager;
	private PermissionManager permissionManager;
	
	public ConfigManager(PictureLogin plugin) {
		this.plugin = plugin;

		// 加载主配置文件
		File configFile = new File(plugin.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			plugin.saveResource("config.yml", false);
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		
		// 初始化语言管理器
		languageManager = new LanguageManager(plugin, this);
		
		// 这里不初始化userManager和permissionManager，由PictureLogin主类负责初始化
	}
	
	/**
	 * 重新加载配置文件
	 */
	public void reloadConfig() {
		try {
			// 重新加载主配置
			File configFile = new File(plugin.getDataFolder(), "config.yml");
			if (!configFile.exists()) {
				plugin.saveResource("config.yml", false);
			}
			config = YamlConfiguration.loadConfiguration(configFile);
			
			// 重新创建语言管理器，以确保完全刷新
			if (languageManager != null) {
				languageManager = new LanguageManager(plugin, this);
			} else {
				languageManager = new LanguageManager(plugin, this);
			}
			
			// 再次设置语言以确保配置生效
			String language = getString("language", "en_US");
			languageManager.setLanguage(language);
			
			// 重新加载用户配置
			if (userManager != null) {
				userManager.loadConfig();
			}
			
			// 重新加载权限配置
			if (permissionManager != null) {
				permissionManager.loadConfig();
			}
			
			// 使用新的语言管理器获取消息
			plugin.getLogger().info(languageManager.getMessage("log_plugin_reloaded"));
		} catch (Exception e) {
			plugin.getLogger().severe("重新加载配置时出错: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private char getChar() {
		try {
			return ImageChar.valueOf(config.getString("character").toUpperCase()).getChar();
		} catch (IllegalArgumentException e) {
			return ImageChar.BLOCK.getChar();
		}
	}
	
	public ImageMessage getMessage(List<String> messages, BufferedImage image) {
		int imageDimensions = 8, count = 0;
		ImageMessage imageMessage = new ImageMessage(image, imageDimensions, getChar());
		String[] msg = new String[imageDimensions];

		for (String message : messages) {
			if (count > msg.length) break;
			msg[count++] = message;
		}

		while (count < imageDimensions) {
			msg[count++] = "";
		}

		if (config.getBoolean("center-text", false))
			return imageMessage.appendCenteredText(msg);

		return imageMessage.appendText(msg);
	}

	public boolean getBoolean(String key) {
		return config.getBoolean(key);
	}

	public boolean getBoolean(String key, Boolean def) { 
		return config.getBoolean(key, def); 
	}

	public List<String> getStringList(String key) {
		return config.getStringList(key);
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String def) {
		return config.getString(key, def);
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public String getMessage(String key) {
		// 确保languageManager已初始化
		if (languageManager == null) {
			languageManager = new LanguageManager(plugin, this);
		}
		return languageManager.getMessage(key);
	}

	public String getURL() {
		String url = config.getString("url");

		if (url == null) {
			plugin.getLogger().log(Level.SEVERE, languageManager.getMessage("log_url_error"));

			return "https://minepic.org/avatar/8/%uuid%";
		}

		return url;
	}

	/**
	 * 获取登录消息列表
	 */
	public List<String> getMessages() {
		return getMessageList("messages", new ArrayList<>());
	}

	/**
	 * 获取首次登录消息列表
	 */
	public List<String> getFirstJoinMessages() {
		return getMessageList("first-join-messages", new ArrayList<>());
	}

	/**
	 * 获取离开消息列表
	 */
	public List<String> getLeaveMessages() {
		return getMessageList("leave-messages", new ArrayList<>());
	}

	/**
	 * 从配置文件中获取消息列表
	 * 
	 * @param path 配置路径
	 * @param defaultValue 默认值
	 * @return 消息列表
	 */
	private List<String> getMessageList(String path, List<String> defaultValue) {
		if (!config.contains(path)) {
			return defaultValue;
		}
		
		List<String> messages = config.getStringList(path);
		return messages.isEmpty() ? defaultValue : messages;
	}

	// 设置UserManager和PermissionManager的引用
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}

	/**
	 * 检查是否启用登录音效
	 */
	public boolean isPlayLoginSound() {
		return config.getBoolean("play-login-sound", true);
	}
	
	/**
	 * 获取默认登录音效
	 * 
	 * @return 默认音效名称
	 */
	public String getLoginSound() {
		return config.getString("login-sound.sound", "ENTITY_PLAYER_LEVELUP");
	}
	
	/**
	 * 获取默认音效音量
	 * 
	 * @return 默认音效音量
	 */
	public float getLoginSoundVolume() {
		return (float) config.getDouble("login-sound.volume", 1.0);
	}
	
	/**
	 * 获取默认音效音调
	 * 
	 * @return 默认音效音调
	 */
	public float getLoginSoundPitch() {
		return (float) config.getDouble("login-sound.pitch", 1.0);
	}

	public UserManager getUserManager() {
		return userManager;
	}
	
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}
}
