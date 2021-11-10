package me.ryanhamshire.GPFlags;

import me.ryanhamshire.GPFlags.util.Util;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Data holder for flags
 */
//singleton class which manages all GriefPrevention data (except for config options)
public class FlagsDataStore {

    private final static String dataLayerFolderPath = "plugins" + File.separator + "GPFlags";
    final static String configFilePath = dataLayerFolderPath + File.separator + "config.yml";
    final static String messagesFilePath = dataLayerFolderPath + File.separator + "messages.yml";
    final static String flagsFilePath = dataLayerFolderPath + File.separator + "flags.yml";
    final static String flagsErrorFilePath = dataLayerFolderPath + File.separator + "flagsError.yml";

    //in-memory cache for messages
    private String[] messages;

    public FlagsDataStore() {
        this.loadMessages();
    }

    private void loadMessages() {
        Messages[] messageIDs = Messages.values();
        this.messages = new String[Messages.values().length];

        HashMap<String, CustomizableMessage> defaults = new HashMap<>();

        //initialize defaults
        this.addDefault(defaults, Messages.NoCommandPermission, "You do not have permission to use command: &7/gpflags &b{0}", "0:subcommand");
        this.addDefault(defaults, Messages.UnknownCommand, "Unknown Command: &7/gpflags &c{0}", "0:subcommand");
        this.addDefault(defaults, Messages.PlayerOnlyCommand, "Player Only Command: &7/gpflags &c{0}", "0:subcommand");
        this.addDefault(defaults, Messages.ReloadComplete, "Reloaded config settings and flags from disk.  If you've updated your GPFlags jar file, you MUST either /reload or reboot your server to activate the update.", null);
        this.addDefault(defaults, Messages.NoFlagsInThisClaim, "This claim doesn't have any flags.", null);
        this.addDefault(defaults, Messages.ThatFlagNotSet, "That flag isn't set here.", null);
        this.addDefault(defaults, Messages.InvalidFlagDefName, "Available Flags: {0}", "0:flags list");
        this.addDefault(defaults, Messages.NoFlagsHere, "There aren't any flags set here.", null);
        this.addDefault(defaults, Messages.StandInAClaim, "Please stand inside a GriefPrevention claim and try again.", null);
        this.addDefault(defaults, Messages.FlagsClaim, "This Claim: {0}", "0:list of active flags in a land claim");
        this.addDefault(defaults, Messages.FlagsParent, "Parent Claim: {0}", "0:list of active flags in the parent claim of this land claim");
        this.addDefault(defaults, Messages.FlagsDefault, "All Claims: {0}", "0:list of active default flags in all land claims");
        this.addDefault(defaults, Messages.FlagsWorld, "This World: {0}", "0:list of active flags in this world");
        this.addDefault(defaults, Messages.FlagsServer, "Entire Server: {0}", "0:list of flags which are active everywhere on the server");
        this.addDefault(defaults, Messages.NoFlagPermission, "You don't have permission to use flag: &b{0}", "0:flag name");
        this.addDefault(defaults, Messages.DefaultFlagSet, "Set flag for all land claims.  To make exceptions, move to specific land claims and use '/gpflags UnSetClaimFlag'.  Undo with '/gpflags UnSetDefaultClaimFlag'.", null);
        this.addDefault(defaults, Messages.DefaultFlagUnSet, "That flag is no longer set by default in any land claims.", null);
        this.addDefault(defaults, Messages.ServerFlagSet, "Set flag for entire server (all worlds).", null);
        this.addDefault(defaults, Messages.ServerFlagUnSet, "That flag is no longer set at the server level.", null);
        this.addDefault(defaults, Messages.WorldFlagSet, "Set flag for this world.", null);
        this.addDefault(defaults, Messages.WorldFlagUnSet, "That flag is no longer set for this world.", null);
        this.addDefault(defaults, Messages.NotYourClaim, "You don't have permission to configure flags in this land claim.", null);

        this.addDefault(defaults, Messages.UpdateGPForSubdivisionFlags, "Until you update GriefPrevention, you may only apply flags to top-level land claims.  You're currently standing in a subclaim/subdivision.", null);

        this.addDefault(defaults, Messages.DisableMonsterSpawns, "Disabled monster spawns in this land claim.", null);
        this.addDefault(defaults, Messages.EnableMonsterSpawns, "Re-enabled monster spawns in this land claim.", null);
        this.addDefault(defaults, Messages.DisableMonsters, "Disabled monsters in this land claim.", null);
        this.addDefault(defaults, Messages.EnableMonsters, "Re-enabled monsters in this land claim.", null);

        this.addDefault(defaults, Messages.DisableMobSpawns, "Now blocking living entity (mob) spawns in this land claim.", null);
        this.addDefault(defaults, Messages.EnableMobSpawns, "Stopped blocking living entity (mob) spawns in this land claim.", null);

        this.addDefault(defaults, Messages.DisableMobDamage, "Now blocking environmental and monster damage to passive and named mobs in this land claim.", null);
        this.addDefault(defaults, Messages.EnableMobDamage, "Stopped blocking environmental and monster damage to passive and named mobs in this land claim.", null);

        this.addDefault(defaults, Messages.AddEnablePvP, "Disabled GriefPrevention and GPFlags player vs. player combat limitations in this land claim.", null);
        this.addDefault(defaults, Messages.RemoveEnabledPvP, "GriefPrevention and GPFlags may now limit player combat in this land claim.", null);

        this.addDefault(defaults, Messages.MessageRequired, "Please specify a message to send.", null);
        this.addDefault(defaults, Messages.CommandRequired, "Please specify a command line to execute.", null);
        this.addDefault(defaults, Messages.ConsoleCommandRequired, "Please specify a command line(s) to execute.  You may find the %owner%, %name% and %uuid% placeholders useful.  Separate multiple command lines with a semicolon (;).", null);
        this.addDefault(defaults, Messages.PlayerCommandRequired, "Please specify a player command line(s) to execute.  You may find the %owner%, %name% and %uuid% placeholders useful.  Separate multiple command lines with a semicolon (;).\"", null);
        this.addDefault(defaults, Messages.AddedEnterMessage, "Players entering this land claim will now receive this message: {0}", "0: message to send");
        this.addDefault(defaults, Messages.RemovedEnterMessage, "Players entering this land claim will not receive any message.", null);

        this.addDefault(defaults, Messages.AddedExitMessage, "Players exiting this land claim will now receive this message: {0}", "0: message to send");
        this.addDefault(defaults, Messages.RemovedExitMessage, "Players exiting this land claim will not receive any message.", null);
        this.addDefault(defaults, Messages.EnterExitPrefix, "", "This prefix will be added to all enter/exit message flags");

        this.addDefault(defaults, Messages.SetRespawnLocation, "Players who die in this land claim will now respawn at the specified location.", null);
        this.addDefault(defaults, Messages.UnSetRespawnLocation, "Players who die in this land claim will now respawn per the usual rules.", null);

        this.addDefault(defaults, Messages.LocationRequired, "Please specify a location in four parts, like this: world x y z", null);
        this.addDefault(defaults, Messages.WorldNotFound, "World '{0}' not found.", "0: world");

        this.addDefault(defaults, Messages.EnableKeepInventory, "Players will keep their inventories when they die in this land claim.", null);
        this.addDefault(defaults, Messages.DisableKeepInventory, "Now allowing players to drop their loot on death in this land claim.", null);

        this.addDefault(defaults, Messages.EnableInfiniteArrows, "Arrows fired within this land claim will be refunded.", null);
        this.addDefault(defaults, Messages.DisableInfiniteArrows, "Disabled refunding for arrows fired within this land claim.", null);

        this.addDefault(defaults, Messages.EnableKeepLevel, "Players will keep their experience/levels when they die in this land claim.", null);
        this.addDefault(defaults, Messages.DisableKeepLevel, "Disabled protection for experience/levels when dying in this land claim.", null);

        this.addDefault(defaults, Messages.EnableNetherPortalPlayerCommand, "Players who step into nether portals in this land claim will now auto-execute the specified command line.", null);
        this.addDefault(defaults, Messages.DisableNetherPortalPlayerCommand, "Disabled player command execution for nether portals in this land claim.", null);

        this.addDefault(defaults, Messages.EnableNetherPortalConsoleCommand, "When players step into nether portals in this land claim the specified command line(s) will execute.", null);
        this.addDefault(defaults, Messages.DisableNetherPortalConsoleCommand, "Disabled console command execution for nether portals in this land claim.", null);

        this.addDefault(defaults, Messages.AddedEnterCommand, "When players step into this area, the specified command line(s) will execute.", null);
        this.addDefault(defaults, Messages.RemovedEnterCommand, "Disabled console command execution for entering this area.", null);

        this.addDefault(defaults, Messages.AddedExitCommand, "When players step out of this area, the specified command line(s) will execute.", null);
        this.addDefault(defaults, Messages.RemovedExitCommand, "Disabled console command execution for leaving this area.", null);

        this.addDefault(defaults, Messages.EnableNoCombatLoot, "Except for players, entities which die in this land claim will not drop loot.", null);
        this.addDefault(defaults, Messages.DisableNoCombatLoot, "Stopped blocking loot from non-player deaths.", null);

        this.addDefault(defaults, Messages.EnableNoPlayerDamage, "Players will not take any damage in this land claim.", null);
        this.addDefault(defaults, Messages.DisableNoPlayerDamage, "Stopped preventing player damage in this land claim.", null);

        this.addDefault(defaults, Messages.EnableNoPlayerDamageByMonster, "Players will not take any damage by monsters in this land claim.", null);
        this.addDefault(defaults, Messages.DisableNoPlayerDamageByMonster, "Stopped preventing player damage by monsters in this land claim.", null);

        this.addDefault(defaults, Messages.EnabledNoEnter, "Players now require /AccessTrust or higher permission to enter this area.  Players with permission gpflags.bypass are immune to this flag.", null);
        this.addDefault(defaults, Messages.DisabledNoEnter, "Stopped requiring permission to enter this area.", null);
        this.addDefault(defaults, Messages.NoEnterMessage, "You have been blocked from entering this claim", null);

        this.addDefault(defaults, Messages.EnableNoFluidFlow, "Now preventing source fluid blocks from spreading in this land claim.", null);
        this.addDefault(defaults, Messages.DisableNoFluidFlow, "Stopped limiting fluid flow in this land claim.", null);

        this.addDefault(defaults, Messages.EnableHealthRegen, "Now regenerating player health here.", null);
        this.addDefault(defaults, Messages.DisableHealthRegen, "Stopped regenerating player health here.", null);
        this.addDefault(defaults, Messages.HealthRegenGreaterThanZero, "Please specify how many health points (minimum: 1) players should regenerate per 5 seconds.", null);

        this.addDefault(defaults, Messages.EnableNoHunger, "Disabled food level loss and hunger damage in this area.  Food level regen per 5 seconds: {0}", "0:regen amount");
        this.addDefault(defaults, Messages.DisableNoHunger, "Disabled food level regeneration and stopped blocking food level loss in this area.", null);
        this.addDefault(defaults, Messages.FoodRegenInvalid, "Please specify how much food level to regenerate per 5 seconds (zero for no regneration).", null);

        this.addDefault(defaults, Messages.EnableCommandBlackList, "Now blocking the specified commands in this area.  Players with permission gpflags.bypass are immune to this flag.", null);
        this.addDefault(defaults, Messages.DisableCommandBlackList, "Stopped blocking commands in this area.", null);
        this.addDefault(defaults, Messages.EnableCommandWhiteList, "Now blocking all commands EXCEPT the specified commands in this area.  Players with permission gpflags.bypass are immune to this flag.", null);
        this.addDefault(defaults, Messages.DisableCommandWhiteList, "Stopped blocking commands in this area.", null);
        this.addDefault(defaults, Messages.CommandListRequired, "Please provide a list of commands, separated by semicolons(;).", null);
        this.addDefault(defaults, Messages.CommandBlockedHere, "You don't have permission to use that command here.", null);

        this.addDefault(defaults, Messages.CantFlyHere, "You can't fly here.", null);
        this.addDefault(defaults, Messages.EnableNoFlight, "Now blocking flight in this area.  Players with permission gpflags.bypass are immune to this flag.", null);
        this.addDefault(defaults, Messages.DisableNoFlight, "Stopped preventing flight in this area.", null);

        this.addDefault(defaults, Messages.EnableTrappedDestination, "The /trapped command will now send players to the specified location when executed here.", null);
        this.addDefault(defaults, Messages.DisableTrappedDestination, "Stopped overriding the /trapped command when used in this area.", null);

        this.addDefault(defaults, Messages.EnableNoLootProtection, "Player death loot will not be protected by GriefPrevention in this area.", null);
        this.addDefault(defaults, Messages.DisableNoLootProtection, "Stopped blocking death loot protection in this area.", null);

        this.addDefault(defaults, Messages.EnableNoExpiration, "Claims here will never expire.", null);
        this.addDefault(defaults, Messages.DisableNoExpiration, "Stopped blocking claim expiration here..", null);

        this.addDefault(defaults, Messages.EnableNoEnderPearl, "Now blocking ender pearl teleportation to/from this area.", null);
        this.addDefault(defaults, Messages.DisableNoEnderPearl, "Stopped blocking ender pearl teleportation to/from this area.", null);
        this.addDefault(defaults, Messages.NoEnderPearlInClaim, "{p}, you cannot use enderpearls in {o}'s claim",
                "o: owner of claim" + " p: event player");
        this.addDefault(defaults, Messages.NoEnderPearlToClaim, "{p}, you cannot use enderpearls to teleport into {o}'s claim",
                "o: owner of claim" + " p: event player");
        this.addDefault(defaults, Messages.NoEnderPearlInWorld, "{p}, you cannot use enderpearls in this world",
                "p: event player");

        this.addDefault(defaults, Messages.EnableNoMcMMOSkills, "Now blocking McMMO skill use in this area.", null);
        this.addDefault(defaults, Messages.DisableNoMcMMOSkills, "Stopped blocking McMMO skill use in this area.", null);

        this.addDefault(defaults, Messages.EnabledNoMcMMOXP, "Now blocking McMMO XP gain in this area.", null);
        this.addDefault(defaults, Messages.DisabledNoMcMMOXP, "Stopped blocking McMMO XP gain in this area.", null);

        this.addDefault(defaults, Messages.EnableNoLeafDecay, "Now blocking leaf decay in this area.", null);
        this.addDefault(defaults, Messages.DisableNoLeafDecay, "Stopped blocking leaf decay in this area.", null);

        this.addDefault(defaults, Messages.EnableNoMcMMODeathPenalty, "Now blocking McMMO death penalties in this area.", null);
        this.addDefault(defaults, Messages.DisableNoMcMMODeathPenalty, "Stopped blocking McMMO death penalties in this area.", null);

        this.addDefault(defaults, Messages.EnableNoPetDamage, "Now blocking all damage to pets in this area.", null);
        this.addDefault(defaults, Messages.DisableNoPetDamage, "Stopped blocking damage to pets in this area.", null);

        this.addDefault(defaults, Messages.EnableNoWeatherChange, "Now blocking all weather changes in this area.", null);
        this.addDefault(defaults, Messages.DisableNoWeatherChange, "Stopped blocking weather changes in this area.", null);

        this.addDefault(defaults, Messages.EnableNoItemPickup, "Now blocking all item pickups in this area.", null);
        this.addDefault(defaults, Messages.DisableNoItemPickup, "Stopped blocking item pickups in this area.", null);

        this.addDefault(defaults, Messages.EnableNoItemDrop, "Now blocking player item drops in this area.", null);
        this.addDefault(defaults, Messages.DisableNoItemDrop, "Stopped blocking player item drops in this area.", null);

        this.addDefault(defaults, Messages.EnableNoChorusFruit, "Now blocking chorus fruit teleportation in this area.", null);
        this.addDefault(defaults, Messages.DisableNoChorusFruit, "Stopped blocking chorus fruit teleportation in this area.", null);

        this.addDefault(defaults, Messages.SpleefArenaHelp, "Example syntax: 'minecraft:snow_block minecraft:bricks 20'.  See the GriefPrevention Flags page on spigotmc.org for more help.", null);
        this.addDefault(defaults, Messages.SetSpleefArena, "Now allowing some block types to be destroyed, and automatically regenerating them when players die in this area.", null);
        this.addDefault(defaults, Messages.UnSetSpleefArena, "Stopped overriding Grief Prevention's block breaking rules and generating blocks when players die in this area.", null);

        this.addDefault(defaults, Messages.EnableNoGrowth, "Blocks will no longer grow in this area", null);
        this.addDefault(defaults, Messages.DisableNoGrowth, "Blocks will now continue to grow in this area", null);

        this.addDefault(defaults, Messages.ExitFlightDisabled, "Flight disabled", null);
        this.addDefault(defaults, Messages.EnterFlightEnabled, "Flight enabled", null);

        this.addDefault(defaults, Messages.OwnerFlightEnabled, "The owner of this claim can now fly in this claim", null);
        this.addDefault(defaults, Messages.OwnerFlightDisabled, "The owner of this claim can no longer fly in this claim", null);

        this.addDefault(defaults, Messages.OwnerMemberFlightEnabled, "The owner and members with access trust or higher can now fly in this claim", null);
        this.addDefault(defaults, Messages.OwnerMemberFlightDisabled, "The owner and members of this claim can no longer fly in this claim", null);

        this.addDefault(defaults, Messages.EnabledNoEnterPlayer, "Enabled NoEnterPlayer for: {0}", "0: players to block");
        this.addDefault(defaults, Messages.DisabledNoEnterPlayer, "Disabled NoEnterPlayer", null);
        this.addDefault(defaults, Messages.NoEnterPlayerMessage, "You have been blocked from entering this claim", null);
        this.addDefault(defaults, Messages.PlayerRequired, "Player(s) required. Use a comma to separate", null);

        this.addDefault(defaults, Messages.PlayerWeatherRequired, "Weather required <sun/rain>", null);
        this.addDefault(defaults, Messages.PlayerWeatherSet, "Player weather in this claim has been set to {0}", "0: Weather to send");
        this.addDefault(defaults, Messages.PlayerWeatherUnSet, "Player weather has been unset in this claim", null);

        this.addDefault(defaults, Messages.PlayerTimeRequired, "Time required <day/noon/night/midnight>", null);
        this.addDefault(defaults, Messages.PlayerTimeSet, "Player time in this claim has been set to {0}", "0: Time to send");
        this.addDefault(defaults, Messages.PlayerTimeUnSet, "Player time has been unset in this claim", null);

        this.addDefault(defaults, Messages.PlayerGamemodeRequired, "Gamemode required <survival/creative/adventure/spectator>", null);
        this.addDefault(defaults, Messages.PlayerGamemodeSet, "Player gamemode in this claim has been set to {0}", "0: Gamemode to send");
        this.addDefault(defaults, Messages.PlayerGamemodeUnSet, "Player gamemode has been unset in this claim", null);
        this.addDefault(defaults, Messages.PlayerGamemode, "Your gamemode has been changed to {0}", "0: Gamemode to send");

        this.addDefault(defaults, Messages.EnableNoVineGrowth, "Vines will no longer grow in this area", null);
        this.addDefault(defaults, Messages.DisableNoVineGrowth, "Vines will now continue to grow in this area", null);

        this.addDefault(defaults, Messages.EnableNoSnowForm, "Snow will no longer form in this area", null);
        this.addDefault(defaults, Messages.DisableNoSnowForm, "Snow will now continue to form in this area", null);

        this.addDefault(defaults, Messages.EnableNoIceForm, "Ice will no longer form in this area", null);
        this.addDefault(defaults, Messages.DisableNoIceForm, "Ice will now continue to form in this area", null);

        this.addDefault(defaults, Messages.EnabledNoFireSpread, "Fire will no longer spread in this area", null);
        this.addDefault(defaults, Messages.DisabledNoFireSpread, "Fire will now continue to spread in this area", null);

        this.addDefault(defaults, Messages.EnableNoFireDamage, "Fire will no longer damage blocks in this area", null);
        this.addDefault(defaults, Messages.DisableNoFireDamage, "Fire will now continue to damage blocks in this area", null);

        this.addDefault(defaults, Messages.EnabledNoFallDamage, "Player will no longer take fall damage in this claim", null);
        this.addDefault(defaults, Messages.DisabledNoFallDamage, "Players will now continue to take fall damage in this claim", null);

        this.addDefault(defaults, Messages.EnabledNoExplosionDamage, "Players will no longer take damage caused by explosions in this claim", null);
        this.addDefault(defaults, Messages.DisabledNoExplosionDamage, "Players will now continue to take damage caused by explosions in this claim", null);

        this.addDefault(defaults, Messages.NoOwnerFlag, "You cannot set both OwnerFly and OwnerMemberFly flags in one claim", null);

        this.addDefault(defaults, Messages.ChangeBiomeSet, "The biome in this claim has been set to {0}. Relog to see the changes.", "0: Biome");
        this.addDefault(defaults, Messages.ChangeBiomeUnset, "The biome in this claim has been restored. Relog to see the changes.", null);

        this.addDefault(defaults, Messages.NoFlagInClaim, "This flag cannot be set in a claim", null);
        this.addDefault(defaults, Messages.NoFlagInWorld, "This flag cannot be set for a whole world", null);
        this.addDefault(defaults, Messages.NoFlagInServer, "This flag cannot be set for the whole server", null);

        this.addDefault(defaults, Messages.EnableNoOpenDoor, "Doors can no longer be opened in this area", null);
        this.addDefault(defaults, Messages.DisableNoOpenDoor, "Doors can now be opened in this area", null);
        this.addDefault(defaults, Messages.NoOpenDoorMessage, "You do not have permission to open {0} in this area", "0: DoorType");

        this.addDefault(defaults, Messages.EnabledNoVehicle, "Vehicles can no longer be placed in this area", null);
        this.addDefault(defaults, Messages.DisabledNoVehicle, "Vehicles can now be placed in this area", null);
        this.addDefault(defaults, Messages.NoPlaceVehicle, "You cannot place vehicles in this area", null);
        this.addDefault(defaults, Messages.NoEnterVehicle, "You can not enter vehicles in this area", null);
        this.addDefault(defaults, Messages.NoVehicleAllowed, "Vehicles are not allowed in this area", null);

        this.addDefault(defaults, Messages.EnabledNoMobSpawnsType, "The spawning of {0} has been disabled in this area", "0: Mob Types");
        this.addDefault(defaults, Messages.DisabledNoMobSpawnsType, "The flag mobs will now be able to spawn again in this area", null);
        this.addDefault(defaults, Messages.MobTypeRequired, "A mob type is required", null);
        this.addDefault(defaults, Messages.MobTypePerm, "You do not have permission to deny the spawning of {0}", "0: Mob Type");

        this.addDefault(defaults, Messages.EnabledNoItemDamage, "Items will no longer take damage in this area", null);
        this.addDefault(defaults, Messages.DisabledNoItemDamage, "Items will continue to take damage in this area", null);

        this.addDefault(defaults, Messages.EnabledRaidMemberOnly, "Only claim members can trigger raids in this area", null);
        this.addDefault(defaults, Messages.DisabledRaidMemberOnly, "Anyone can trigger raids in this area", null);
        this.addDefault(defaults, Messages.RaidMemberOnlyDeny, "You cannot initiate a raid in this area", null);

        this.addDefault(defaults, Messages.EnabledProtectNamedMobs, "Named mobs will no longer take damage in this area", null);
        this.addDefault(defaults, Messages.DisabledProtectNamedMobs, "Named mobs will continue to take damage in this area", null);

        this.addDefault(defaults, Messages.EnableNoElytra, "Players will no longer be able to glide in this area", null);
        this.addDefault(defaults, Messages.DisableNoElytra, "Players will now be able to glide in this area", null);
        
        this.addDefault(defaults, Messages.EnableViewContainers, "Players can now view (not manipulate) any container on your claim", null);
        this.addDefault(defaults, Messages.DisableViewContainers, "Players can no longer view containers on your claim", null);
        
        this.addDefault(defaults, Messages.EnableReadLecterns, "Players can now read (not manipulate) lecturns on your claim", null);
        this.addDefault(defaults, Messages.DisableReadLecterns, "Players can no longer read lecturns on your claim", null);

        this.addDefault(defaults, Messages.Prefix, "&7[&bGP&3Flags&7] &r", null);

        //load the config file
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(messagesFilePath));

