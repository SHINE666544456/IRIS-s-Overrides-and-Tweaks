package dev.skinoverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

/**
 * Takes a screenshot with the whole HUD hidden (hotbar, chat, crosshair, our HUD elements, bubbles).
 * Player name tags are part of the world, so they stay visible.
 */
public final class CleanScreenshot {

    private static int ticksLeft;
    private static boolean hudWasHidden;

    private CleanScreenshot() {
    }

    public static void request(MinecraftClient mc) {
        if (ticksLeft > 0 || mc.currentScreen != null || mc.player == null) {
            return;
        }
        hudWasHidden = mc.options.hudHidden;
        mc.options.hudHidden = true;
        ticksLeft = 3; // let a few frames render without the HUD
    }

    public static void tick(MinecraftClient mc) {
        if (ticksLeft <= 0) {
            return;
        }
        if (mc.currentScreen != null) {
            mc.options.hudHidden = hudWasHidden;
            ticksLeft = 0;
            return;
        }
        if (--ticksLeft == 0) {
            ScreenshotRecorder.saveScreenshot(mc.runDirectory, mc.getFramebuffer(),
                    message -> mc.execute(() -> mc.inGameHud.getChatHud().addMessage(message)));
            mc.options.hudHidden = hudWasHidden;
        }
    }
}
