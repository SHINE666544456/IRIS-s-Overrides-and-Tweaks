package dev.skinoverride.hud;

import dev.skinoverride.ModConfig;
import dev.skinoverride.Ui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Status effects with name, level and a countdown. Flashes red when about to run out. */
public class EffectsWidget implements HudWidget {

    private static final int ROW_H = 24;
    private static final int ROW_GAP = 2;
    private static final int MAX_ROWS = 10;

    private record Row(Identifier sprite, String name, int ticks, boolean infinite, boolean beneficial) {
    }

    private final List<Row> rows = new ArrayList<>();
    private int w;
    private int h;

    @Override
    public String id() {
        return "effects";
    }

    @Override
    public String label() {
        return "Effect timers";
    }

    @Override
    public float defaultX() {
        return 1f;
    }

    @Override
    public float defaultY() {
        return 0f;
    }

    @Override
    public boolean enabled() {
        return ModConfig.get().effectsHud;
    }

    @Override
    public void prepare(MinecraftClient mc, boolean preview) {
        rows.clear();
        if (preview || mc.player == null) {
            rows.add(new Row(sprite("speed"), "Speed II", 20 * 95, false, true));
            rows.add(new Row(sprite("regeneration"), "Regeneration", 20 * 6, false, true));
            rows.add(new Row(sprite("poison"), "Poison", 20 * 40, false, false));
        } else {
            for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
                if (!inst.shouldShowIcon() || rows.size() >= MAX_ROWS) {
                    continue;
                }
                Identifier key = inst.getEffectType().getKey()
                        .map(k -> k.getValue())
                        .orElse(Identifier.ofVanilla("speed"));
                String name = inst.getEffectType().value().getName().getString();
                if (inst.getAmplifier() > 0) {
                    name += " " + roman(inst.getAmplifier() + 1);
                }
                rows.add(new Row(
                        Identifier.of(key.getNamespace(), "mob_effect/" + key.getPath()),
                        name,
                        inst.getDuration(),
                        inst.isInfinite(),
                        inst.getEffectType().value().isBeneficial()));
            }
        }
        if (rows.isEmpty()) {
            w = 0;
            h = 0;
            return;
        }
        int max = 0;
        for (Row r : rows) {
            max = Math.max(max, Math.max(
                    mc.textRenderer.getWidth(r.name()),
                    mc.textRenderer.getWidth(timeText(r))));
        }
        w = 4 + 18 + 5 + max + 6;
        h = rows.size() * (ROW_H + ROW_GAP) - ROW_GAP;
    }

    private static Identifier sprite(String name) {
        return Identifier.ofVanilla("mob_effect/" + name);
    }

    private static String roman(int n) {
        String[] numerals = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return n >= 1 && n < numerals.length ? numerals[n] : String.valueOf(n);
    }

    private static String timeText(Row r) {
        return r.infinite() ? "inf" : Ui.formatSeconds((r.ticks() + 19) / 20);
    }

    @Override
    public int width() {
        return w;
    }

    @Override
    public int height() {
        return h;
    }

    @Override
    public void render(DrawContext c, MinecraftClient mc, int x, int y, boolean preview) {
        ModConfig cfg = ModConfig.get();
        int cy = y;
        for (Row r : rows) {
            boolean low = !r.infinite() && r.ticks() <= cfg.effectsLowSeconds * 20;
            boolean flash = low && cfg.effectsFlash;
            boolean on = flash && Ui.blink();

            Ui.roundRect(c, x, cy, w, ROW_H, on ? 0xC0501818 : Ui.BG);
            int border = low ? (on || !cfg.effectsFlash ? Ui.BAD : 0xFF7A2B2B) : Ui.BORDER;
            Ui.roundBorder(c, x, cy, w, ROW_H, border);
            c.fill(x + 1, cy + 3, x + 3, cy + ROW_H - 3, r.beneficial() ? Ui.GOOD : Ui.BAD);

            c.drawGuiTexture(RenderPipelines.GUI_TEXTURED, r.sprite(), x + 5, cy + 3, 18, 18);

            c.drawTextWithShadow(mc.textRenderer, r.name(), x + 28, cy + 3, Ui.TEXT);
            int timeColor = low ? (on || !cfg.effectsFlash ? Ui.BAD : 0xFFB04040) : Ui.MUTED;
            c.drawTextWithShadow(mc.textRenderer, timeText(r), x + 28, cy + 13, timeColor);
            cy += ROW_H + ROW_GAP;
        }
    }
}
