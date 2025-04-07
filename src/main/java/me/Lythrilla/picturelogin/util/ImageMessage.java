package me.Lythrilla.picturelogin.util;

import de.themoep.minedown.MineDown;
import me.Lythrilla.picturelogin.PictureLogin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageMessage {
    private final static char TRANSPARENT_CHAR = ' ';
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('&')
            .hexCharacter('#')
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    
    // 颜色代码转换为MiniMessage格式的正则表达式
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)&#([0-9A-F]{6})");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORX])");
    
    private String[] lines;
    
    public ImageMessage(BufferedImage image, int height, char imgChar) {
        Color[][] chatColors = toChatColorArray(image, height);
        lines = toImgMessage(chatColors, imgChar);
    }

    private Color[][] toChatColorArray(BufferedImage image, int height) {
        double ratio = (double) image.getHeight() / image.getWidth();
        int width = (int) (height / ratio);
        if (width > 10) width = 10;
        BufferedImage resized = resizeImage(image, width, height);

        Color[][] chatImg = new Color[resized.getWidth()][resized.getHeight()];
        for (int x = 0; x < resized.getWidth(); x++) {
            for (int y = 0; y < resized.getHeight(); y++) {
                int rgb = resized.getRGB(x, y);
                chatImg[x][y] = new Color(rgb, true);
            }
        }
        return chatImg;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        AffineTransform af = new AffineTransform();
        af.scale(
            width / (double) originalImage.getWidth(),
            height / (double) originalImage.getHeight());

        AffineTransformOp operation = new AffineTransformOp(af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return operation.filter(originalImage, null);
    }

    private String[] toImgMessage(Color[][] colors, char imgchar) {
        lines = new String[colors[0].length];
        
        for (int y = 0; y < colors[0].length; y++) {
            StringBuilder line = new StringBuilder();
            for (int x = 0; x < colors.length; x++) {
                Color color = colors[x][y];
                // 转换为颜色代码
                if (color != null) {
                    // 使用hex颜色格式
                    line.append("&")
                        .append(colorToHex(colors[x][y]))
                        .append(imgchar);
                }
                else {
                    line.append(TRANSPARENT_CHAR);
                }
            }
            lines[y] = line.toString() + ChatColor.RESET;
        }
        
        return lines;
    }
    
    private String colorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public ImageMessage appendText(String... text) {
        for (int y = 0; y < lines.length; y++) {
            if (text.length > y) {
                lines[y] += " " + text[y];
            }
        }
        return this;
    }

    public ImageMessage appendCenteredText(String... text) {
        for (int y = 0; y < lines.length; y++) {
            if (text.length > y) {
                int len = ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH - lines[y].length();
                lines[y] = lines[y] + center(text[y], len);
            } else {
                return this;
            }
        }
        return this;
    }

    private String center(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        } else if (s.length() == length) {
            return s;
        } else {
            int leftPadding = (length - s.length()) / 2;
            StringBuilder leftBuilder = new StringBuilder();
            for (int i = 0; i < leftPadding; i++) {
                leftBuilder.append(" ");
            }
            return leftBuilder.toString() + s;
        }
    }

    /**
     * 将传统的颜色代码转换为MiniMessage格式
     */
    private String legacyToMiniMessage(String input) {
        // 转换十六进制颜色
        String result = HEX_COLOR_PATTERN.matcher(input).replaceAll(matchResult -> {
            String hex = matchResult.group(1);
            return "<#" + hex + ">";
        });
        
        // 转换常规颜色代码
        result = AMPERSAND_PATTERN.matcher(result).replaceAll(matchResult -> {
            String code = matchResult.group(1).toLowerCase();
            switch (code) {
                case "0": return "<black>";
                case "1": return "<dark_blue>";
                case "2": return "<dark_green>";
                case "3": return "<dark_aqua>";
                case "4": return "<dark_red>";
                case "5": return "<dark_purple>";
                case "6": return "<gold>";
                case "7": return "<gray>";
                case "8": return "<dark_gray>";
                case "9": return "<blue>";
                case "a": return "<green>";
                case "b": return "<aqua>";
                case "c": return "<red>";
                case "d": return "<light_purple>";
                case "e": return "<yellow>";
                case "f": return "<white>";
                case "k": return "<obfuscated>";
                case "l": return "<bold>";
                case "m": return "<strikethrough>";
                case "n": return "<underlined>";
                case "o": return "<italic>";
                case "r": return "<reset>";
                default: return "&" + code;
            }
        });
        
        return result;
    }

    public void sendToPlayer(Player player) {
        try {
            // 获取PictureLogin实例以访问Adventure API
            PictureLogin plugin = (PictureLogin) Bukkit.getPluginManager().getPlugin("PictureLogin");
            if (plugin == null) {
                // 如果无法获取插件实例，回退到传统方式
                sendToPlayerLegacy(player);
                return;
            }
            
            // 使用Adventure API发送消息
            for (String line : lines) {
                // 识别头像部分和文本部分
                String[] parts = line.split(" ", 2);
                
                // 处理头像部分（保持传统颜色代码格式）
                String avatarPart = parts[0];
                Component avatarComponent = LEGACY_SERIALIZER.deserialize(
                    ChatColor.translateAlternateColorCodes('&', avatarPart)
                );
                
                Component finalComponent;
                
                if (parts.length > 1) {
                    String textPart = parts[1];
                    Component textComponent;
                    
                    if (isMiniMessageFormat(textPart)) {
                        // 文本部分已经是MiniMessage格式
                        textComponent = MINI_MESSAGE.deserialize(textPart);
                    } else {
                        // 将传统颜色代码转换为MiniMessage格式后解析
                        String miniMessageText = legacyToMiniMessage(
                            ChatColor.translateAlternateColorCodes('&', textPart)
                        );
                        textComponent = MINI_MESSAGE.deserialize(miniMessageText);
                    }
                    
                    // 合并头像和文本组件
                    finalComponent = Component.empty()
                        .append(avatarComponent)
                        .append(Component.space())
                        .append(textComponent);
                } else {
                    // 只有头像部分
                    finalComponent = avatarComponent;
                }
                
                // 发送最终组件
                plugin.adventure().player(player).sendMessage(finalComponent);
            }
        } catch (Exception e) {
            // 出现任何错误都回退到传统方式
            e.printStackTrace();
            sendToPlayerLegacy(player);
        }
    }
    
    /**
     * 使用传统方式发送消息（当Adventure API不可用时使用）
     */
    private void sendToPlayerLegacy(Player player) {
        for (String line : lines) {
            player.spigot().sendMessage(MineDown.parse(line));
        }
    }
    
    /**
     * 判断文本是否使用MiniMessage格式
     */
    private boolean isMiniMessageFormat(String text) {
        return text.contains("<") && text.contains(">") && 
               (text.contains("<gray>") || text.contains("<yellow>") || 
                text.contains("<green>") || text.contains("<rainbow>") || 
                text.contains("<gradient") || text.contains("<#"));
    }

    /**
     * 获取图像消息的所有行
     * 
     * @return 包含图像的所有行的数组
     */
    public String[] getLines() {
        return lines;
    }
}
