package me.ryanhamshire.GPFlags.util;

import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.GPFlagsConfig;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.TextMode;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

@SuppressWarnings("WeakerAccess")
public class Util {

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major Major version to check (Most likely just going to be 1)
     * @param minor Minor version to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor) {
        return isRunningMinecraft(major, minor, 0);
    }

    /**
     * Check if server is running a minimum Minecraft version
     *
     * @param major    Major version to check (Most likely just going to be 1)
     * @param minor    Minor version to check
     * @param revision Revision to check
     * @return True if running this version or higher
     */
    public static boolean isRunningMinecraft(int major, int minor, int revision) {
        String[] version = getMinecraftVersion().split("\\.");
        int maj = Integer.parseInt(version[0]);
        int min = Integer.parseInt(version[1]);
        int rev;
        try {
            rev = Integer.parseInt(version[2]);
        } catch (Exception ignore) {
            rev = 0;
        }
        return maj > major || min > minor || (min == minor && rev >= revision);
    }

    /**
     * Get the Minecraft version the server is running
     *
     * @return Minecraft version the server is running
     */
    public static String getMinecraftVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }


    /**
     * Disable the flight mode of a player whom cant fly
     * <p>This is mainly used when a player deletes their claim.</p>
     *
     * @param player Player to disable flight for
     */
    public static void disableFlight(Player player) {
        if (player.isFlying() && !canFly(player)) {
            Location loc = player.getLocation();
            Block block = loc.getBlock();
            while (block.getY() > 2 && !block.getType().isSolid() && block.getType() != Material.WATER) {
                block = block.getRelative(BlockFace.DOWN);
            }
            player.setAllowFlight(false);
            if (loc.getY() - block.getY() >= 4) {
                GPFlags.getInstance().getPlayerListener().addFallingPlayer(player);
            }
            Util.sendMessage(player, TextMode.Warn, Messages.ExitFlightDisabled);
        }
        if (player.getAllowFlight() && !canFly(player)) {
            player.setAllowFlight(false);
            Util.sendMessage(player, TextMode.Warn, Messages.ExitFlightDisabled);
        }
    }

    private static boolean canFly(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SPECTATOR || mode == GameMode.CREATIVE ||
                player.hasPermission("gpflags.bypass.fly") || player.hasPermission("gpflags.bypass");
    }

    /**
     * Get the ItemStack form of a vehicle
     * <p>Specifically a boat or minecart</p>
     *
     * @param vehicle Vehicle to get item from
     * @return ItemStack that matches vehicle
     */
    public static ItemStack getItemFromVehicle(Vehicle vehicle) {
        Material material = null;
        if (vehicle instanceof Boat) {
            switch (((Boat) vehicle).getWoodType()) {
                case BIRCH:
                    material = Material.BIRCH_BOAT;
                    break;
                case ACACIA:
                    material = Material.ACACIA_BOAT;
                    break;
                case JUNGLE:
                    material = Material.JUNGLE_BOAT;
                    break;
                case REDWOOD:
                    material = Material.SPRUCE_BOAT;
                    break;
                case DARK_OAK:
                    material = Material.DARK_OAK_BOAT;
                    break;
                default:
                    material = Material.OAK_BOAT;
            }
        } else if (vehicle instanceof RideableMinecart) {
            material = Material.MINECART;
        } else if (vehicle instanceof StorageMinecart) {
            material = Material.CHEST_MINECART;
        } else if (vehicle instanceof CommandMinecart) {
            material = Material.COMMAND_BLOCK_MINECART;
        } else if (vehicle instanceof ExplosiveMinecart) {
            material = Material.TNT_MINECART;
        } else if (vehicle instanceof HopperMinecart) {
            material = Material.HOPPER_MINECART;
        } else if (vehicle instanceof PoweredMinecart) {
            material = Material.FURNACE_MINECART;
        }
        if (material != null) {
            return new ItemStack(material);
        }
        return null;
    }

    /**
     * Check if an ItemStack is a vehicle
     * <p>Specifically a boat or minecart</p>
     *
     * @param itemStack ItemStack to check
     * @return True if item is a vehicle
     */
    public static boolean isAVehicle(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case MINECART:
            case CHEST_MINECART:
            case COMMAND_BLOCK_MINECART:
            case FURNACE_MINECART:
            case HOPPER_MINECART:
            case TNT_MINECART:
            case BIRCH_BOAT:
            case ACACIA_BOAT:
            case DARK_OAK_BOAT:
            case JUNGLE_BOAT:
            case OAK_BOAT:
            case SPRUCE_BOAT:
                return true;
        }
        return false;
    }

    public static boolean isMonster(Entity entity) {
        EntityType type = entity.getType();
        return (entity instanceof Monster || type == EntityType.GHAST || type == EntityType.MAGMA_CUBE || type == EntityType.SHULKER
                || type == EntityType.PHANTOM || type == EntityType.SLIME);
    }

    /**
     * Shortcut for adding color to a string
     *
     * @param string String including color codes
     * @return Formatted string
     */
    public static String getColString(String string) {
        final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(string);
        StringBuffer buffer = new StringBuffer(string.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        string = matcher.appendTail(buffer).toString();
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Send a {@link MessageSpecifier} to a player, or console if player is null
     *
     * @param player    Player to send message to, or null if to console
     * @param color     Color of message
     * @param specifier Message specifier to send
     */
    public static void sendMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, MessageSpecifier specifier) {
        sendMessage(player, color, specifier.getMessageID(), specifier.getMessageParams());
    }

    /**
     * Send a {@link Messages Message} to a player, or console if player is null
     *
     * @param player    Player to send message to, or null if to console
     * @param color     Color of message
     * @param messageID Message to send
     * @param args      Message parameters
     */
    public static void sendMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, Messages messageID, String... args) {
        String message = GPFlags.getInstance().getFlagsDataStore().getMessage(messageID, args);
        sendMessage(player != null ? player : Bukkit.getConsoleSender(), color + message);
    }

    public static void sendMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, String message) {
        sendMessage(player, color + message);
    }

    public static void sendMessage(@Nullable CommandSender receiver, String format, Object... objects) {
        sendMessage(receiver, String.format(format, objects));
    }

    public static void sendMessage(@Nullable CommandSender receiver, String message) {
        if (receiver != null) {
            receiver.sendMessage(getColString(GPFlags.getInstance().getFlagsDataStore().getMessage(Messages.Prefix) + message));
        } else {
            log(message);
        }
    }

    public static void sendClaimMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, MessageSpecifier specifier) {
        sendClaimMessage(player, color, specifier.getMessageID(), specifier.getMessageParams());
    }

    public static void sendClaimMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, Messages messageID, String... args) {
        String message = GPFlags.getInstance().getFlagsDataStore().getMessage(messageID, args);
        sendClaimMessage(player, color, message);
    }

    public static void sendClaimMessage(@Nullable CommandSender player, org.bukkit.ChatColor color, String message) {
        sendClaimMessage(player, color + message);
    }

    public static void sendClaimMessage(@Nullable CommandSender receiver, String format, Object... args) {
        sendClaimMessage(receiver, String.format(format, args));
    }

    public static void sendClaimMessage(@Nullable CommandSender receiver, String message) {
        if (receiver != null) {
            receiver.sendMessage(Util.getColString(message));
        } else {
            log(Util.getColString(message));
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(getColString(Messages.Prefix + message));
    }

    public static void log(String format, Object... objects) {
        log(String.format(format, objects));
    }

    public static void logFlagCommands(String log) {
        if (GPFlagsConfig.LOG_ENTER_EXIT_COMMANDS) {
            Util.log(log);
        }
    }

}
