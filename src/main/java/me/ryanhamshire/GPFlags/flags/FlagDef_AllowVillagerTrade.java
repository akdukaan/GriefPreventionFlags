package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.Flag;
import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GriefPrevention.events.ClaimPermissionCheckEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class FlagDef_AllowVillagerTrade extends FlagDefinition {

    public FlagDef_AllowVillagerTrade(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    @Override
    public String getName() {
        return "AllowVillagerTrade";
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.EnabledAllowVillagerTrade);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.DisabledAllowVillagerTrade);
    }
    public boolean canTrade(Villager villager, Player player) {

//        get the flag at the villager's location
        final Flag flag = this.getFlagInstanceAtLocation(villager.getLocation(), player);

//        allow the player to trade if the flag is set
        return flag != null;
    }

    @EventHandler
    public void onGPVillagerTrade(ClaimPermissionCheckEvent event) {

        if(event.getCheckedPlayer() == null) return;

//        only looking for PlayerInteractEntityEvent (the event that GP cancels for trading with villagers)
        if(!(event.getTriggeringEvent() instanceof PlayerInteractEntityEvent)) return;
        final PlayerInteractEntityEvent clickMobEvent = (PlayerInteractEntityEvent) event.getTriggeringEvent();

//        only check if the player right-clicked a villager
        if(!(clickMobEvent.getRightClicked() instanceof Villager)) return;
        final Villager villager = (Villager) clickMobEvent.getRightClicked();

//        check if the player is allowed to trade (if the flag is set)
        if(!canTrade(villager, clickMobEvent.getPlayer())) return;

//        allow the player to trade (in GP, a null denial reason basically tells GP the player has permission)
        event.setDenialReason(null);
    }
}