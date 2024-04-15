package me.ryanhamshire.GPFlags;

import me.ryanhamshire.GPFlags.event.PlayerPostClaimBorderEvent;
import me.ryanhamshire.GPFlags.flags.*;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.TrustChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class FlightManager implements Listener {
    private static final HashSet<Player> fallImmune = new HashSet<>();

    @EventHandler
    private void onFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = ((Player) e.getEntity());
        EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause != EntityDamageEvent.DamageCause.FALL) return;
        if (!fallImmune.contains(p)) return;
        e.setDamage(0);
        fallImmune.remove(p);
    }

    public static void manageFlightLater(Player player, int ticks) {
        Bukkit.getScheduler().runTaskLater(GPFlags.getInstance(), () -> {
            managePlayerFlight(player, player.getLocation());
        }, ticks);
    }


    @EventHandler
    public void onTrustChanged(TrustChangedEvent event) {
        String identifier = event.getIdentifier();

        // If everyone, manage flight for everyone in affected claim(s)
        if (identifier.equalsIgnoreCase("public") || identifier.equalsIgnoreCase("all")) {
            Collection<Claim> claims = event.getClaims();
            for (Claim claim : claims) {
                for (Player player : Util.getPlayersIn(claim)) {
                    manageFlightLater(player, 1);
                }
            }
            return;
        }

        // If a player, manage flight for the player
        try {
            UUID uuid = UUID.fromString(identifier);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                manageFlightLater(player, 1);
            }
            return;
        } catch (IllegalArgumentException ignored) {}

        // Otherwise, its a permission
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(identifier)) {
                manageFlightLater(player, 1);
            }
        }
    }

    @EventHandler
    public void onChangeClaim(PlayerPostClaimBorderEvent event) {
        managePlayerFlight(event.getPlayer(), event.getLocTo());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        managePlayerFlight(player, player.getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        managePlayerFlight(player, player.getLocation());
    }

    @EventHandler
    public void onClaimDelete(ClaimDeletedEvent event) {
        for (Player player : Util.getPlayersIn(event.getClaim())) {
            managePlayerFlight(player, player.getLocation());
        }
    }

    public static boolean allowedEntry(Player player, Location location) {
        if (!FlagDef_NoEnter.allowedEntry(player, location)) return false;
        if (!FlagDef_NoEnterPlayer.allowedEntry(player, location)) return false;
        return true;
    }

    public static void managePlayerFlight(Player player, Location location) {
        boolean manageFlight = gpfManagesFlight(player);
        if (!allowedEntry(player, location)) return;

        // If you could already fly
        if (player.getAllowFlight()) {
            if (manageFlight) {
                if (FlagDef_OwnerMemberFly.letPlayerFly(player, location)) {
                    return;
                }
                if (FlagDef_OwnerFly.letPlayerFly(player, location)) {
                    return;
                }
            }
            if (!FlagDef_NoFlight.letPlayerFly(player, location)) {
                turnOffFlight(player);
                return;
            }
            if (manageFlight) {
                if (FlagDef_PermissionFly.letPlayerFly(player, location)) {
                    return;
                }
                turnOffFlight(player);
            }
            return;
        }

        // If you couldn't already fly
        if (manageFlight) {
            if (FlagDef_OwnerMemberFly.letPlayerFly(player, location)) {
                turnOnFlight(player);
                return;
            }
            if (FlagDef_OwnerFly.letPlayerFly(player, location)) {
                turnOnFlight(player);
                return;
            }
        }
        if (!FlagDef_NoFlight.letPlayerFly(player, location)) {
            return;
        }
        if (manageFlight) {
            if (FlagDef_PermissionFly.letPlayerFly(player, location)) {
                turnOnFlight(player);
            }
        }
    }

    private static void turnOffFlight(Player player) {
        MessagingUtil.sendMessage(player, TextMode.Err, Messages.CantFlyHere);
        player.setFlying(false);
        player.setAllowFlight(false);

        Location location = player.getLocation();
        Block floor = location.getBlock();
        while (floor.getY() > 2 && !floor.getType().isSolid() && floor.getType() != Material.WATER) {
            floor = floor.getRelative(BlockFace.DOWN);
        }
        if (location.getY() - floor.getY() >= 4) {
            fallImmune.add(player);
        }
    }

    private static void turnOnFlight(Player player) {
        player.setAllowFlight(true);
        MessagingUtil.sendMessage(player, TextMode.Success, Messages.EnterFlightEnabled);
        Material below = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        if ((below == Material.AIR) && !player.isGliding()) {
            player.setFlying(true);
        }
    }

    private static boolean gpfManagesFlight(Player player) {
        if (player.hasPermission("gpflags.bypass.fly")) return false;
        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE) return false;
        if (mode == GameMode.SPECTATOR) return false;
        return true;
    }
}
