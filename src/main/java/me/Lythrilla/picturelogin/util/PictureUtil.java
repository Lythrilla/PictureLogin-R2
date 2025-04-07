package me.Lythrilla.picturelogin.util;

import static me.Lythrilla.picturelogin.util.Translate.tl;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import me.Lythrilla.picturelogin.util.ImageMessage;
import me.clip.placeholderapi.PlaceholderAPI;
import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.config.FallbackPicture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bobacadodl.imgmessage.ImageChar;

public class PictureUtil {
	private final PictureLogin plugin;
	private ConfigManager config;
	
	// 缓存玩家的ASCII头像行
	private final Map<UUID, String[]> avatarLinesCache = new HashMap<>();
	// 缓存过期时间（毫秒）
	private static final long CACHE_EXPIRE_TIME = 60000; // 1分钟
	// 缓存最后更新时间
	private final Map<UUID, Long> cacheLastUpdate = new HashMap<>();
	// 添加离线玩家头像缓存
	private final Map<String, String[]> offlinePlayerAvatarCache = new HashMap<>();
	private final Map<String, Long> offlinePlayerCacheLastUpdate = new HashMap<>();
	// 用于标记正在加载中的离线玩家
	private final Map<String, Boolean> loadingOfflinePlayers = new HashMap<>();

	public PictureUtil(PictureLogin plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfigManager();
	}

	private URL newURL(String player_uuid, String player_name) {
		String url;
		
		// 检查服务器是否为离线模式
		boolean isOfflineMode = !Bukkit.getServer().getOnlineMode();
		
		// SkinsRestorer集成
		if (Hooks.SKINSRESTORER && PictureLogin.skinsRestorerAPI != null) {
			try {
				var skinData = PictureLogin.skinsRestorerAPI.getSkinStorage().findSkinData(player_name);
				if (skinData.isPresent()) {
					// 如果有SkinsRestorer数据，使用皮肤值
					return new URL("https://minepic.org/avatar/8/" + skinData.get().getProperty().getValue());
				}
			} catch (Exception e) {
				plugin.getLogger().warning("获取SkinsRestorer皮肤数据时出错: " + e.getMessage());
				// 出错时继续使用常规URL
			}
		}
		
		// 使用配置文件中的URL，根据服务器模式替换变量
		url = config.getURL();
		
		if (isOfflineMode) {
			// 离线模式优先使用玩家名
			url = url.replace("%pname%", player_name);
			// 如果URL中有%uuid%但没有%pname%，用玩家名替换%uuid%
			if (url.contains("%uuid%") && !url.contains("%pname%")) {
				url = url.replace("%uuid%", player_name);
			} else {
				url = url.replace("%uuid%", player_uuid);
			}
		} else {
			// 在线模式正常替换
			url = url.replace("%uuid%", player_uuid)
				   .replace("%pname%", player_name);
		}

		try {
			return new URL(url);
		} catch (Exception e) {
			plugin.getLogger().warning("无法解析头像URL: " + e.getMessage());
			return null;
		}
	}

	private BufferedImage getImage(Player player) {
		URL head_image = newURL(player.getUniqueId().toString(), player.getName());

		// URL Formatted correctly.
		if (head_image != null) {
            try {
            	//User-Agent is needed for HTTP requests
            	HttpURLConnection connection = (HttpURLConnection) head_image.openConnection();
            	connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
                connection.setConnectTimeout(5000); // 5秒连接超时
                connection.setReadTimeout(5000);    // 5秒读取超时
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning(tl("error_retrieving_avatar") + " - 服务器返回状态码: " + responseCode + " URL: " + head_image);
                    return null;
                }
                
                return ImageIO.read(connection.getInputStream());
            } catch (Exception e) {
            	String errorMsg = e.getMessage();
            	if (errorMsg == null) errorMsg = e.getClass().getName();
                plugin.getLogger().warning(tl("error_retrieving_avatar") + " - 原因: " + errorMsg + " URL: " + head_image);
            }
		} else {
		    plugin.getLogger().warning(tl("error_avatar_url_null"));
		}

