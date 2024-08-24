package me.ryanhamshire.GPFlags.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final MiniMessage minimessage = MiniMessage.builder().build();

    @SuppressWarnings("all")
    public static String hexString(String message) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&", "").replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Component validatedHexComp(CommandSender sender, String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = parseNativeColorCodes(message);
        message = validateMinimessage(sender, message);
        return minimessage.deserialize(message);
    }

    public static Component hexComp(String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = parseNativeColorCodes(message);
        return minimessage.deserialize(message);
    }

    private static String parseHexColorCodes(String message) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&", "");

            message = message.replace(hexCode, "<color:" + replaceSharp + ">");
            matcher = pattern.matcher(message);
        }
        return message;
    }

    private static String parseNativeColorCodes(String message) {
        Pattern pattern = Pattern.compile("&[a-flmnokrA-F0-9]");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String colorCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = colorCode.replace("&", "");

            message = message.replace(colorCode, getNativeColor(replaceSharp.toCharArray()[0]));
            matcher = pattern.matcher(message);
        }
        return message;
    }

    public static Component fixColor(String message, String key, String value) {
        if (message.split(key).length == 0)
            return ChatUtil.hexComp(value);

        String colorMsg = message.split(key)[0];
        String color = colorMsg.substring(colorMsg.length() - 2);
        if (color.trim().isEmpty())
            return ChatUtil.hexComp(value);
        if (color.charAt(0) == '&')
            return ChatUtil.hexComp(color + value);
        else return ChatUtil.hexComp(value);
    }

    private static String getNativeColor(char color) {
        String reset = "<!b><!i><!obf><!u><!st>";
        String colorCode = "<color:white>";
        if (color == 'r') return reset + colorCode;
        switch (color) {
            case 'a':
                colorCode = "<color:green>";
            case 'b':
                colorCode = "<color:aqua>";
            case 'c':
                colorCode = "<color:red>";
            case 'd':
                colorCode = "<color:light_purple>";
            case 'e':
                colorCode = "<color:yellow>";
            case 'f':
                colorCode = "<color:white>";
            case '0':
                colorCode = "<color:black>";
            case '1':
                colorCode = "<color:dark_blue>";
            case '2':
                colorCode = "<color:dark_green>";
            case '3':
                colorCode = "<color:dark_aqua>";
            case '4':
                colorCode = "<color:dark_red>";
            case '5':
                colorCode = "<color:dark_purple>";
            case '6':
                colorCode = "<color:gold>";
            case '7':
                colorCode = "<color:gray>";
            case '8':
                colorCode = "<color:dark_gray>";
            case '9':
                colorCode = "<color:blue>";
            case 'l':
                colorCode = "<bold>";
            case 'm':
                colorCode = "<strikethrough>";
            case 'n':
                colorCode = "<u>";
            case 'o':
                colorCode = "<italic>";
            case 'k':
                colorCode = "<obf>";
        }
        switch (color) {
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'k':
                break;
            default:
                colorCode = reset + colorCode;
        }
        return colorCode;
    }

    private static String validateMinimessage(CommandSender sender, String message) {

        // Check if there is a link in the message without click-component
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;
            if(!sender.hasPermission("gpflags.messages.links")) {
                message = message.replace(matcher.group(), "");
                requiredChanges = true;
            } else {
                String link = message.substring(matcher.start(), matcher.end());
                message = message.replace(link, "<click:open_url:" + link + ">" + link + "</click>");
                matcher = pattern.matcher(message);
            }
            if (!requiredChanges) {
                break;
            }
        }

        // Check for click-components
        pattern = Pattern.compile("<click:.*?>");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;
            String clickComponent = message.substring(matcher.start(), matcher.end());
            if (clickComponent.contains("suggest_command") && !sender.hasPermission("gpflags.messages.click.suggest_command")) {
                message = message.replace(clickComponent, "");
                requiredChanges = true;
            }
            if (clickComponent.contains("run_command") && !sender.hasPermission("gpflags.messages.click.run_command")) {
                message = message.replace(clickComponent, "");
                requiredChanges = true;
            }
            if (clickComponent.contains("open_url") && !sender.hasPermission("gpflags.messages.click.open_url")) {
                message = message.replace(clickComponent, "");
                requiredChanges = true;
            }
            matcher = pattern.matcher(message);

            if (!requiredChanges) {
                break;
            }
        }

        // Check for hover-components
        pattern = Pattern.compile("<hover:.*?>");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;

            String hoverComponent = message.substring(matcher.start(), matcher.end());
            if (hoverComponent.contains("show_text") && !sender.hasPermission("gpflags.messages.hover.show_text")) {
                message = message.replace(hoverComponent, "");
                requiredChanges = true;
            }
            if (hoverComponent.contains("show_item") && !sender.hasPermission("gpflags.messages.hover.show_item")) {
                message = message.replace(hoverComponent, "");
                requiredChanges = true;
            }
            if (hoverComponent.contains("show_entity") && !sender.hasPermission("gpflags.messages.hover.show_entity")) {
                message = message.replace(hoverComponent, "");
                requiredChanges = true;
            }
            matcher = pattern.matcher(message);

            if (!requiredChanges) {
                break;
            }
        }

        // Check for hex-colors
        message = message.replace("<#", "<color:#");
        pattern = Pattern.compile("<color:#.*?>");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;
            String hexComponent = message.substring(matcher.start(), matcher.end());
            if (!sender.hasPermission("gpflags.messages.hex-colors")) {
                message = message.replace(hexComponent, "<color:white>");
                requiredChanges = true;
            }
            matcher = pattern.matcher(message);

            if (!requiredChanges) {
                break;
            }
        }

        // Check for colors
        message = ChatUtil.parseNativeColorCodes(message);
        pattern = Pattern.compile("<color:.*?>");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;
            String colorComponent = message.substring(matcher.start(), matcher.end());
            if (!sender.hasPermission("gpflags.messages.colors")) {
                message = message.replace(colorComponent, "");
                requiredChanges = true;
            }
            matcher = pattern.matcher(message);

            if (!requiredChanges) {
                break;
            }
        }

        return message;
    }
}
