package me.Lythrilla.picturelogin.util;

import me.Lythrilla.picturelogin.config.ConfigManager;
import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

public class Hooks {
    private PluginManager plugins;
    private ConfigManager config;
    private Logger logger;

    public static boolean AUTHME;
    public static boolean PLACEHOLDER_API;
     public static boolean SKINSRESTORER;

    public Hooks(PluginManager plugins, ConfigManager config, Logger logger) {
        this.plugins = plugins;
        this.config = config;
        this.logger = logger;

        hookAuthMe();
        hookPlaceHolderAPI();
        hookSkinsRestorer();
    }

    private boolean hookPlugin(String plugin) {
        if (!plugins.isPluginEnabled(plugin))
            return false;

        // Make sure user wants to hook into the plugin
        if (!config.getBoolean("hooks." + plugin, true))
            return false;

        logger.info(() -> "Hooked into: " + plugin);
        return true;
    }

    private void hookAuthMe() {
        AUTHME = hookPlugin("AuthMe");
    }

    private void hookPlaceHolderAPI() {
        PLACEHOLDER_API = hookPlugin("PlaceholderAPI");
    }
    
    private void hookSkinsRestorer() {
        SKINSRESTORER = hookPlugin("SkinsRestorer");
    }
}
