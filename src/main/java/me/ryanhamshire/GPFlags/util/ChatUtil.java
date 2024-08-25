package me.ryanhamshire.GPFlags.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final MiniMessage minimessage = MiniMessage.builder().build();

    // Used to format chat-messages (legacy-support) of a specific sender related to his permissions
    public static String validatedHexString(CommandSender sender, String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = normalizeColorCodes(message);
        if (sender == null) return message;
        message = validateMinimessage(sender, message);
        return message;
    }

    // Used to format a chat-messages (legacy-support) without previous validation of somebody.
    public static Component hexComp(String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = normalizeColorCodes(message);
        return minimessage.deserialize(message);
    }

    // Parses legacy hex-colors to MiniMessage hex-colors
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

    // Normalized legacy color-codes and shortened Minimessage color-codes to written out MiniMessage color-codes
    private static String normalizeColorCodes(String message) {
        message = message.replace("&0", "<color:black>");
        message = message.replace("&1", "<color:dark_blue>");
        message = message.replace("&2", "<color:dark_green>");
        message = message.replace("&3", "<color:dark_aqua>");
        message = message.replace("&4", "<color:dark_red>");
        message = message.replace("&5", "<color:dark_purple>");
        message = message.replace("&6", "<color:gold>");
        message = message.replace("&7", "<color:gray>");
        message = message.replace("&8", "<color:dark_gray>");
        message = message.replace("&9", "<color:blue>");
        message = message.replace("&a", "<color:green>");
        message = message.replace("&b", "<color:aqua>");
        message = message.replace("&c", "<color:red>");
        message = message.replace("&d", "<color:light_purple>");
        message = message.replace("&e", "<color:yellow>");
        message = message.replace("&f", "<color:white>");
        message = message.replace("&l", "<bold>");
        message = message.replace("&m", "<strikethrough>");
        message = message.replace("&n", "<underlined>");
        message = message.replace("&o", "<italic>");
        message = message.replace("&k", "<obfuscated>");
        message = message.replace("&r", "<reset>");
        message = message.replace("<red>", "<color:red>");
        message = message.replace("<green>", "<color:green>");
        message = message.replace("<blue>", "<color:blue>");
        message = message.replace("<yellow>", "<color:yellow>");
        message = message.replace("<aqua>", "<color:aqua>");
        message = message.replace("<gold>", "<color:gold>");
        message = message.replace("<gray>", "<color:gray>");
        message = message.replace("<dark_gray>", "<color:dark_gray>");
        message = message.replace("<dark_red>", "<color:dark_red>");
        message = message.replace("<dark_green>", "<color:dark_green>");
        message = message.replace("<dark_blue>", "<color:dark_blue>");
        message = message.replace("<dark_aqua>", "<color:dark_aqua>");
        message = message.replace("<dark_purple>", "<color:dark_purple>");
        message = message.replace("<light_purple>", "<color:light_purple>");
        message = message.replace("<black>", "<color:black>");
        message = message.replace("<white>", "<color:white>");
        message = message.replace("<b>", "<bold>");
        message = message.replace("<u>", "<underlined>");
        message = message.replace("<i>", "<italic>");
        message = message.replace("<st>", "<strikethrough>");
        message = message.replace("<obf>", "<obfuscated>");
        return message;
    }

    // Validates a minimessage for a specific sender, meaning we check for permissions and remove components if necessary
    private static String validateMinimessage(CommandSender sender, String message) {

        /*
        // Check if there is a link in the message without click-component
        Pattern pattern = Pattern.compile("(?<!<click:open_url:)(https?://\\S+)(?!>)");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String link = message.substring(matcher.start(), matcher.end());
            message = message.replace(link, "<click:open_url:" + link + ">" + link + "</click>");
            matcher = pattern.matcher(message);
        }*/

        // Check if there are links
        Pattern pattern = Pattern.compile("https?://(?:www\\.)?[\\w\\-.]+\\.[a-z]{2,}(?:/[\\w\\-.,@?^=%&:/~+#]*)?");
        Matcher matcher = pattern.matcher(message);

        String pseudoMessage = message;
        while (matcher.find()) {
            String link = pseudoMessage.substring(matcher.start(), matcher.end());
            pseudoMessage = pseudoMessage.replace(link, "");
            if(message.contains("<click:open_url:" + link + ">")) continue; // Skip if already a click-component
            message = message.replace(link, "<click:open_url:" + link + ">" + link + "</click>");
            matcher = pattern.matcher(pseudoMessage);
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

        // Check for bold, italic, strikethrough, underline, obfuscated
        pattern = Pattern.compile("<(bold|italic|strikethrough|underlined|obfuscated)>");
        matcher = pattern.matcher(message);
        while (matcher.find()) {
            boolean requiredChanges = false;
            String styleComponent = message.substring(matcher.start(), matcher.end());
            if (!sender.hasPermission("gpflags.messages.style." + styleComponent.replace("<", "").replace(">", ""))) {
                message = message.replace(styleComponent, "");
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
