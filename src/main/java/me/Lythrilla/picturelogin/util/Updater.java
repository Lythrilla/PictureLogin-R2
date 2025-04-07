package me.Lythrilla.picturelogin.util;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import me.Lythrilla.picturelogin.PictureLogin;
import me.Lythrilla.picturelogin.config.LanguageManager;

import static me.Lythrilla.picturelogin.util.Translate.tl;

public class Updater {

     public Updater(Logger log, String currentVersion) {
        final String USER_AGENT = "PictureLogin Plugin";
        
        try {
            // 暂时禁用更新检查，直到GitHub仓库有正式的releases
            if (PictureLogin.getInstance() != null && PictureLogin.getInstance().getConfigManager() != null) {
                LanguageManager langManager = PictureLogin.getInstance().getConfigManager().getLanguageManager();
                if (langManager != null) {
                    log.info(langManager.getMessage("log_update_check_disabled").replace("%version%", currentVersion));
                } else {
                    // 直接使用英文默认语言
                    log.info("PictureLogin " + currentVersion + " - Update check disabled");
                }
            } else {
                // 直接使用英文默认语言
                log.info("PictureLogin " + currentVersion + " - Update check disabled");
            }
            
            // 如果需要在后期开启更新检查，可以取消下面的注释
            /*
            // 使用GitHub API获取最新版本
            URL url = new URL("https://api.github.com/repos/Lythrilla/PictureLogin-master/releases/latest");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT);
            connection.addRequestProperty("Accept", "application/vnd.github.v3+json");
            
            // 读取下载的文件
            JsonObject jsonObject = new JsonParser().parse(new InputStreamReader(connection.getInputStream())).getAsJsonObject();

            // 从GitHub API获取tag_name作为版本号
            String latest_version = jsonObject.get("tag_name").toString().replace("\"", "");

            // 比较当前插件版本与下载的版本
            if (!currentVersion.equalsIgnoreCase(latest_version)) {
                log.info(tl("update_available").replace("%current%", currentVersion).replace("%new%", latest_version));
                log.info(tl("update_available_download"));
            }
            */
        } catch (Exception e) {
            log.warning(tl("error_update_check"));
            // 不显示详细错误信息，避免困扰用户
            // log.warning("错误详情: " + e.getMessage());
        }
    }

}
