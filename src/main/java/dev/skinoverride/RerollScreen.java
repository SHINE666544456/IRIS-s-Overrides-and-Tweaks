package dev.skinoverride;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Trade reroller setup as a wishlist: click enchantments on the left to add them, set the lowest level and the
 * highest price you accept for each on the right, then Start. The reroller stops at the first trade that fits any
 * entry. Each enchantment shows its highest level and the cheapest price it can ever have.
 */
public class RerollScreen extends ThemedScreen {

    private static final int ROW_H = 14;
    private static final int WISH_H = 40;

    private final Screen parent;
    private final ModConfig cfg = ModConfig.get();
    private final List<String> all = EnchantInfo.ids();
    private List<String> shown = new ArrayList<>();
    private TextFieldWidget search;
    private double scrollL;
    private double scrollR;
    private String message = "";
    private long messageUntil;

    private int px, py, pw, ph, listW, listTop, listBottom, rx, rw, wishTop, wishBottom;

    public RerollScreen(Screen parent) {
        super(Text.literal("Trade reroller"));
        this.parent = parent;
    }

    private void say(String text) {
        message = text;
        messageUntil = System.currentTimeMillis() + 5000L;
    }

    private void filter() {
        String q = search == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        shown = new ArrayList<>();
        for (String id : all) {
            if (q.isEmpty() || id.contains(q)) {
                shown.add(id);
            }
        }
        scrollL = 0;
    }

    private ModConfig.Wish wishFor(String id) {
        for (ModConfig.Wish w : cfg.rerollWishes) {
            if (w.enchant.equals(id)) {
                return w;
            }
        }
        return null;
    }

    @Override
    protected void build() {
        pw = Math.min(this.width - 20, 540);
        ph = this.height - 24 - 34;
        px = (this.width - pw) / 2;
        py = 24;
        listW = 190;
        listTop = py + 44;
        listBottom = py + ph - 4;
        rx = px + listW + 14;
        rw = px + pw - 8 - rx;
        wishTop = py + 34;
        wishBottom = py + ph - 52;

        this.search = new TextFieldWidget(this.textRenderer, px + 4, py + 22, listW, 16, Text.literal("Search enchantments"));
        this.search.setPlaceholder(Text.literal("Search enchantment...").formatted(Formatting.DARK_GRAY));
        this.search.setMaxLength(30);
        this.search.setChangedListener(t -> filter());
        this.addDrawableChild(this.search);
        filter();

        String[] methods = {"Auto", "Trade Cycling button", "Break job block"};
        int half = (rw - 4) / 2;
        button(rx, py + ph - 46, half, 18, () -> "Method: " + methods[MathHelper.clamp(cfg.rerollMethod, 0, 2)],
                () -> true, false, () -> {
                    cfg.rerollMethod = (cfg.rerollMethod + 1) % 3;
                    cfg.save();
                });
        button(rx + half + 4, py + ph - 46, half, 18, () -> "Max tries: " + cfg.rerollMaxTries, () -> true, false, () -> {
            int[] steps = {50, 100, 300, 600, 1000};
            int next = steps[0];
            for (int step : steps) {
                if (step > cfg.rerollMaxTries) {
                    next = step;
                    break;
                }
            }
            cfg.rerollMaxTries = next;
            cfg.save();
        });
        button(rx, py + ph - 24, rw, 20,
                () -> TradeReroller.active() ? "Stop rerolling" : "Start rerolling", () -> true, true, () -> {
                    if (TradeReroller.active()) {
                        TradeReroller.stop(this.client, "Reroll stopped");
                        say("Stopped");
                        return;
                    }
                    cfg.save();
                    String error = TradeReroller.start(this.client, null);
                    if (error != null) {
                        say(error);
                    } else {
                        this.client.setScreen(null);
                    }
                });

        button(px + (pw - 110) / 2, this.height - 28, 110, 20, "Done", false, this::close);
    }

    private double maxScrollL() {
        return Math.max(0, shown.size() * ROW_H + 4 - (listBottom - listTop));
    }

    private double maxScrollR() {
        return Math.max(0, cfg.rerollWishes.size() * WISH_H + 4 - (wishBottom - wishTop));
    }

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        Ui.panel(c, px, py, pw, ph);
        Ui.header(c, this.textRenderer, "Villager trade reroller", px + 1, py + 1, pw - 2);
        Ui.panel(c, px + 4, listTop, listW, listBottom - listTop);
        Ui.panel(c, rx, wishTop, rw, wishBottom - wishTop);
        super.render(c, mouseX, mouseY, delta);

