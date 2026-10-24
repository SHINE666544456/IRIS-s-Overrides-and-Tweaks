package dev.skinoverride;

import net.minecraft.client.MinecraftClient;

/** Hold-to-zoom with smooth easing, scroll to change the zoom level. */
public final class Zoom {

    private static float prev = 1f;
    private static float cur = 1f;
    private static float factor = -1f;
    private static boolean held;
    private static boolean smoothApplied;
    private static boolean smoothBefore;

    private Zoom() {
    }

    public static void tick(MinecraftClient mc, boolean keyDown) {
        if (factor < 0) {
            factor = ModConfig.get().zoomDefault;
        }
        held = keyDown && mc.currentScreen == null && mc.player != null;
        float target = held ? factor : 1f;
        prev = cur;
        cur += (target - cur) * 0.4f;
        if (Math.abs(target - cur) < 0.01f) {
            cur = target;
        }

        boolean wantSmooth = held && ModConfig.get().zoomSmoothCamera;
        if (wantSmooth && !smoothApplied) {
            smoothBefore = mc.options.smoothCameraEnabled;
            mc.options.smoothCameraEnabled = true;
            smoothApplied = true;
        } else if (!wantSmooth && smoothApplied) {
            mc.options.smoothCameraEnabled = smoothBefore;
            smoothApplied = false;
        }
    }

    /** How much the field of view is divided by (1 = no zoom). */
    public static float divisor(float tickProgress) {
        return prev + (cur - prev) * tickProgress;
    }

    /** Returns true if the scroll was used for zooming (so the hotbar must not scroll). */
    public static boolean onScroll(double vertical) {
        if (!held || vertical == 0) {
            return false;
        }
        factor = (float) Math.max(1.5, Math.min(30.0, factor * (vertical > 0 ? 1.2 : 1 / 1.2)));
        return true;
    }
}
