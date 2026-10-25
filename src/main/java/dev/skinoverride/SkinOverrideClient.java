package dev.skinoverride;

import dev.skinoverride.hud.HudManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/** Client entrypoint: keybinds, HUD layer and the AFK tracker. */
public class SkinOverrideClient implements ClientModInitializer {

    public static final String MOD_ID = "skinoverride";
    public static final Logger LOGGER = LoggerFactory.getLogger("IRIS'S Overrides and Tweaks");

    private static KeyBinding skinsKey;
    private static KeyBinding settingsKey;
    private static KeyBinding zoomKey;
    private static KeyBinding fullbrightKey;
    private static KeyBinding shotKey;
    private static KeyBinding freecamKey;
    private static KeyBinding lightKey;

    @Override
    public void onInitializeClient() {
        SkinStore.init();
        ModConfig.get();
        HudManager.register();

        skinsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KeyBinding.Category.MISC));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyBinding.Category.MISC));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, KeyBinding.Category.MISC));
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.fullbright", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KeyBinding.Category.MISC));
        shotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.cleanshot", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F9, KeyBinding.Category.MISC));

        freecamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.freecam", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, KeyBinding.Category.MISC));
        lightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skinoverride.light", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, KeyBinding.Category.MISC));

        // Freecam must grab the movement keys before the game reads them.
        ClientTickEvents.START_CLIENT_TICK.register(Freecam::startTick);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Fullbright.restore();
            PlayTime.save();
        });

        // Day counter, clock and playtime on the pause menu.
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                ScreenEvents.afterRender(screen).register((s, context, mouseX, mouseY, tickDelta) ->
                        PauseInfo.render(context, client, tickDelta));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AfkTracker.tick(client);
            DayClock.tick(client);
            Subtitles.ensureRegistered(client);
            Zoom.tick(client, zoomKey.isPressed());
            Fullbright.tick(client);
            CleanScreenshot.tick(client);
            Notifications.tick(client);
            LightOverlay.tick(client);
            DamageNumbers.tick(client);
            HitColor.tick(client);

            while (freecamKey.wasPressed()) {
                Freecam.toggle(client);
            }
            while (lightKey.wasPressed()) {
                ModConfig cfg = ModConfig.get();
                cfg.lightOverlay = !cfg.lightOverlay;
                cfg.save();
                if (client.player != null) {
                    client.inGameHud.setOverlayMessage(Text.literal("Light overlay " + (cfg.lightOverlay ? "on" : "off")), false);
                }
            }

            while (fullbrightKey.wasPressed()) {
                ModConfig cfg = ModConfig.get();
                cfg.fullbright = !cfg.fullbright;
                cfg.save();
                if (client.player != null) {
                    client.inGameHud.setOverlayMessage(Text.literal("Fullbright " + (cfg.fullbright ? "on" : "off")), false);
                }
            }
            while (shotKey.wasPressed()) {
                CleanScreenshot.request(client);
            }

            while (skinsKey.wasPressed()) {
                // If you're looking at a player, open the GUI with them preselected.
                UUID target = client.targetedEntity instanceof AbstractClientPlayerEntity p ? p.getUuid() : null;
                client.setScreen(new SkinOverrideScreen(client.currentScreen, target));
            }
            while (settingsKey.wasPressed()) {
                client.setScreen(new SettingsScreen(client.currentScreen));
            }
        });
    }
}