        // ---- enchantment list ----
        scrollL = MathHelper.clamp(scrollL, 0.0, maxScrollL());
        c.enableScissor(px + 5, listTop + 1, px + 3 + listW, listBottom - 1);
        for (int i = 0; i < shown.size(); i++) {
            int y = listTop + 3 + i * ROW_H - (int) scrollL;
            if (y + ROW_H < listTop || y > listBottom) {
                continue;
            }
            String id = shown.get(i);
            boolean wished = wishFor(id) != null;
            boolean hov = Ui.inside(mouseX, mouseY, px + 4, y, listW, ROW_H) && mouseY >= listTop && mouseY < listBottom;
            if (wished) {
                Ui.roundRect(c, px + 7, y, listW - 6, ROW_H, Ui.withAlpha(Ui.ACCENT, 0x70));
            } else if (hov) {
                Ui.roundRect(c, px + 7, y, listW - 6, ROW_H, 0x40FFFFFF);
            }
            Ui.text(c, this.textRenderer, EnchantInfo.pretty(id), px + 11, y + 3, Ui.TEXT);
            String info = "max " + EnchantInfo.roman(EnchantInfo.maxLevel(id)) + " | from " + EnchantInfo.minPrice(id, 1);
            Ui.text(c, this.textRenderer, info, px + 3 + listW - 6 - this.textRenderer.getWidth(info), y + 3,
                    EnchantInfo.treasure(id) ? Ui.WARN : Ui.MUTED);
        }
        c.disableScissor();
        Ui.text(c, this.textRenderer, "Click to add or remove. Orange = treasure (double price).", px + 4, listBottom + 1 - 0, Ui.MUTED);

        // ---- wishlist ----
        Ui.text(c, this.textRenderer, "Wishlist: stops at the first match", rx, py + 24, Ui.MUTED);
        scrollR = MathHelper.clamp(scrollR, 0.0, maxScrollR());
        c.enableScissor(rx + 1, wishTop + 1, rx + rw - 1, wishBottom - 1);
        if (cfg.rerollWishes.isEmpty()) {
            Ui.text(c, this.textRenderer, "Empty. Click enchantments on the left.", rx + 8, wishTop + 10, Ui.MUTED);
        }
        for (int i = 0; i < cfg.rerollWishes.size(); i++) {
            ModConfig.Wish w = cfg.rerollWishes.get(i);
            int y = wishTop + 3 + i * WISH_H - (int) scrollR;
            if (y + WISH_H < wishTop || y > wishBottom) {
                continue;
            }
            int x0 = rx + 6;
            int max = EnchantInfo.maxLevel(w.enchant);
            Ui.card(c, rx + 3, y, rw - 6, WISH_H - 3, Ui.BORDER);
            Ui.text(c, this.textRenderer, EnchantInfo.pretty(w.enchant) + "  (max " + EnchantInfo.roman(max) + ")", x0, y + 4, Ui.ACCENT);
            smallButton(c, mouseX, mouseY, rx + rw - 20, y + 2, 14, "x");

            int ly = y + 16;
            Ui.text(c, this.textRenderer, "Level", x0, ly + 2, Ui.MUTED);
            smallButton(c, mouseX, mouseY, x0 + 32, ly, 12, "-");
            Ui.text(c, this.textRenderer, EnchantInfo.roman(w.minLevel) + "+", x0 + 47, ly + 2, Ui.TEXT);
            smallButton(c, mouseX, mouseY, x0 + 66, ly, 12, "+");

            int cx = x0 + 92;
            Ui.text(c, this.textRenderer, "Max price", cx, ly + 2, Ui.MUTED);
            smallButton(c, mouseX, mouseY, cx + 46, ly, 18, "-8");
            smallButton(c, mouseX, mouseY, cx + 66, ly, 14, "-1");
            Ui.text(c, this.textRenderer, String.valueOf(w.maxCost), cx + 83, ly + 2, Ui.TEXT);
            smallButton(c, mouseX, mouseY, cx + 100, ly, 14, "+1");
            smallButton(c, mouseX, mouseY, cx + 116, ly, 18, "+8");

            int lo = EnchantInfo.minPrice(w.enchant, w.minLevel);
            int hi = EnchantInfo.maxPrice(w.enchant, w.minLevel);
            boolean impossible = w.maxCost < lo;
            String hint = impossible
                    ? "Cheapest possible at this level is " + lo + ": it will never match."
                    : "At this level the price is " + lo + " to " + hi + " emeralds.";
            Ui.text(c, this.textRenderer, this.textRenderer.trimToWidth(hint, rw - 16), x0, y + 29, impossible ? Ui.BAD : Ui.MUTED);
        }
        c.disableScissor();

