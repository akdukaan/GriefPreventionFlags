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
import org.bukkit.event.player.PlayerJoinEvent;

public class FlagDef_PlayerTime extends PlayerMovementFlagDefinition implements Listener {

    public FlagDef_PlayerTime(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public void onChangeClaim(Player player, Location lastLocation, Location to, Claim claimFrom, Claim claimTo) {
        if (lastLocation == null) return;
        Flag flag = this.getFlagInstanceAtLocation(to, player);

        if (flag == null) {
            if (this.getFlagInstanceAtLocation(lastLocation, player) != null) {
                player.resetPlayerTime();
            }
            return;
        }
        if (flag == this.getFlagInstanceAtLocation(lastLocation, player)) return;

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
