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
	
	// Cache for player ASCII avatar lines
	private final Map<UUID, String[]> avatarLinesCache = new HashMap<>();
	// Cache expiration time (milliseconds)
	private static final long CACHE_EXPIRE_TIME = 1600000; 
	// Cache last update time
	private final Map<UUID, Long> cacheLastUpdate = new HashMap<>();
	// Add offline player avatar cache
	private final Map<String, String[]> offlinePlayerAvatarCache = new HashMap<>();
	private final Map<String, Long> offlinePlayerCacheLastUpdate = new HashMap<>();
	// For marking offline players that are currently loading
	private final Map<String, Boolean> loadingOfflinePlayers = new HashMap<>();

	public PictureUtil(PictureLogin plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfigManager();
	}

	private URL newURL(String player_uuid, String player_name) {
		String url;
		
		// Check if server is in offline mode
		boolean isOfflineMode = !Bukkit.getServer().getOnlineMode();
		
		// SkinsRestorer integration
		if (Hooks.SKINSRESTORER && PictureLogin.skinsRestorerAPI != null) {
			try {
				var skinData = PictureLogin.skinsRestorerAPI.getSkinStorage().findSkinData(player_name);
				if (skinData.isPresent()) {
					// If SkinsRestorer data exists, use the skin value
					return new URL("https://minepic.org/avatar/8/" + skinData.get().getProperty().getValue());
				}
			} catch (Exception e) {
				plugin.getLogger().warning("Error getting SkinsRestorer skin data: " + e.getMessage());
				// Continue with regular URL on error
			}
		}
		
		// Use URL from config file, replace variables based on server mode
		url = config.getURL();
		
		if (isOfflineMode) {
			// Offline mode prioritizes player name
			url = url.replace("%pname%", player_name);
			// If URL contains %uuid% but not %pname%, replace %uuid% with player name
			if (url.contains("%uuid%") && !url.contains("%pname%")) {
				url = url.replace("%uuid%", player_name);
			} else {
				url = url.replace("%uuid%", player_uuid);
			}
		} else {
			// Online mode normal replacement
			url = url.replace("%uuid%", player_uuid)
				   .replace("%pname%", player_name);
		}

		try {
			return new URL(url);
		} catch (Exception e) {
			plugin.getLogger().warning("Unable to parse avatar URL: " + e.getMessage());
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
                connection.setConnectTimeout(5000); // 5 second connection timeout
                connection.setReadTimeout(5000);    // 5 second read timeout
                
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning(tl("error_retrieving_avatar") + " - Server returned status code: " + responseCode + " URL: " + head_image);
                    return null;
                }
                
                return ImageIO.read(connection.getInputStream());
            } catch (Exception e) {
            	String errorMsg = e.getMessage();
            	if (errorMsg == null) errorMsg = e.getClass().getName();
                plugin.getLogger().warning(tl("error_retrieving_avatar") + " - Reason: " + errorMsg + " URL: " + head_image);
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
			plugin.getLogger().warning(tl("error_fallback_img") + " - Reason: " + errorMsg);
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
	 * Add placeholders to messages
	 *
	 * @param msg Message to process
	 * @param player Player instance
	 * @return Message with placeholders replaced
	 */
	public String addPlaceholders(String msg, Player player) {
		if (msg == null) return "";
		
		// Check if it's MiniMessage format
		boolean isMiniMessage = msg.startsWith("<") && msg.contains(">");
		
		// Replace regular placeholders
		msg = msg.replace("%player%", player.getName());
		msg = msg.replace("%pname%", player.getName());
		msg = msg.replace("%uuid%", player.getUniqueId().toString());
		msg = msg.replace("%online%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
		msg = msg.replace("%max%", String.valueOf(plugin.getServer().getMaxPlayers()));
		msg = msg.replace("%motd%", plugin.getServer().getMotd());
		msg = msg.replace("%displayname%", player.getDisplayName());
		
		// Add PlaceholderAPI support
		if (Hooks.PLACEHOLDER_API && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			msg = PlaceholderAPI.setPlaceholders(player, msg);
		}
		
		// If not MiniMessage format, use traditional color codes
		if (!isMiniMessage) {
			msg = ChatColor.translateAlternateColorCodes('&', msg);
		}
		
		return msg;
	}
	
	/**
	 * Convert MiniMessage format to legacy color codes
	 * 
	 * @param input Text in MiniMessage format
	 * @return Text in legacy color code format
	 */
	private String convertMiniMessageToLegacy(String input) {
		// Simple conversion of common MiniMessage tags
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
		
		// Convert gradient and rainbow effects to simple colors
		input = input.replaceAll("<gradient:[^>]*>", "&e");
		input = input.replace("</gradient>", "&r");
		input = input.replace("<rainbow>", "&e");
		input = input.replace("</rainbow>", "&r");
		
		// Process traditional color codes after conversion
		input = ChatColor.translateAlternateColorCodes('&', input);
		
		return input;
	}

	public void clearChat(Player player) {
		for (int i = 0; i < 20; i++) {
			player.sendMessage("");
		}
	}

	/**
	 * Get a specified line of the player's ASCII avatar
	 * 
	 * @param player Player object
	 * @param lineNumber Line number (starting from 1)
	 * @return The specified line of the avatar, empty string if invalid
	 */
	public String getAvatarLine(Player player, int lineNumber) {
		// Ensure line number is valid
		if (lineNumber < 1) {
			return "";
		}
		
		// Check if cache exists and is valid
		UUID playerUUID = player.getUniqueId();
		if (avatarLinesCache.containsKey(playerUUID) && 
			System.currentTimeMillis() - cacheLastUpdate.getOrDefault(playerUUID, 0L) < CACHE_EXPIRE_TIME) {
			String[] lines = avatarLinesCache.get(playerUUID);
			if (lineNumber <= lines.length) {
				String line = lines[lineNumber - 1];
				// Ensure color codes are properly processed
				return ChatColor.translateAlternateColorCodes('&', line);
			}
			return "";
		}
		
		// Generate new avatar
		BufferedImage image = getImage(player);
		if (image == null) {
			return "";
		}
		
		// Create image message
		ImageMessage imageMessage = new ImageMessage(image, 8, ImageChar.BLOCK.getChar());
		String[] lines = imageMessage.getLines();
		
		// Cache the result
		avatarLinesCache.put(playerUUID, lines);
		cacheLastUpdate.put(playerUUID, System.currentTimeMillis());
		
		// Return the requested line
		if (lineNumber <= lines.length) {
			String line = lines[lineNumber - 1];
			// Ensure color codes are properly processed
			return ChatColor.translateAlternateColorCodes('&', line);
		}
		return "";
	}
	
	/**
	 * Clear avatar cache for a player
	 * 
	 * @param player Player to clear cache for
	 */
	public void clearAvatarCache(Player player) {
		UUID playerUUID = player.getUniqueId();
		avatarLinesCache.remove(playerUUID);
		cacheLastUpdate.remove(playerUUID);
	}
	
	/**
	 * Clear all caches
	 */
	public void clearAllAvatarCaches() {
		avatarLinesCache.clear();
		cacheLastUpdate.clear();
		offlinePlayerAvatarCache.clear();
		offlinePlayerCacheLastUpdate.clear();
		loadingOfflinePlayers.clear();
	}

	/**
	 * Asynchronously get a specified line of avatar
	 * 
	 * @param playerName Player name
	 * @param lineNumber Line number (starting from 1)
	 * @return The specified line of the avatar, empty string if invalid
	 */
	public String getAvatarLineByName(String playerName, int lineNumber) {
		// Try to find the specified player from online players
		Player player = Bukkit.getPlayer(playerName);
		if (player != null) {
			// If player is online, use the existing method
			return getAvatarLine(player, lineNumber);
		}
		
		// Ensure line number is valid
		if (lineNumber < 1) {
			return "";
		}
		
		// Check if offline player cache exists and is valid
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
		
		// If currently loading, return a temporary prompt
		if (Boolean.TRUE.equals(loadingOfflinePlayers.get(lowerPlayerName))) {
			return "&7Loading...";
		}
		
		// Asynchronously get the avatar
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
