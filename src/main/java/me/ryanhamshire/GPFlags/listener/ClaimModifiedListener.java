package me.ryanhamshire.GPFlags.listener;

import me.ryanhamshire.GPFlags.event.PlayerPostClaimBorderEvent;
import me.ryanhamshire.GPFlags.event.PlayerPreClaimBorderEvent;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimModifiedListener implements Listener {

    @EventHandler
    private void onChangeClaim(ClaimModifiedEvent event) {
        Claim claimTo = event.getTo();
        Claim claimFrom = event.getFrom();
        World world = claimFrom.getGreaterBoundaryCorner().getWorld();
        for (Player player : world.getPlayers()) {
            Location loc = Util.getInBoundsLocation(player);

            // Resizing a claim to be smaller and falling on the outside
            if (!claimTo.contains(loc, false, false) && claimFrom.contains(loc, false, false)) {
                PlayerPreClaimBorderEvent borderEvent = new PlayerPreClaimBorderEvent(player, claimFrom, null, loc, loc);
                Bukkit.getPluginManager().callEvent(borderEvent);
                if (!borderEvent.isCancelled()) {
                    Bukkit.getPluginManager().callEvent(new PlayerPostClaimBorderEvent(borderEvent));
                }
            }
            // Resizing a claim to be larger and falling on the inside
            if (claimTo.contains(loc, false, false) && !claimFrom.contains(loc, false, false)) {
                PlayerPreClaimBorderEvent borderEvent = new PlayerPreClaimBorderEvent(player, null, claimTo, loc, loc);
                Bukkit.getPluginManager().callEvent(borderEvent);
                if (!borderEvent.isCancelled()) {
                    Bukkit.getPluginManager().callEvent(new PlayerPostClaimBorderEvent(borderEvent));
                }
            }
        }
    }
}
