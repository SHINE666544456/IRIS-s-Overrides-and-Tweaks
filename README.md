# IRIS'S Overrides and Tweaks (Fabric, Minecraft 1.21.11)

Version 1.0.3.

A **client-side only** mod. Press **K** to open a GUI, pick any player, and change how their skin looks **on your screen**. No server mod needed, and nobody else sees the change.

## Features
Press **K** for skins, **J** for settings (both rebindable under Controls).

**Skins**
- 3D preview of the selected player. Hover a skin to preview it before applying.
- Add skins by dragging a `.png` (e.g. from The Skindex) onto the window, dropping it in `config/skinoverride/skins/`, or typing a Minecraft username and pressing **Download skin**.
- Everything is remembered: assignments are saved by player UUID, the GUI reopens on your last player, and **Restore last** brings back a player's previous skin after a reset.
- Looking at a player when you press K opens the GUI with them selected.
- Per-player model override (auto / wide / slim). Capes and elytra are kept from the real account.

**HUD** (all movable: Settings -> Edit HUD layout, drag with the mouse, edges and center snap)
- **Armor & held items:** worn armor, main hand and off hand with durability numbers or percent. Slots flash red when durability is low.
- **Effect timers:** replaces the vanilla effect icons with name, level and countdown; flashes red when about to run out.
- **Crosshair inspect:** a panel for the player you are looking at: health, held/worn items and their enchantments. Off by default; enable it in Settings.
- **AFK list:** optional small list of players currently AFK.

**AFK detection**
- Players you can see are marked AFK after they stand still and stop turning for a configurable time. The tag shows in the skin screen, the inspect overlay and the optional HUD list.

**Settings screen:** toggles, sliders and option cyclers for every feature above.

## Limits
- Skins only change on your own screen. Other players keep seeing the real skin.
- Inspect and the 3D slots only work for players loaded in your world, and only show held/worn items: servers never send other players' inventories to your client, so no client mod can show them.
- AFK detection only watches players near you. A player starts counting from the moment you first see them, and being pushed (water, pistons) counts as moving.
- Mojang rate-limits username lookups; wait a minute if one fails.
- Mixins use Yarn names for 1.21.11. Other Minecraft versions need updates.

## Install
1. Install Fabric Loader 0.19.5+ and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.11.
2. Drop the jar from the **Releases** page (or `build/libs/`) into `.minecraft/mods`.

## Build
```
./gradlew build
```
The jar lands in `build/libs/` (use the one without `-sources`). Requires JDK 21.

## Put it on GitHub
```
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<you>/iris-overrides-and-tweaks.git
git push -u origin main
```
Every push builds the mod with GitHub Actions (download the jar from the run's **Artifacts**). To publish a release jar:
```
git tag v1.0.3
git push origin v1.0.3
```
