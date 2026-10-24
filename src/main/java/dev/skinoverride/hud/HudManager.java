package dev.skinoverride.hud;

import dev.skinoverride.ModConfig;
import dev.skinoverride.SkinOverrideClient;
import dev.skinoverride.WorldLabels;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.List;

/** Registers one HUD layer that draws every enabled widget at its saved position. */
public final class HudManager {

    public static final int PAD = 2;

    public static final List<HudWidget> WIDGETS = List.of(
            new InfoWidget(),
            new ArmorWidget(),
            new EffectsWidget(),
            new InspectWidget(),
            new AfkWidget());

    private HudManager() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of(SkinOverrideClient.MOD_ID, "hud"),
                (context, tickCounter) -> renderAll(context));

        // Our effect timers replace the vanilla effect icons while enabled.
        HudElementRegistry.replaceElement(
                VanillaHudElements.STATUS_EFFECTS,
                original -> (context, tickCounter) -> {
                    if (!ModConfig.get().effectsHud) {
                        original.render(context, tickCounter);
                    }
                });
    }

    private static void renderAll(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) {
            return;
        }
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        for (HudWidget w : WIDGETS) {
            if (!w.enabled() || !w.visible(mc)) {
                continue;
            }
            w.prepare(mc, false);
            if (w.width() <= 0 || w.height() <= 0) {
                continue;
            }
            int[] p = position(w, sw, sh, w.width(), w.height());
            w.render(context, mc, p[0], p[1], false);
        }
        WorldLabels.render(context, mc);
    }

    /** Top-left pixel position of a widget of the given size. */
    public static int[] position(HudWidget w, int sw, int sh, int ww, int wh) {
        float[] f = ModConfig.get().positions.get(w.id());
        float fx = f == null || f.length < 2 ? w.defaultX() : f[0];
        float fy = f == null || f.length < 2 ? w.defaultY() : f[1];
        int freeX = Math.max(0, sw - ww - PAD * 2);
        int freeY = Math.max(0, sh - wh - PAD * 2);
        return new int[]{PAD + Math.round(fx * freeX), PAD + Math.round(fy * freeY)};
    }

    /** Saves a widget's position from pixel coordinates. */
    public static void setPosition(HudWidget w, int x, int y, int sw, int sh, int ww, int wh) {
        int freeX = Math.max(1, sw - ww - PAD * 2);
        int freeY = Math.max(1, sh - wh - PAD * 2);
        float fx = Math.max(0f, Math.min(1f, (x - PAD) / (float) freeX));
        float fy = Math.max(0f, Math.min(1f, (y - PAD) / (float) freeY));
        ModConfig.get().positions.put(w.id(), new float[]{fx, fy});
    }

    public static void resetPositions() {
        ModConfig.get().positions.clear();
        ModConfig.get().save();
    }
}
