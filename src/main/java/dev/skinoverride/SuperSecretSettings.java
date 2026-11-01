package dev.skinoverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * "Super Secret Settings", the old joke button of Minecraft 1.7.2 that cycled through screen effects. The original
 * used real shaders; this version draws colored filters, scan lines, a grid and a vignette over the world instead
 * (under the HUD), which is as close as can be done without touching the game's shader system.
 */
public final class SuperSecretSettings {

    private static final String[] NAMES = {
            "Off", "Notch", "Desaturate", "Green", "Phosphor", "Scan Lines", "Sepia", "Pink", "Blue Hour",
            "Vignette", "Bits", "Night Vision"
    };

    private SuperSecretSettings() {
    }

    public static int count() {
        return NAMES.length;
    }

    public static String name(int index) {
        return NAMES[Math.floorMod(index, NAMES.length)];
    }

    /** Next effect (the old button's click). */
    public static void cycle(MinecraftClient mc) {
        ModConfig cfg = ModConfig.get();
        cfg.sssIndex = (cfg.sssIndex + 1) % NAMES.length;
        cfg.save();
        if (mc.player != null) {
            mc.inGameHud.setOverlayMessage(Text.literal("Super Secret Settings: " + NAMES[cfg.sssIndex]), false);
        }
    }

    public static void render(DrawContext c, MinecraftClient mc) {
        int index = ModConfig.get().sssIndex;
        if (index <= 0 || index >= NAMES.length || mc.player == null) {
            return;
        }
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        switch (index) {
            case 1 -> { // Notch: washed out with soft dark corners
                c.fill(0, 0, w, h, 0x30A0A0A0);
                vignette(c, w, h, 0x90);
            }
            case 2 -> c.fill(0, 0, w, h, 0x90808080);
            case 3 -> c.fill(0, 0, w, h, 0x6030FF40);
            case 4 -> { // Phosphor: green glow and lines
                c.fill(0, 0, w, h, 0x4030FF50);
                scanLines(c, w, h, 0x50);
            }
            case 5 -> scanLines(c, w, h, 0x70);
            case 6 -> c.fill(0, 0, w, h, 0x70A06A30);
            case 7 -> c.fill(0, 0, w, h, 0x55FF60B0);
            case 8 -> c.fill(0, 0, w, h, 0x682040C0);
            case 9 -> vignette(c, w, h, 0xC0);
            case 10 -> { // Bits: a chunky pixel grid
                int step = 3;
                for (int x = 0; x < w; x += step) {
                    c.fill(x, 0, x + 1, h, 0x40000000);
                }
                for (int y = 0; y < h; y += step) {
                    c.fill(0, y, w, y + 1, 0x40000000);
                }
            }
            case 11 -> { // Night Vision: bright green with dark corners
                c.fill(0, 0, w, h, 0x4A40FF60);
                vignette(c, w, h, 0xA0);
            }
            default -> {
            }
        }
    }

    private static void scanLines(DrawContext c, int w, int h, int alpha) {
        int color = (alpha << 24);
        for (int y = 0; y < h; y += 2) {
            c.fill(0, y, w, y + 1, color);
        }
    }

    /** Darkens the edges in steps, strongest at the very edge. */
    private static void vignette(DrawContext c, int w, int h, int maxAlpha) {
        int steps = 14;
        int depthX = w / 4;
        int depthY = h / 4;
        for (int i = 0; i < steps; i++) {
            int a = maxAlpha * (steps - i) / steps / 2;
            int color = a << 24;
            int dx = depthX * i / steps;
            int dy = depthY * i / steps;
            int nx = depthX * (i + 1) / steps;
            int ny = depthY * (i + 1) / steps;
            c.fill(dx, dy, nx, h - dy, color);            // left
            c.fill(w - nx, dy, w - dx, h - dy, color);    // right
            c.fill(dx, dy, w - dx, ny, color);            // top
            c.fill(dx, h - ny, w - dx, h - dy, color);    // bottom
        }
    }
}
