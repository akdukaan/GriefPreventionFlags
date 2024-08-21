package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.event.PlayerPostClaimBorderEvent;
import me.ryanhamshire.GPFlags.listener.PlayerListener;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlagDef_NoVehicle extends PlayerMovementFlagDefinition {

    public FlagDef_NoVehicle(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    public boolean allowMovement(Player player, Location lastLocation, Location to, Claim claimFrom, Claim claimTo) {
        if (player.getVehicle() == null) return true;

        Flag flag = this.getFlagInstanceAtLocation(to, player);
        if (flag == null) return true;
        if (Util.shouldBypass(player, claimTo, flag)) return true;

        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoVehicleAllowed);
        return false;
    }

    @EventHandler
    private void onVehicleMove(VehicleMoveEvent event) {
        // Check if another flag denies the movement
        Vehicle vehicle = event.getVehicle();
        List<Entity> passengers = vehicle.getPassengers();
        if (passengers.isEmpty()) return;
        Entity passenger = passengers.get(0);
        if (!(passenger instanceof Player)) return;
        Player player = (Player) passenger;
        if (!PlayerListener.processMovement(event.getTo(), event.getFrom(), player, null)) return;

        // Check if NoVehicle denies the movement
        Flag flag = this.getFlagInstanceAtLocation(event.getTo(), player);
        if (flag == null) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(), false, null);
        if (Util.shouldBypass(player, claim, flag)) return;

        // Break the vehicle since we can't cancel VehicleMoveEvent
        breakVehicle(vehicle, event.getFrom());
        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoVehicleAllowed);
    }

    @EventHandler
    private void onTeleport(PlayerTeleportEvent event) {
        // Check if the tp will be allowed
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (!PlayerListener.processMovement(to, event.getFrom(), player, null)) return;

        // Check if NoVehicle applies
        Entity entity = player.getVehicle();
        if (!(entity instanceof Vehicle)) return;
        Flag flag = this.getFlagInstanceAtLocation(to, player);
        if (flag == null) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if (Util.shouldBypass(player, claim, flag)) return;

        // Allow the movement but make the player leave the vehicle first
        player.leaveVehicle();
        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoVehicleAllowed);
    }

    @EventHandler
    private void onMount(VehicleEnterEvent event) {
        Entity entity = event.getEntered();
        Vehicle vehicle = event.getVehicle();
        if (!(entity instanceof Player)) return;
        Player player = ((Player) entity);

        Flag flag = this.getFlagInstanceAtLocation(vehicle.getLocation(), player);
        if (flag == null) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(vehicle.getLocation(), false, null);
        if (Util.shouldBypass(player, claim, flag)) return;

        event.setCancelled(true);
        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoEnterVehicle);
    }

    @EventHandler
    private void onCollision(VehicleEntityCollisionEvent event) {
        Vehicle vehicle = event.getVehicle();
        Flag flag = this.getFlagInstanceAtLocation(vehicle.getLocation(), null);
        if (flag != null) {
            Entity entity = event.getEntity();
            if (entity instanceof Player) {
                Player player = (Player) entity;
                Claim claim = GriefPrevention.instance.dataStore.getClaimAt(vehicle.getLocation(), false, cachedClaim);
                cachedClaim = claim;
                if (Util.shouldBypass(player, claim, flag)) return;
                event.setCollisionCancelled(true);
                event.setCancelled(true);
            }
        }
    }

    /*
    Unneeded because GP already cancels this...?
    @EventHandler
    private void onPlace(EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        Flag flag = this.getFlagInstanceAtLocation(entity.getLocation(), null);
        if (flag == null) return;
        if (!(entity instanceof Vehicle)) return;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(entity.getLocation(), false, null);
        Player player = event.getPlayer();
        if (Util.shouldBypass(player, claim, flag)) return;
        MessagingUtil.sendMessage(player, TextMode.Err, Messages.NoVehicleAllowed);
    }
     */

    @Override
    public String getName() {
        return "NoVehicle";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnabledNoVehicle);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisabledNoVehicle);
    }

    @Override
    public List<FlagType> getFlagType() {
        return Collections.singletonList(FlagType.CLAIM);
    }

}
