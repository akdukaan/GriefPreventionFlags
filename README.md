# GriefPreventionFlags
GriefPreventionFlags is a plugin to allow players to customize their GriefPrevention claims with claimflags.

This is a fork from the original author [BigScary](https://github.com/BigScary/GriefPreventionFlags) and the previous maintainers [ShaneBeee](https://github.com/ShaneBeee/GriefPreventionFlags) and [lewysDavies](https://github.com/lewysDavies/GriefPreventionFlags).

### Download
Download the latest release from the [Birdflop Discord](https://discord.gg/MBdsxAR) or compile it yourself from here.


### What does this fork change from the [Spigot release](https://www.spigotmc.org/resources/gpflags.55773/)?
- Removed support for GriefPrevention 16.17.1 and below. You can download GriefPrevention 16.17.2-SNAPSHOT from [here](https://ci.appveyor.com/api/buildjobs/g11ovt3huylwdu7e/artifacts/target%2FGriefPrevention.jar). 
- Added support for shorthand hex colors (&#rrggbb).
- Added the `ViewContainers` claimflag which allows players to view, but not manipulate, any container on a claim.
- Added the `ReadLecterns` claimflag which allows players to read but not manipulate any lectern with a book on a claim.
- Fixed the `ChangeBiome` claimflag.
- Fixed the `NoFlight` claimflag.
- Fixed a null pointer exception with global instances of the `NoVehicle` claim flag.
- Fixed a bug where `NoMobDamage` would also prevent players from renaming mobs.
- Fixed a bug where `NoIceForm` would not prevent frostwalker's frosted ice from forming.
- Fixed a bug where `NoVineGrowth` would allow the growth of glow litchen, weeping vines, and twisting vines.
- Fixed a bug where `AllowPvP` does not apply after a server restart.
- Fixed a bug where `AllowPvP` could duplicate arrows.
- Fixed a bug where `InfiniteArrows` could duplicate arrows. 
- Fixed a bug where entering or exiting a claim from beyond world build height would allow the player to bypass PlayerClaimBorderEvent.
- Fixed a bug where resizing claims could allow players to bypass PlayerClaimBorderEvent.
- Fixed a bug where movement-based default flags would trigger at the wrong times. 
- Removed the ability for players to set messages with `NoEnter`. My reasoning can be found in a pinned message in the [Birdflop Discord](https://discord.gg/MBdsxAR).
