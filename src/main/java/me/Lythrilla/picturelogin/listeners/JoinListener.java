package me.Lythrilla.picturelogin.listeners;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.Lythrilla.picturelogin.util.Hooks;
import me.Lythrilla.picturelogin.util.PictureWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.ConfigManager;
import me.Lythrilla.picturelogin.util.PictureUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinListener implements Listener {
	private PictureLogin plugin;
	private PictureUtil pictureUtil;
	private ConfigManager config;
	private Player player;

	public JoinListener(PictureLogin plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfigManager();
		this.pictureUtil = plugin.getPictureUtil();
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent event) {
		this.player = event.getPlayer();

		// block the default join message
		if (config.getBoolean("block-join-message", false))
			event.setJoinMessage(null);

		if (Hooks.AUTHME) {
			authMeLogin();
			return;
		}

		sendImage();
	}

	private void authMeLogin() {
		new BukkitRunnable() {

			@Override
			public void run() {
				// Stop if player left the server
				if (player == null || !player.isOnline())
					this.cancel();

				// Check for authentication
				if (AuthMeApi.getInstance().isAuthenticated(player)) {
					sendImage();
					this.cancel();
				}
			}

		}.runTaskTimer(plugin, 0L, 20L);
	}

	private void sendImage() {
		PictureWrapper wrapper = new PictureWrapper(plugin, player);

		if (config.getBoolean("async", true))
			wrapper.runTaskAsynchronously(plugin);
		else
			wrapper.runTask(plugin);
	}
}
