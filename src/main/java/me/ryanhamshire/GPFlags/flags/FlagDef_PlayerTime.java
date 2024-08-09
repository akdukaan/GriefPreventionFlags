package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlagDef_PlayerTime extends PlayerMovementFlagDefinition implements Listener {

    public FlagDef_PlayerTime(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location respawnLoc = player.getRespawnLocation();
        Location deathLoc = player.getLocation();
        handleTime(player, deathLoc, respawnLoc);
    }

    @Override
    public void onChangeClaim(Player player, Location from, Location to, Claim claimFrom, Claim claimTo) {
        handleTime(player, from, to);
    }

    public void handleTime(Player player, @Nullable Location from, Location to) {
        // Check if time flag changed
        if (from == null) return;
        Flag flagFrom = this.getFlagInstanceAtLocation(from, player);
        Flag flagTo = this.getFlagInstanceAtLocation(to, player);
        if (flagTo == flagFrom) return;

        // Reset the time if moving from enabled to disabled
        if (flagTo == null) {
            player.resetPlayerTime();
            return;
        }

        // Set time to new flag
        setPlayerTime(player, flagTo);
    }

    public void setPlayerTime(Player player, @NotNull Flag flag) {
        String time = flag.parameters;
        if (time.equalsIgnoreCase("day")) {
            player.setPlayerTime(0, false);
        } else if (time.equalsIgnoreCase("noon")) {
            player.setPlayerTime(6000, false);
        } else if (time.equalsIgnoreCase("night")) {
            player.setPlayerTime(12566, false);
        } else if (time.equalsIgnoreCase("midnight")) {
            player.setPlayerTime(18000, false);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Flag flag = this.getFlagInstanceAtLocation(player.getLocation(), player);
        if (flag == null) return;

        // Set time to new flag
        setPlayerTime(player, flag);
    }

    @Override
    public SetFlagResult validateParameters(String parameters, CommandSender sender) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.PlayerTimeRequired));
        }
        if (!parameters.equalsIgnoreCase("day") && !parameters.equalsIgnoreCase("noon") &&
                !parameters.equalsIgnoreCase("night") && !parameters.equalsIgnoreCase("midnight")) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.PlayerTimeRequired));
        }
        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public String getName() {
        return "PlayerTime";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.PlayerTimeSet, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.PlayerTimeUnSet);
    }

}
