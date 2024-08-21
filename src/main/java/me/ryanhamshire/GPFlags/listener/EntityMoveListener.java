package me.ryanhamshire.GPFlags.listener;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class EntityMoveListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    private void onMove(EntityMoveEvent event) {
        List<Entity> passengers = event.getEntity().getPassengers();
        if (passengers.isEmpty()) return;
        Entity passenger = passengers.get(0);
        if (!(passenger instanceof Player)) return;
        Player rider = (Player) passenger;
        Location locTo = event.getTo();
        Location locFrom = event.getFrom();
        if (PlayerListener.flagsPreventMovement(locTo, locFrom, rider)) {
            event.setCancelled(true);
        }
    }
}