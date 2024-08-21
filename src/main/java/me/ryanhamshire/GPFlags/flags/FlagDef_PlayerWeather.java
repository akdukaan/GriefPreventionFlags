package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlagDef_PlayerWeather extends PlayerMovementFlagDefinition implements Listener {

    public FlagDef_PlayerWeather(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        Location respawnLoc = player.getRespawnLocation();
        Location deathLoc = player.getLocation();
        handleWeather(player, deathLoc, respawnLoc);
    }

    @Override
    public void onChangeClaim(Player player, Location from, Location to, Claim claimFrom, Claim claimTo) {
        handleWeather(player, from, to);
    }

    public void handleWeather(Player player, @Nullable Location from, Location to) {
        // Check if weather flag changed
        if (from == null) return;
        Flag flagFrom = this.getFlagInstanceAtLocation(from, player);
        Flag flagTo = this.getFlagInstanceAtLocation(to, player);
        if (flagTo == flagFrom) return;

        // Reset the weather if moving from enabled to disabled
        if (flagTo == null) {
            player.resetPlayerWeather();
            return;
        }

        // Set weather to new flag
        setPlayerWeather(player, flagTo);
    }

    public void setPlayerWeather(Player player, @NotNull Flag flag) {
        String weather = flag.parameters;
        if (weather.equalsIgnoreCase("sun")) {
            player.setPlayerWeather(WeatherType.CLEAR);
        } else if (weather.equalsIgnoreCase("rain")) {
            player.setPlayerWeather(WeatherType.DOWNFALL);
        }
    }

    // todo remove this and most other calls to PlayerJoinEvent and PlayerRespawnEvent
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Flag flag = this.getFlagInstanceAtLocation(player.getLocation(), player);
        if (flag == null) return;

        // Set weather to new flag
        setPlayerWeather(player, flag);
    }

    @Override
    public SetFlagResult validateParameters(String parameters, CommandSender sender) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.PlayerWeatherRequired));
        }
        if (!parameters.equalsIgnoreCase("sun") && !parameters.equalsIgnoreCase("rain")) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.PlayerWeatherRequired));
        }
        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public String getName() {
        return "PlayerWeather";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.PlayerWeatherSet, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.PlayerWeatherUnSet);
    }

}
