package me.ryanhamshire.GPFlags.listener;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.event.PlayerPostClaimBorderEvent;
import me.ryanhamshire.GPFlags.event.PlayerPreClaimBorderEvent;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Purpose is
 */
public class PlayerListener implements Listener {

    private static final DataStore dataStore = GriefPrevention.instance.dataStore;

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event) {
        Location locTo = event.getTo();
        Location locFrom = event.getFrom();
        Player player = event.getPlayer();
        processMovement(locTo, locFrom, player, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onTeleport(PlayerTeleportEvent event) {
        Location locTo = event.getTo();
        Location locFrom = event.getFrom();
        Player player = event.getPlayer();
        processMovement(locTo, locFrom, player, event);
    }

    @EventHandler
    private void onVehicleMove(VehicleMoveEvent event) {
        Location locTo = event.getTo();
        Location locFrom = event.getFrom();
        Vehicle vehicle = event.getVehicle();
        // If either player weren't allowed in the claim, eject the players and
        // teleport the banned players to the old location
        for (Entity entity : vehicle.getPassengers()) {
            if (entity instanceof Player) {
                Player player = ((Player) entity);
                if (!processMovement(locTo, locFrom, player, null)) {
                    vehicle.eject();
                    player.teleport(locFrom);
                }
            }
        }
        // If boats are banned in the region,
        // todo check the claim
        Util.breakVehicle(vehicle, locFrom);
    }

    @EventHandler
    private void onMount(VehicleEnterEvent event) {
        Entity entity = event.getEntered();
        Vehicle vehicle = event.getVehicle();
        if (entity instanceof Player) {
            Player player = ((Player) entity);
            Location from = player.getLocation();
            Location to = vehicle.getLocation();
            processMovement(to, from, player, event);
        }
    }

    @EventHandler
    private void onClaimDelete(ClaimDeletedEvent event) {
        Claim claim = event.getClaim();
        for (Player player : Util.getPlayersIn(claim)) {
            Location location = player.getLocation();
            PlayerPostClaimBorderEvent borderEvent = new PlayerPostClaimBorderEvent(player, claim, null, location, location);
            Bukkit.getPluginManager().callEvent(borderEvent);
        }
    }

    @EventHandler
    private void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        Location spawn = player.getLocation();
        Claim cachedClaim = dataStore.getPlayerData(player.getUniqueId()).lastClaim;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(spawn,false, cachedClaim);
        PlayerPostClaimBorderEvent borderEvent = new PlayerPostClaimBorderEvent(event.getPlayer(), null, claim, null, spawn);
        Bukkit.getPluginManager().callEvent(borderEvent);
    }

    @EventHandler
    private void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location spawn = event.getRespawnLocation();
        Claim cachedClaim = dataStore.getPlayerData(player.getUniqueId()).lastClaim;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(spawn,false, cachedClaim);
        PlayerPostClaimBorderEvent borderEvent = new PlayerPostClaimBorderEvent(event.getPlayer(), null, claim, null, spawn);
        Bukkit.getPluginManager().callEvent(borderEvent);
    }



    /**
     * Takes in a movement and calls PreClaimBorderEvent if needed.
     * If the event was allowed, will call PostClaimBorderEvent
     * @param locTo
     * @param locFrom
     * @param player
\     * @return If the movement was allowed
     */
    public static boolean processMovement(Location locTo, Location locFrom, Player player) {
        if (locTo.getBlockX() == locFrom.getBlockX() && locTo.getBlockY() == locFrom.getBlockY() && locTo.getBlockZ() == locFrom.getBlockZ()) {
            return true;
        }

        Location locFromAdj = Util.getInBoundsLocation(locFrom);
        Location locToAdj = Util.getInBoundsLocation(locTo);
        Claim lastClaim = dataStore.getPlayerData(player.getUniqueId()).lastClaim;
        Claim claimFrom = dataStore.getClaimAt(locFromAdj, false, lastClaim);
        Claim claimTo = dataStore.getClaimAt(locToAdj, false, null);
        if (claimTo == claimFrom) return true;

        PlayerPreClaimBorderEvent playerPreClaimBorderEvent = new PlayerPreClaimBorderEvent(player, claimFrom, claimTo, locFromAdj, locToAdj);
        Bukkit.getPluginManager().callEvent(playerPreClaimBorderEvent);
        if (playerPreClaimBorderEvent.isCancelled()) return false;

        Bukkit.getPluginManager().callEvent(new PlayerPostClaimBorderEvent(playerPreClaimBorderEvent));
        return true;
    }

}
