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
    public boolean zoomSmoothCamera = true;

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
    }

    /** HUD element id -> {x, y} as 0..1 fractions of the free screen space. */
    public Map<String, float[]> positions = new HashMap<>();

    public static ModConfig get() {
        if (instance == null) {
            instance = load();
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
                    return read;
                }
            } catch (Exception e) {
                SkinOverrideClient.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        return new ModConfig();
    }

    public void save() {
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