        boolean tc = TradeReroller.cyclingModLoaded();
        String note = System.currentTimeMillis() < messageUntil ? message
                : TradeReroller.status() != null && !TradeReroller.status().isEmpty() ? TradeReroller.status()
                : tc ? "Trade Cycling found: Auto clicks its button. Escape stops a reroll."
                : "Trade Cycling not found: Auto breaks the job block. Escape stops a reroll.";
        Ui.text(c, this.textRenderer, this.textRenderer.trimToWidth(note, rw), rx, py + ph - 62, Ui.ACCENT);

        drawButtons(c, mouseX, mouseY);
    }

    private void smallButton(DrawContext c, int mx, int my, int x, int y, int w, String label) {
        Ui.button(c, this.textRenderer, x, y, w, 12, label, Ui.inside(mx, my, x, y, w, 12), true, false);
    }

    private boolean hit(double mx, double my, int x, int y, int w) {
        return Ui.inside(mx, my, x, y, w, 12);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled) || clickButtons(click)) {
            return true;
        }
        if (click.button() != 0) {
            return false;
        }
        double mx = click.x();
        double my = click.y();

        if (Ui.inside(mx, my, px + 4, listTop, listW, listBottom - listTop)) {
            int index = (int) ((my - listTop - 3 + scrollL) / ROW_H);
            if (index >= 0 && index < shown.size()) {
                String id = shown.get(index);
                ModConfig.Wish existing = wishFor(id);
                if (existing != null) {
                    cfg.rerollWishes.remove(existing);
                } else {
                    cfg.rerollWishes.add(new ModConfig.Wish(id, EnchantInfo.maxLevel(id), 64));
                }
                cfg.save();
                clickSound();
                return true;
            }
        }

        if (Ui.inside(mx, my, rx, wishTop, rw, wishBottom - wishTop)) {
            for (int i = 0; i < cfg.rerollWishes.size(); i++) {
                ModConfig.Wish w = cfg.rerollWishes.get(i);
                int y = wishTop + 3 + i * WISH_H - (int) scrollR;
                int x0 = rx + 6;
                int ly = y + 16;
                int cx = x0 + 92;
                int max = EnchantInfo.maxLevel(w.enchant);
                if (hit(mx, my, rx + rw - 20, y + 2, 14)) {
                    cfg.rerollWishes.remove(i);
                } else if (hit(mx, my, x0 + 32, ly, 12)) {
                    w.minLevel = MathHelper.clamp(w.minLevel - 1, 1, max);
                } else if (hit(mx, my, x0 + 66, ly, 12)) {
                    w.minLevel = MathHelper.clamp(w.minLevel + 1, 1, max);
                } else if (hit(mx, my, cx + 46, ly, 18)) {
                    w.maxCost = MathHelper.clamp(w.maxCost - 8, 1, 64);
                } else if (hit(mx, my, cx + 66, ly, 14)) {
                    w.maxCost = MathHelper.clamp(w.maxCost - 1, 1, 64);
                } else if (hit(mx, my, cx + 100, ly, 14)) {
                    w.maxCost = MathHelper.clamp(w.maxCost + 1, 1, 64);
                } else if (hit(mx, my, cx + 116, ly, 18)) {
                    w.maxCost = MathHelper.clamp(w.maxCost + 8, 1, 64);
                } else {
                    continue;
                }
                cfg.save();
                clickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= rx) {
            scrollR = MathHelper.clamp(scrollR - verticalAmount * 16, 0.0, maxScrollR());
        } else {
            scrollL = MathHelper.clamp(scrollL - verticalAmount * ROW_H * 2, 0.0, maxScrollL());
        }
        return true;
    }

    @Override
    public void close() {
        cfg.save();
        this.client.setScreen(this.parent);
    }
}
