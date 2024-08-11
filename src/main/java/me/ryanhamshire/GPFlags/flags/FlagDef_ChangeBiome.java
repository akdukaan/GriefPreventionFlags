package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GPFlags.util.MessagingUtil;
import me.ryanhamshire.GPFlags.util.Util;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class FlagDef_ChangeBiome extends FlagDefinition {

    public FlagDef_ChangeBiome(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    /**
     * What actually does all the biome changing stuff
     * @param greater corner
     * @param lesser corner
     * @param biome the biome to set it to
     * @return The number of ticks it's going to take to finish changing the biome
     */
    private int changeBiome(Location greater, Location lesser, Biome biome) {
        int lX = (int) lesser.getX();
        int lY = (int) lesser.getY();
        int lZ = (int) lesser.getZ();
        int gX = (int) greater.getX();
        int gY = (int) greater.getY();
        int gZ = (int) greater.getZ();
        World world = lesser.getWorld();
        int i = 0;
        for (int x = lX; x < gX; x++) {
            int finalX = x;
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    for (int z = lZ; z < gZ; z++) {
                        Location loadLoc = new Location(world, finalX, 100, z);
                        Chunk loadChunk = loadLoc.getChunk();
                        if (!(loadChunk.isLoaded())) {
                            loadChunk.load();
                        }
                        for (int y = lY; y <= gY; y++) {
                            world.setBiome(finalX, y, z, biome);
                        }
                    }
                }
            };
            runnable.runTaskLater(GPFlags.getInstance(), i++);
        }
        return i;
    }

    /**
     * Runs the other changeBiome and then refreshes chunks in the claim
     * @param claim
     * @param biome
     */
    private void changeBiome(Claim claim, Biome biome) {
        Location greater = claim.getGreaterBoundaryCorner();
        greater.setY(Util.getMaxHeight(greater));
        Location lesser = claim.getLesserBoundaryCorner();
        int i = changeBiome(greater, lesser, biome);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                refreshChunks(claim);
            }
        };
        runnable.runTaskLater(GPFlags.getInstance(), i);
    }

    private void refreshChunks(Claim claim) {
        int view = Bukkit.getServer().getViewDistance();
        Player player = Bukkit.getPlayer(claim.getOwnerName());
        if (player != null && player.isOnline()) {
            Location loc = player.getLocation();
            if (claim.contains(loc, true, true)) {
                int X = loc.getChunk().getX();
                int Z = loc.getChunk().getZ();
                for (int x = X - view; x <= (X + view); x++) {
                    for (int z = Z - view; z <= (Z + view); z++) {
                        player.getWorld().refreshChunk(x, z);
                    }
                }
            }
        }
    }

    /**
     * Validates biome name and permissions and then runs the changeBiome command
     * @param sender
     * @param claim
     * @param biome
     * @return
     */
    public boolean changeBiome(CommandSender sender, Claim claim, String biome) {
        Biome b;
        try {
            b = Biome.valueOf(biome);
        } catch (Throwable e) {
            sender.sendMessage("<red>Invalid biome");
            return false;
        }
        World world = claim.getLesserBoundaryCorner().getWorld();
        if (world == null) {
            sender.sendMessage("<red>World does not exist");
            return false;
        }
        if (!sender.hasPermission("gpflags.flag.changebiome." + biome)) {
            MessagingUtil.sendMessage(sender,"<red>You do not have permissions for the biome <aqua>" + biome + " <red>." );
            return false;
        }
        changeBiome(claim, b);
        return true;
    }

    public void resetBiome(Claim claim) {
        // Restore biome by matching with biome of block 2 north of claim
        Biome biome = claim.getLesserBoundaryCorner().getBlock().getRelative(BlockFace.NORTH, 6).getBiome();
        changeBiome(claim, biome);
    }

    @EventHandler
    public void onClaimDelete(ClaimDeletedEvent e) {
        if (e.getClaim().parent != null) return; //don't restore a sub-claim
        Claim claim = e.getClaim();
        FlagManager fm = GPFlags.getInstance().getFlagManager();
        if (fm.getEffectiveFlag(claim.getLesserBoundaryCorner(), this.getName(), claim) == null) return;

        resetBiome(claim);
    }

    @Override
    public String getName() {
        return "ChangeBiome";
    }

    @Override
    public SetFlagResult validateParameters(String parameters, CommandSender sender) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.MessageRequired));
        }

        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.ChangeBiomeSet, parameters);
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.ChangeBiomeUnset);
    }

    @Override
    public List<FlagType> getFlagType() {
        return Collections.singletonList(FlagType.CLAIM);
    }

}
