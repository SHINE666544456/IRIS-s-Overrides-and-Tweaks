# IRIS'S Overrides and Tweaks (Fabric, Minecraft 1.21.11)

Version 1.4.0.

A **client-side only** mod. Press **K** to open a GUI, pick any player, and change how their skin looks **on your screen**. No server mod needed, and nobody else sees the change.

## Features
Press **K** for skins and **J** for settings. Other keys (all rebindable under Controls, search "IRIS"): **C** zoom (hold), **G** fullbright, **F9** clean screenshot, **F8** freecam, **F7** light level overlay.

**Skins**
- 3D preview of the selected player; hover a skin to preview it before applying.
- Add skins by dragging a `.png` (e.g. from The Skindex) onto the window, dropping it in `config/skinoverride/skins/`, or typing a Minecraft username and pressing **Download skin**.
- Assignments are saved by player UUID, the GUI reopens on your last player, and **Restore last** brings back a player's previous skin.

**HUD** (Settings -> Edit HUD layout: drag to move, **scroll over an element to resize it**, right-click to reset its size)
- **Armor & held items:** row or column (to sit beside the hotbar), durability numbers or percent, flashing red when low.
- **Effect timers:** icon, name, level and countdown. The bar on the left drains and shifts green -> yellow -> orange -> red as the effect runs out.
- **Coordinates & FPS:** XYZ, facing direction, FPS and ping.
- **Day & time:** permanent day counter with a ticking in-game clock (24h or AM/PM, optional seconds, optional playtime line).
- **Subtitles:** the vanilla sound captions rebuilt as a movable, resizable element with direction arrows.
- **Keystrokes & CPS:** WASD, space and the mouse buttons light up when held; clicks per second under LMB / RMB.
- **Join / leave pop-ups:** a small notice when a player comes online or goes offline (optionally only for players on your per-player list), with a soft sound.
- **Crosshair inspect** and an **AFK list** (both optional).

**Pause menu panel:** the day number, exact ticking time of day, time until sunset / next day, and playtime (this world or server, this session, all worlds) plus the real clock.

**AFK detection:** tags above heads, in the skin screen, in the inspect overlay and the optional HUD list.

**Chat** (Settings -> Chat)
- Name and message colors for everyone, or per player with a color grid or a custom hex code. Solid, rainbow or color-cycle. Separate colors for join, leave, advancement and death messages.
- **Emotes:** `:smile: :heart: :fire: :poggies:` and more (16 faces/symbols). **Armor icons:** `:Dchest:` (diamond chestplate), `:LLeg:` (leather leggings), `:Ihelm:`, `:Gboot:` ... Letters: L leather, C chainmail, I iron, G gold, D diamond, N netherite; pieces: helm, chest, leg, boot. Long names work too (`:diamond_chestplate:`).
- **Speech bubbles** above nearby players, and **text to speech** through Minecraft's narrator.

**Freecam (F8):** the camera detaches from your player and flies freely through blocks. WASD moves, jump/sneak go up/down, sprint is a x3 boost, scroll changes the speed. Your character stays where it is (and is drawn there), actions are blocked while flying, and it exits if you get hurt. Everything is a setting.

**Light level:** a line in the info panel (block / sky light where you stand) and an overlay (F7) of colored squares on the floor around you: red = 0 (hostile mobs can spawn), yellow = 1-7, green = 8+.

**Combat** (Settings -> Combat; everything has an on/off switch)
- **Damage numbers:** floating numbers when a creature's health drops. "Only my hits" shows just creatures you were attacking; turn it off for all nearby damage (and optionally healing). Size, duration, decimals and color (auto by damage, a fixed color, rainbow or cycle) are adjustable.
- **Hit flash color:** recolors the red tint on hurt creatures (any color, rainbow or cycle) with adjustable strength.
- **Crosshair:** Vanilla, Dot, Plus, Plus + dot, Circle, Square or X with size, thickness, gap, color, outline, red-on-target and a hit marker. (Custom styles replace the vanilla attack indicator.)
- **Hotbar cooldowns:** a colored wipe over items on cooldown, the seconds left, and a green flash when ready.

**Nametags:** set a nametag color for everyone or per player (solid, rainbow or cycling; the rainbow animates).

**Visuals**
- Reveal invisible players, fullbright, instant zoom (hold C, scroll to change the level, no animation), clean screenshot (HUD hidden, name tags stay).

**Theme** (Settings -> Theme): presets (including *Cherry Blossom* and *Pink Night*), accent color, background color and panel opacity for every IRIS menu and HUD box.

## Limits
- Skins only change on your own screen. Other players keep seeing the real skin.
- Inspect and the 3D slots only work for players loaded in your world, and only show held/worn items: servers never send other players' inventories to your client, so no client mod can show them.
- Chat colors and emotes need servers that send normal chat messages; some chat plugins format messages in ways that can't be recolored. Colors are fixed when a message arrives, so rainbow does not animate.
- Speech bubbles and AFK tags are drawn from the camera's point of view and can be slightly off while your field of view is changing (sprinting, effects).
- AFK detection only watches players near you. A player starts counting from the moment you first see them, and being pushed (water, pistons) counts as moving.
- "Only my hits" is judged from your attacks (you pressed attack while aiming at the creature); the game does not tell the client who dealt each hit, so arrows and other indirect hits only show with "Only my hits" off.
- Light overlay and freecam use the client's own data and rendering hooks; the overlay shows squares through walls within its radius.
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
git tag v1.4.0
git push origin v1.4.0
```
