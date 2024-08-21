package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.TextMode;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FlagDef_NoVehicle extends PlayerMovementFlagDefinition {

    public FlagDef_NoVehicle(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public void onChangeClaim(@NotNull Player player, @Nullable Location from, @NotNull Location to, @Nullable Claim claimFrom, @Nullable Claim claimTo) {
        // Check if it's a minecart or boat and not an animal
        if (player.getVehicle() == null) return;
        Entity entity = player.getVehicle();
        if (!(entity instanceof Vehicle)) return;
        Vehicle vehicle = (Vehicle) entity;

        // Ignore event for non-drivers
        List<Entity> passengers = vehicle.getPassengers();
        if (passengers.isEmpty()) return;
        Entity passenger = passengers.get(0);
        if (!(passenger instanceof Player)) return;
        Player driver = (Player) passenger;

        // Check if the flag exists
        Flag flag = this.getFlagInstanceAtLocation(driver.getLocation(), player);
        if (flag == null) return;

        // Check if the driver can bypass
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(to, false, null);
        if (Util.shouldBypass(player, claim, flag)) return;

        // Break the vehicle and alert all passengers
        Util.breakVehicle(vehicle, from);
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
                event.setCancelled(true);
            }
        }
    }

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
