package dev.skinoverride;

import dev.skinoverride.hud.HudManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Fullbright.restore());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AfkTracker.tick(client);
            Zoom.tick(client, zoomKey.isPressed());
            Fullbright.tick(client);
            CleanScreenshot.tick(client);

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
