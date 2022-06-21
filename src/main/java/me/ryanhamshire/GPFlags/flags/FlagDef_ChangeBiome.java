package me.ryanhamshire.GPFlags.flags;

import me.ryanhamshire.GPFlags.FlagManager;
import me.ryanhamshire.GPFlags.GPFlags;
import me.ryanhamshire.GPFlags.MessageSpecifier;
import me.ryanhamshire.GPFlags.Messages;
import me.ryanhamshire.GPFlags.SetFlagResult;
import me.ryanhamshire.GPFlags.listener.RidableMoveListener;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class FlagDef_ChangeBiome extends FlagDefinition {

    public FlagDef_ChangeBiome(FlagManager manager, GPFlags plugin) {
        super(manager, plugin);
    }

    private int changeBiome(Location greater, Location lesser, Biome biome) {
        int lX = (int) lesser.getX();
        int lZ = (int) lesser.getZ();
        int gX = (int) greater.getX();
        int gZ = (int) greater.getZ();
        World world = lesser.getWorld();
        assert world != null;
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
                        for (int y = 0; y <= world.getMaxHeight(); y++) {
                            world.setBiome(finalX, y, z, biome);
                        }
                    }
                }
            };
            runnable.runTaskLater(GPFlags.getInstance(), i++);
        }
        return i;
    }

    private void changeBiome(Claim claim, Biome biome) {
        Location greater = claim.getGreaterBoundaryCorner();
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

    @SuppressWarnings("deprecation")
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

    public boolean changeBiome(CommandSender sender, Claim claim, String biome) {
        Biome b;
        try {
            b = Biome.valueOf(biome);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Invalid biome");
            return false;
        }
        World world = claim.getLesserBoundaryCorner().getWorld();
        assert world != null;
        if (!sender.hasPermission("gpflags.flag.changebiome." + biome)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cYou do not have permissions for the biome &b" + biome + " &c."));
            return false;
        }
        changeBiome(claim, b);
        return true;
    }

    public void resetBiome(Long claimID) {
        resetBiome(GriefPrevention.instance.dataStore.getClaim(claimID));
    }

    public void resetBiome(Claim claim) {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");

            Location greater = claim.getGreaterBoundaryCorner();
            Location lesser = claim.getLesserBoundaryCorner();
            int lX = lesser.getBlockX();
            int lZ = lesser.getBlockZ();
            int gX = greater.getBlockX();
            int gZ = greater.getBlockZ();
            World world = lesser.getWorld();
            assert world != null;
            int maxHeight = greater.getBlockY();
            int minHeight = lesser.getBlockY();
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
                            for (int y = minHeight; y <= maxHeight; y++) {
                                // TODO getComputedBiome is WRONG
                                world.setBiome(finalX, y, z, loadLoc.getBlock().getComputedBiome());
                            }
                        }
                    }
                };
                runnable.runTaskLater(GPFlags.getInstance(), i++);
            }
        } catch (ClassNotFoundException ignored) {
            // Restore biome by matching with biome of block 2 north of claim
            Biome biome = claim.getLesserBoundaryCorner().getBlock().getRelative(BlockFace.NORTH, 6).getBiome();
            changeBiome(claim, biome);
        }
    }

    @EventHandler
    public void onClaimDelete(ClaimDeletedEvent e) {
        if (e.getClaim().getOwnerName() == null) return; //don't restore a sub-claim
        Claim claim = e.getClaim();

        if (GPFlags.getInstance().getFlagManager().getFlag(claim, this) == null)
            return; // Return if flag is non existent

        resetBiome(claim);
    }

    @Override
    public String getName() {
        return "ChangeBiome";
    }

    @Override
    public SetFlagResult validateParameters(String parameters) {
        if (parameters.isEmpty()) {
            return new SetFlagResult(false, new MessageSpecifier(Messages.MessageRequired));
        }

        return new SetFlagResult(true, this.getSetMessage(parameters));
    }

    @Override
    public MessageSpecifier getSetMessage(String parameters) {
        return new MessageSpecifier(Messages.ChangeBiomeSet, parameters); // TODO CHANGE
    }

    @Override
    public MessageSpecifier getUnSetMessage() {
        return new MessageSpecifier(Messages.ChangeBiomeUnset); // TODO CHANGE
    }

    @Override
    public List<FlagType> getFlagType() {
        return Collections.singletonList(FlagType.CLAIM);
    }

}