		// Incorrectly formatted URL or couldn't load from URL
		try {
			plugin.getLogger().info(tl("using_fallback_img"));
			return ImageIO.read(new FallbackPicture(plugin).get());
		} catch (Exception e) {
			String errorMsg = e.getMessage();
			if (errorMsg == null) errorMsg = e.getClass().getName();
			plugin.getLogger().warning(tl("error_fallback_img") + " - 原因: " + errorMsg);
			return null;
		}
	}

	public ImageMessage createPictureMessage(Player player, List<String> messages) {
		BufferedImage image = getImage(player);

		if (image == null) return null;

		messages.replaceAll((message) -> addPlaceholders(message, player));

		return config.getMessage(messages, image);
	}

	public void sendOutPictureMessage(ImageMessage picture_message) {
		plugin.getServer().getOnlinePlayers().forEach((online_player) -> {
			if (config.getBoolean("clear-chat", false))
				clearChat(online_player);

			picture_message.sendToPlayer(online_player);
		});
	}

	// String Utility Functions

	/**
	 * 添加消息中的占位符
	 *
	 * @param msg 待处理的消息
	 * @param player 玩家实例
	 * @return 添加了占位符的消息
	 */
	public String addPlaceholders(String msg, Player player) {
		if (msg == null) return "";
		
		// 检查是否为MiniMessage格式
		boolean isMiniMessage = msg.startsWith("<") && msg.contains(">");
		
		// 替换常规占位符
		msg = msg.replace("%player%", player.getName());
		msg = msg.replace("%pname%", player.getName());
		msg = msg.replace("%uuid%", player.getUniqueId().toString());
		msg = msg.replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
		msg = msg.replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers()));
		msg = msg.replace("%motd%", plugin.getServer().getMotd());
		msg = msg.replace("%displayname%", player.getDisplayName());
		
		// 添加PlaceholderAPI支持
		if (Hooks.PLACEHOLDER_API && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			msg = PlaceholderAPI.setPlaceholders(player, msg);
		}
		
		// 如果不是MiniMessage格式，使用传统的颜色代码处理
		if (!isMiniMessage) {
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		}
		
		return msg;
	}
	
	/**
	 * 将MiniMessage格式转换为传统颜色代码
	 * 
	 * @param input MiniMessage格式的文本
	 * @return 传统颜色代码格式的文本
	 */
	private String convertMiniMessageToLegacy(String input) {
		// 简单转换一些常见的MiniMessage标签
		input = input.replace("<gray>", "&7");
		input = input.replace("</gray>", "&r");
		input = input.replace("<yellow>", "&e");
		input = input.replace("</yellow>", "&r");
		input = input.replace("<green>", "&a");
		input = input.replace("</green>", "&r");
		input = input.replace("<bold>", "&l");
		input = input.replace("</bold>", "&r");
		input = input.replace("<red>", "&c");
		input = input.replace("</red>", "&r");
		input = input.replace("<blue>", "&9");
		input = input.replace("</blue>", "&r");
		input = input.replace("<aqua>", "&b");
		input = input.replace("</aqua>", "&r");
		input = input.replace("<gold>", "&6");
		input = input.replace("</gold>", "&r");
		input = input.replace("<white>", "&f");
		input = input.replace("</white>", "&r");
		input = input.replace("<black>", "&0");
		input = input.replace("</black>", "&r");
		
		// 将渐变和彩虹效果转换为简单的颜色
		input = input.replaceAll("<gradient:[^>]*>", "&e");
		input = input.replace("</gradient>", "&r");
		input = input.replace("<rainbow>", "&e");
		input = input.replace("</rainbow>", "&r");
		
		// 转换后处理传统颜色代码
		input = ChatColor.translateAlternateColorCodes('&', input);
		
		return input;
	}

	public void clearChat(Player player) {
		for (int i = 0; i < 20; i++) {
			player.sendMessage("");
		}
	}

	/**
	 * 获取玩家ASCII头像的指定行
	 * 
	 * @param player 玩家对象
	 * @param lineNumber 行号（从1开始）
	 * @return 头像的指定行，如果无效则返回空字符串
	 */
	public String getAvatarLine(Player player, int lineNumber) {
		// 确保行号有效
		if (lineNumber < 1) {
			return "";
		}
		
		// 检查缓存是否存在且有效
		UUID playerUUID = player.getUniqueId();
		if (avatarLinesCache.containsKey(playerUUID) && 
			System.currentTimeMillis() - cacheLastUpdate.getOrDefault(playerUUID, 0L) < CACHE_EXPIRE_TIME) {
			String[] lines = avatarLinesCache.get(playerUUID);
			if (lineNumber <= lines.length) {
				String line = lines[lineNumber - 1];
				// 确保颜色代码被正确处理
				return ChatColor.translateAlternateColorCodes('&', line);
			}
			return "";
		}
		
		// 生成新的头像
		BufferedImage image = getImage(player);
		if (image == null) {
			return "";
		}
		
		// 创建图像消息
		ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
		String[] lines = imageMessage.getLines();
		
		// 缓存结果
		avatarLinesCache.put(playerUUID, lines);
		cacheLastUpdate.put(playerUUID, System.currentTimeMillis());
		
		// 返回请求的行
		if (lineNumber <= lines.length) {
			String line = lines[lineNumber - 1];
			// 确保颜色代码被正确处理
			return ChatColor.translateAlternateColorCodes('&', line);
		}
		return "";
	}
	
	/**
	 * 清除玩家的头像缓存
	 * 
	 * @param player 要清除缓存的玩家
	 */
	public void clearAvatarCache(Player player) {
		UUID playerUUID = player.getUniqueId();
		avatarLinesCache.remove(playerUUID);
		cacheLastUpdate.remove(playerUUID);
	}
	
	/**
	 * 清除所有缓存
	 */
	public void clearAllAvatarCaches() {
		avatarLinesCache.clear();
		cacheLastUpdate.clear();
		offlinePlayerAvatarCache.clear();
		offlinePlayerCacheLastUpdate.clear();
		loadingOfflinePlayers.clear();
	}

	/**
	 * 异步获取头像的指定行
	 * 
	 * @param playerName 玩家名
	 * @param lineNumber 行号（从1开始）
	 * @return 头像的指定行，如果无效则返回空字符串
	 */
	public String getAvatarLineByName(String playerName, int lineNumber) {
		// 尝试从在线玩家中找到指定玩家
		Player player = Bukkit.getPlayer(playerName);
		if (player != null) {
			// 如果玩家在线，直接使用现有方法
			return getAvatarLine(player, lineNumber);
		}
		
		// 确保行号有效
		if (lineNumber < 1) {
			return "";
		}
		
		// 检查离线玩家缓存是否存在且有效
		String lowerPlayerName = playerName.toLowerCase();
		if (offlinePlayerAvatarCache.containsKey(lowerPlayerName) && 
		    System.currentTimeMillis() - offlinePlayerCacheLastUpdate.getOrDefault(lowerPlayerName, 0L) < CACHE_EXPIRE_TIME) {
			String[] lines = offlinePlayerAvatarCache.get(lowerPlayerName);
			if (lineNumber <= lines.length) {
				String line = lines[lineNumber - 1];
				return ChatColor.translateAlternateColorCodes('&', line);
			}
			return "";
		}
		
		// 如果正在加载中，返回临时提示符
		if (Boolean.TRUE.equals(loadingOfflinePlayers.get(lowerPlayerName))) {
			return "&7Loading...";
		}
		
		// 异步获取头像
		loadingOfflinePlayers.put(lowerPlayerName, true);
		
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				// Create URL (using player name instead of UUID)
				URL head_image = newURL(playerName, playerName);
				
				// Get avatar image
				BufferedImage image = null;
				if (head_image != null) {
					try {
						HttpURLConnection connection = (HttpURLConnection) head_image.openConnection();
						connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
						connection.setConnectTimeout(5000); // 5 second connection timeout
						connection.setReadTimeout(5000);    // 5 second read timeout
						
						int responseCode = connection.getResponseCode();
						if (responseCode != 200) {
							plugin.getLogger().warning(tl("error_retrieving_avatar") + " - Server returned status code: " + responseCode + " Player: " + playerName + " URL: " + head_image);
							// Don't exit immediately, try using fallback image
						} else {
							image = ImageIO.read(connection.getInputStream());
						}
					} catch (Exception e) {
						String errorMsg = e.getMessage();
						if (errorMsg == null) errorMsg = e.getClass().getName();
						plugin.getLogger().warning(tl("error_retrieving_avatar") + " - Reason: " + errorMsg + " Player: " + playerName + " URL: " + head_image);
					}
				} else {
					plugin.getLogger().warning(tl("error_avatar_url_null") + " Player: " + playerName);
				}
				
				// If failed to get image, use fallback image
				if (image == null) {
					try {
						plugin.getLogger().info(tl("using_fallback_img") + " Player: " + playerName);
						image = ImageIO.read(new FallbackPicture(plugin).get());
					} catch (Exception e) {
						String errorMsg = e.getMessage();
						if (errorMsg == null) errorMsg = e.getClass().getName();
						plugin.getLogger().warning(tl("error_fallback_img") + " - Reason: " + errorMsg + " Player: " + playerName);
						loadingOfflinePlayers.remove(lowerPlayerName);
						return;
					}
				}
				
				// Create image message
				ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
				String[] lines = imageMessage.getLines();
				
				// Cache the result
				offlinePlayerAvatarCache.put(lowerPlayerName, lines);
				offlinePlayerCacheLastUpdate.put(lowerPlayerName, System.currentTimeMillis());
				
				// Loading complete
				loadingOfflinePlayers.remove(lowerPlayerName);
				plugin.getLogger().info("Successfully loaded player avatar asynchronously: " + playerName);
			} catch (Exception e) {
				// Remove loading flag if exception occurs
				loadingOfflinePlayers.remove(lowerPlayerName);
				String errorMsg = e.getMessage();
				if (errorMsg == null) errorMsg = e.getClass().getName();
				plugin.getLogger().warning("Failed to get avatar for player " + playerName + ": " + errorMsg);
			}
		});
		
		// First call returns loading prompt
		return "&7Loading...";
	}

}
