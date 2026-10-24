# IRIS'S Overrides and Tweaks (Fabric, Minecraft 1.21.11)

Version 1.1.0.

A **client-side only** mod. Press **K** to open a GUI, pick any player, and change how their skin looks **on your screen**. No server mod needed, and nobody else sees the change.

## Features
Press **K** for skins and **J** for settings. Other keys (all rebindable under Controls, search "IRIS"): **C** zoom (hold), **G** fullbright, **F9** clean screenshot.

**Skins**
- 3D preview of the selected player; hover a skin to preview it before applying.
- Add skins by dragging a `.png` (e.g. from The Skindex) onto the window, dropping it in `config/skinoverride/skins/`, or typing a Minecraft username and pressing **Download skin**.
- Assignments are saved by player UUID, the GUI reopens on your last player, and **Restore last** brings back a player's previous skin.
- Per-player model override (auto / wide / slim). Capes and elytra are kept from the real account.

**HUD** (all movable: Settings -> Edit HUD layout, drag with the mouse; edges and center snap)
- **Armor & held items:** horizontal row or vertical column (so it can sit beside the hotbar), durability numbers or percent, flashing red when low.
- **Effect timers:** icon, name, level and countdown; flashes red when about to run out.
- **Coordinates & FPS:** XYZ, facing direction, FPS and ping in one small panel.
- **Crosshair inspect:** health, held/worn items and enchantments of the player you look at (off by default).
- **AFK list:** optional list of players currently AFK.

**AFK detection:** players you can see are marked AFK after standing still for a configurable time. Shown as a tag above their head, in the skin screen, the inspect overlay and the optional HUD list.

**Chat** (Settings -> Chat)
- Name and message colors for everyone, or per player (Settings -> Chat -> Per-player colors). Colors can be solid, **rainbow** (every letter) or **color cycle** (each new message gets the next color).
- Separate colors for join, leave, advancement and death messages.
- **Emotes:** type `:smile:`, `:heart:`, `:fire:` ... and 16 built-in pixel emotes appear (optionally also `:)` `<3`). Only you see them.
- **Speech bubbles:** chat messages float above nearby players' heads.
- **Text to speech:** turns on Minecraft's built-in narrator for chat (voice comes from your OS).

**Visuals** (Settings -> Visuals)
- Reveal invisible players and mobs (client-side only).
- Fullbright toggle.
- Zoom: hold **C**, scroll to change the zoom level, optional smooth camera.
- **Clean screenshot:** hides the whole HUD (hotbar, chat, crosshair and all mod overlays); player name tags stay.

## Limits
- Skins only change on your own screen. Other players keep seeing the real skin.
- Inspect and the 3D slots only work for players loaded in your world, and only show held/worn items: servers never send other players' inventories to your client, so no client mod can show them.
- Chat colors and emotes need servers that send normal chat messages; some chat plugins format messages in ways that can't be recolored. Colors are fixed when a message arrives, so rainbow does not animate.
- Speech bubbles and AFK tags are drawn from the camera's point of view and can be slightly off while your field of view is changing (sprinting, effects).
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
git tag v1.1.0
git push origin v1.1.0
```
