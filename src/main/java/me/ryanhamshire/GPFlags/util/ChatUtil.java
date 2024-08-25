package me.ryanhamshire.GPFlags.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final MiniMessage minimessage = MiniMessage.builder().build();

    // Used to format chat-messages (legacy-support) of a specific sender related to his permissions
    public static String validatedHexString(CommandSender sender, String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = parseEasyToComplex(message);
        message = validateMinimessage(sender, message);
        return message;
    }

    // Used to format a chat-messages (legacy-support) without previous validation of somebody.
    public static Component hexComp(String message) {
        message = message.replace("§", "&");
        message = parseHexColorCodes(message);
        message = parseEasyToComplex(message);
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

    // Parses legacy native-colors to MiniMessage native-colors
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

    private static String parseEasyToComplex(String message) {
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
        message = message.replace("&n", "<u>");
        message = message.replace("&o", "<italic>");
        message = message.replace("&k", "<obf>");
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
        return message;
    }

    // Returns the MiniMessage color-code for a specific native-color. It also resets the color when the char is 'r'
    private static String getNativeColor(char color) {
        String reset = "<reset>";
        String colorCode = "<color:white>";
        if (color == 'r') return reset + colorCode;
        switch (color) {
            case 'a':
                colorCode = "<color:green>";
                break;
            case 'b':
                colorCode = "<color:aqua>";
                break;
            case 'c':
                colorCode = "<color:red>";
                break;
            case 'd':
                colorCode = "<color:light_purple>";
                break;
            case 'e':
                colorCode = "<color:yellow>";
                break;
            case 'f':
                colorCode = "<color:white>";
                break;
            case '0':
                colorCode = "<color:black>";
                break;
            case '1':
                colorCode = "<color:dark_blue>";
                break;
            case '2':
                colorCode = "<color:dark_green>";
                break;
            case '3':
                colorCode = "<color:dark_aqua>";
                break;
            case '4':
                colorCode = "<color:dark_red>";
                break;
            case '5':
                colorCode = "<color:dark_purple>";
                break;
            case '6':
                colorCode = "<color:gold>";
                break;
            case '7':
                colorCode = "<color:gray>";
                break;
            case '8':
                colorCode = "<color:dark_gray>";
                break;
            case '9':
                colorCode = "<color:blue>";
                break;
            case 'l':
                colorCode = "<bold>";
                break;
            case 'm':
                colorCode = "<strikethrough>";
                break;
            case 'n':
                colorCode = "<u>";
                break;
            case 'o':
                colorCode = "<italic>";
                break;
            case 'k':
                colorCode = "<obf>";
                break;
        }
        /*switch (color) {
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'k':
                break;
            default:
                colorCode = reset + colorCode;
        }*/
        return colorCode;
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
