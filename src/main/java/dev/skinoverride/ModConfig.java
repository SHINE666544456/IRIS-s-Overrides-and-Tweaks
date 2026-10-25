package dev.skinoverride;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.skinoverride.chat.ChatColors;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** All settings, saved to config/skinoverride/settings.json. Edited from the Settings screen. */
public final class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig instance;

    // armor / held items HUD
    public boolean armorHud = true;
    public boolean armorHeld = true;
    public boolean armorOffhand = false;
    /** 0 = off, 1 = remaining durability, 2 = percent */
    public int armorDurability = 1;
    public boolean armorLowWarning = true;
    /** 0 = horizontal row, 1 = vertical column */
    public int armorLayout = 0;
    public int armorLowPercent = 15;

    // effect timers
    public boolean effectsHud = true;
    public boolean effectsFlash = true;
    public int effectsLowSeconds = 10;

    // inspect overlay (player under the crosshair)
    public boolean inspectHud = false;
    public boolean inspectEnchants = true;
    public boolean inspectHealth = true;
    public int inspectRange = 24;

    // AFK detection
    public boolean afkTracking = true;
    public boolean afkHudList = false;
    public boolean afkTags = true;
    public int afkSeconds = 60;

    // info panel
    public boolean infoHud = true;
    public boolean infoCoords = true;
    public boolean infoDirection = true;
    public boolean infoFps = true;
    public boolean infoPing = false;

    // visuals
    public boolean revealInvisible = true;
    public boolean fullbright = false;
    public int zoomDefault = 4;
    /** The vanilla cinematic camera makes the mouse feel slippery, so it is off by default. */
    public boolean zoomCinematic = false;

    // chat
    public boolean chatCustom = true;
    public int chatDefaultNameColor = ChatColors.DEFAULT;
    public int chatDefaultMsgColor = ChatColors.DEFAULT;
    public int chatJoinColor = ChatColors.DEFAULT;
    public int chatLeaveColor = ChatColors.DEFAULT;
    public int chatAdvancementColor = ChatColors.DEFAULT;
    public int chatDeathColor = ChatColors.DEFAULT;
    public boolean chatEmotes = true;
    public boolean chatEmoteFaces = false;
    public boolean chatBubbles = false;
    public int chatBubbleSeconds = 6;
    public int chatBubbleRange = 24;
    public boolean chatTts = false;
    public String ttsPrevMode = "OFF";
    /** lower-case player name -> colors */
    public Map<String, PlayerChat> chatPlayers = new HashMap<>();

    public static class PlayerChat {
        public String name = "";
        public int nameColor = ChatColors.DEFAULT;
        public int msgColor = ChatColors.DEFAULT;
        public int tagColor = ChatColors.DEFAULT;
    }

    // subtitles
    public boolean subtitlesHud = true;

    // day counter / clock
    public boolean clockHud = true;
    public boolean clockSeconds = true;
    public boolean clock24h = true;
    public boolean clockPlaytime = false;
    public boolean pauseInfo = true;

    // nametags
    public int nametagDefaultColor = ChatColors.DEFAULT;

    // keystrokes / CPS
    public boolean keystrokesHud = false;
    public boolean keystrokesMouse = true;
    public boolean keystrokesCps = true;
    public boolean keystrokesSpace = true;

    // join / leave pop-ups
    public boolean notifyPopups = true;
    public boolean notifyJoin = true;
    public boolean notifyLeave = true;
    public boolean notifySound = true;
    public boolean notifyOnlyListed = false;

    // light level
    public boolean infoLight = false;
    public boolean lightOverlay = false;
    public int lightRadius = 6;
    public boolean lightNumbers = true;

    // freecam
    public int freecamSpeed = 12;
    public boolean freecamDamageOff = true;
    public boolean freecamBlockActions = true;
    public boolean freecamIndicator = true;
    public boolean freecamShowBody = true;

    // damage numbers
    public boolean dmgNumbers = true;
    public boolean dmgOnlyMine = true;
    public boolean dmgShowHeal = false;
    public boolean dmgDecimals = true;
    /** size in tenths: 10 = normal */
    public int dmgSize = 10;
    /** how long a number floats, in tenths of a second */
    public int dmgDuration = 12;
    /** DEFAULT = color by damage */
    public int dmgColor = ChatColors.DEFAULT;

    // hit flash (the red tint on hurt entities)
    public boolean hitColorOn = false;
    public int hitColor = ChatColors.DEFAULT;
    public int hitOpacity = 70;

    // crosshair
    /** 0 vanilla, 1 dot, 2 plus, 3 plus + dot, 4 circle, 5 square, 6 X */
    public int crosshairStyle = 0;
    public int crosshairSize = 5;
    public int crosshairThickness = 1;
    public int crosshairGap = 2;
    public int crosshairColor = ChatColors.DEFAULT;
    public boolean crosshairOutline = true;
    public boolean crosshairTargetColor = true;
    public boolean crosshairHitMarker = true;

    // hotbar cooldowns
    public boolean cooldownOverlay = true;
    public boolean cooldownNumbers = true;
    public boolean cooldownReadyFlash = true;
    public int cooldownColor = ChatColors.DEFAULT;

    // GUI theme
    public int themePreset = 1;
    public int themeAccent = 0x35D0BA;
    public int themeBg = 0x111420;
    public int themeOpacity = 88;

    /** HUD element id -> size multiplier (1.0 = normal) */
    public Map<String, Float> hudScale = new HashMap<>();

    /** HUD element id -> {x, y} as 0..1 fractions of the free screen space. */
    public Map<String, float[]> positions = new HashMap<>();

    public static ModConfig get() {
        if (instance == null) {
            instance = load();
            Ui.applyTheme(instance);
        }
        return instance;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("skinoverride").resolve("settings.json");
    }

    private static ModConfig load() {
        Path path = file();
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ModConfig read = GSON.fromJson(reader, ModConfig.class);
                if (read != null) {
                    if (read.positions == null) {
                        read.positions = new HashMap<>();
                    }
                    if (read.chatPlayers == null) {
                        read.chatPlayers = new HashMap<>();
                    }
                    if (read.hudScale == null) {
                        read.hudScale = new HashMap<>();
                    }
                    return read;
                }
            } catch (Exception e) {
                SkinOverrideClient.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        return new ModConfig();
    }

    public void save() {
        Ui.applyTheme(this);
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            SkinOverrideClient.LOGGER.warn("Could not save {}", path, e);
        }
    }
}
