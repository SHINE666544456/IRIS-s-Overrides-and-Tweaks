package dev.skinoverride;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public int afkSeconds = 60;

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
