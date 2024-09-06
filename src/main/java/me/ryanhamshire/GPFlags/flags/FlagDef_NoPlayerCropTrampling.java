package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class FlagDef_NoPlayerCropTrampling extends FlagDefinition {

    public FlagDef_NoPlayerCropTrampling(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check that the event is a crop trample
        if (event.getAction() != Action.PHYSICAL) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getType() == Material.FARMLAND)) return;

        // Check that the flag is set
        Flag flag = this.getFlagInstanceAtLocation(event.getPlayer().getLocation(), null);
        if (flag == null) return;

        // Prevent the trample
        event.setCancelled(true);
    }

    @Override
    public String getName() {
        return "NoPlayerCropTrampling";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnableNoPlayerCropTrampling);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisableNoPlayerCropTrampling);
    }

}