        //for each message ID
        for (int i = 0; i < messageIDs.length; i++) {
            //get default for this message
            Messages messageID = messageIDs[i];
            CustomizableMessage messageData = defaults.get(messageID.name());

            //if default is missing, log an error and use some fake data for now so that the plugin can run
            if (messageData == null) {
                Util.log("Missing message for " + messageID.name() + ".  Please contact the developer.");
                messageData = new CustomizableMessage(messageID, "Missing message!  ID: " + messageID.name() + ".  Please contact a server admin.", null);
            }

            //read the message from the file, use default if necessary
            this.messages[messageID.ordinal()] = config.getString("Messages." + messageID.name() + ".Text", messageData.text);
            config.set("Messages." + messageID.name() + ".Text", this.messages[messageID.ordinal()]);
            this.messages[messageID.ordinal()] = this.messages[messageID.ordinal()].replace('$', (char) 0x00A7);

            if (messageData.notes != null) {
                messageData.notes = config.getString("Messages." + messageID.name() + ".Notes", messageData.notes);
                config.set("Messages." + messageID.name() + ".Notes", messageData.notes);
            }
        }

        //save any changes
        try {
            config.save(FlagsDataStore.messagesFilePath);
        } catch (IOException exception) {
            Util.log("Unable to write to the configuration file at \"" + FlagsDataStore.messagesFilePath + "\"");
        }

        defaults.clear();
        System.gc();
    }

    private void addDefault(HashMap<String, CustomizableMessage> defaults, Messages id, String text, String notes) {
        CustomizableMessage message = new CustomizableMessage(id, text, notes);
        defaults.put(id.name(), message);
    }

    synchronized public String getMessage(Messages messageID, String... args) {
        String message = messages[messageID.ordinal()];

        for (int i = 0; i < args.length; i++) {
            String param = args[i];
            message = message.replace("{" + i + "}", param);
        }
        return message;
    }

    void close() {
    }

}
