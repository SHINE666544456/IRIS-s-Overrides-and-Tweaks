package dev.skinoverride;

import dev.skinoverride.chat.ChatColors;
import dev.skinoverride.chat.Emotes;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/** Tabbed settings screen: toggles, sliders, color pickers and option cyclers for every feature. */
public class SettingsScreen extends ThemedScreen {

    private sealed interface Row permits Header, Note, Toggle, Slider, Cycle, ColorRow, Action, EmoteGrid {
    }

    private record Header(String text) implements Row {
    }

    private record Note(String text) implements Row {
    }

    private record Toggle(String label, String desc, BooleanSupplier get, Consumer<Boolean> set) implements Row {
    }

    private record Slider(String label, String desc, int min, int max, int step, IntFunction<String> format,
                          IntSupplier get, IntConsumer set) implements Row {
    }

    private record Cycle(String label, String desc, String[] options, IntSupplier get, IntConsumer set) implements Row {
    }

    private record ColorRow(String label, String desc, IntSupplier get, IntConsumer set) implements Row {
    }

    private record Action(String label, String desc, String button, Runnable run) implements Row {
    }

    private record EmoteGrid() implements Row {
    }

    private static final String[] TABS = {"HUD", "Inspect", "AFK", "Chat", "Visuals"};
    private static final int TAB_W = 78;
    private static final int SLIDER_W = 100;

    private final Screen parent;
    private final ModConfig cfg = ModConfig.get();

    private int tab;
    private double scroll;
    private Slider dragging;
    private int dragX;
    private ColorRow picker;
    private int pickerX;
    private int pickerY;

    // panel geometry
    private int px, py, pw, ph, contentX, contentW, contentTop, contentBottom;

    public SettingsScreen(Screen parent) {
        super(Text.literal("Settings"));
        this.parent = parent;
    }

    // ---------- rows ----------

    private List<Row> rows() {
        List<Row> r = new ArrayList<>();
        switch (tab) {
            case 0 -> {
                r.add(new Header("Armor & held items"));
                r.add(new Toggle("Show armor HUD", "Your armor and held items. Drag it anywhere.",
                        () -> cfg.armorHud, v -> cfg.armorHud = v));
                r.add(new Toggle("Show held item", "Main-hand item next to your armor.",
                        () -> cfg.armorHeld, v -> cfg.armorHeld = v));
                r.add(new Toggle("Show off-hand", "Off-hand item too.",
                        () -> cfg.armorOffhand, v -> cfg.armorOffhand = v));
                r.add(new Cycle("Direction", "Row or column (for next to the hotbar).",
                        new String[]{"Horizontal", "Vertical"}, () -> cfg.armorLayout, v -> cfg.armorLayout = v));
                r.add(new Cycle("Durability text", "Number next to each damageable item.",
                        new String[]{"Off", "Remaining", "Percent"},
                        () -> cfg.armorDurability, v -> cfg.armorDurability = v));
                r.add(new Toggle("Low durability warning", "The slot flashes red when low.",
                        () -> cfg.armorLowWarning, v -> cfg.armorLowWarning = v));
                r.add(new Slider("Warn below", "Percent of durability left.", 5, 50, 5,
                        v -> v + "%", () -> cfg.armorLowPercent, v -> cfg.armorLowPercent = v));
                r.add(new Header("Effect timers"));
                r.add(new Toggle("Custom effect timers", "Replaces the vanilla effect icons.",
                        () -> cfg.effectsHud, v -> cfg.effectsHud = v));
                r.add(new Toggle("Flash when low", "Blinks red as the effect runs out.",
                        () -> cfg.effectsFlash, v -> cfg.effectsFlash = v));
                r.add(new Slider("Low at", "Seconds left before it counts as low.", 3, 30, 1,
                        v -> v + "s", () -> cfg.effectsLowSeconds, v -> cfg.effectsLowSeconds = v));
                r.add(new Header("Coordinates & FPS"));
                r.add(new Toggle("Show info panel", "A small movable panel.",
                        () -> cfg.infoHud, v -> cfg.infoHud = v));
                r.add(new Toggle("Coordinates", "Your X / Y / Z.",
                        () -> cfg.infoCoords, v -> cfg.infoCoords = v));
                r.add(new Toggle("Facing direction", "Compass direction you look.",
                        () -> cfg.infoDirection, v -> cfg.infoDirection = v));
                r.add(new Toggle("FPS", "Frames per second.",
                        () -> cfg.infoFps, v -> cfg.infoFps = v));
                r.add(new Toggle("Ping", "Latency to the server.",
                        () -> cfg.infoPing, v -> cfg.infoPing = v));
                r.add(new Note("Move or resize nothing by hand: use Edit HUD layout below."));
            }
            case 1 -> {
                r.add(new Header("Crosshair inspect"));
                r.add(new Toggle("Enable inspect overlay", "Shows gear of the player you look at.",
                        () -> cfg.inspectHud, v -> cfg.inspectHud = v));
                r.add(new Toggle("Show enchantments", "Lists enchants for each held/worn item.",
                        () -> cfg.inspectEnchants, v -> cfg.inspectEnchants = v));
                r.add(new Toggle("Show health", "Their current health.",
                        () -> cfg.inspectHealth, v -> cfg.inspectHealth = v));
                r.add(new Slider("Range", "How far away to inspect.", 8, 64, 4,
                        v -> v + " blocks", () -> cfg.inspectRange, v -> cfg.inspectRange = v));
                r.add(new Note("Only players you can see are shown, and only held/worn items:"));
                r.add(new Note("servers never send other players' inventories to your client."));
            }
            case 2 -> {
                r.add(new Header("AFK detection"));
                r.add(new Toggle("Enable AFK detection", "Watches nearby players for movement.",
                        () -> cfg.afkTracking, v -> cfg.afkTracking = v));
                r.add(new Slider("Mark AFK after", "Time without moving or turning.", 15, 600, 15,
                        Ui::formatShort, () -> cfg.afkSeconds, v -> cfg.afkSeconds = v));
                r.add(new Toggle("AFK tag over heads", "A small AFK label above their head.",
                        () -> cfg.afkTags, v -> cfg.afkTags = v));
                r.add(new Toggle("Show AFK list on HUD", "Small movable list of AFK players.",
                        () -> cfg.afkHudList, v -> cfg.afkHudList = v));
                r.add(new Note("AFK also shows in the skin screen and the inspect overlay."));
                r.add(new Note("Only players loaded near you can be watched. Being pushed"));
                r.add(new Note("by water or pistons counts as moving."));
            }
            case 3 -> {
                r.add(new Header("Chat colors"));
                r.add(new Toggle("Custom chat colors", "Master switch for the colors below.",
                        () -> cfg.chatCustom, v -> cfg.chatCustom = v));
                r.add(new ColorRow("Everyone's name color", "Used unless a player has their own.",
                        () -> cfg.chatDefaultNameColor, v -> cfg.chatDefaultNameColor = v));
                r.add(new ColorRow("Everyone's message color", "Used unless a player has their own.",
                        () -> cfg.chatDefaultMsgColor, v -> cfg.chatDefaultMsgColor = v));
                r.add(new Action("Per-player colors", "Pick name and message colors per player.", "Open",
                        () -> this.client.setScreen(new ChatPlayersScreen(this))));
                r.add(new ColorRow("Join messages", "\"X joined the game\".",
                        () -> cfg.chatJoinColor, v -> cfg.chatJoinColor = v));
                r.add(new ColorRow("Leave messages", "\"X left the game\".",
                        () -> cfg.chatLeaveColor, v -> cfg.chatLeaveColor = v));
                r.add(new ColorRow("Advancements", "Advancement and challenge messages.",
                        () -> cfg.chatAdvancementColor, v -> cfg.chatAdvancementColor = v));
                r.add(new ColorRow("Death messages", "How players died.",
                        () -> cfg.chatDeathColor, v -> cfg.chatDeathColor = v));
                r.add(new Note("Rainbow colors every letter. Color cycle gives each new message"));
                r.add(new Note("a different color. Colors are fixed once a message arrives."));
                r.add(new Header("Emotes"));
                r.add(new Toggle("Emotes", "Show :smile: :heart: ... as pixel emotes.",
                        () -> cfg.chatEmotes, v -> cfg.chatEmotes = v));
                r.add(new Toggle("Text faces", "Also turn :) :D <3 into emotes.",
                        () -> cfg.chatEmoteFaces, v -> cfg.chatEmoteFaces = v));
                r.add(new EmoteGrid());
                r.add(new Note("Only you see emotes. Animated GIFs can't be shown in chat text."));
                r.add(new Header("Speech bubbles"));
                r.add(new Toggle("Bubbles above heads", "Chat messages float above nearby players.",
                        () -> cfg.chatBubbles, v -> cfg.chatBubbles = v));
                r.add(new Slider("Bubble time", "How long a bubble stays.", 3, 15, 1,
                        v -> v + "s", () -> cfg.chatBubbleSeconds, v -> cfg.chatBubbleSeconds = v));
                r.add(new Slider("Bubble range", "How close the player must be.", 8, 64, 4,
                        v -> v + " blocks", () -> cfg.chatBubbleRange, v -> cfg.chatBubbleRange = v));
                r.add(new Header("Text to speech"));
                r.add(new Toggle("Read chat aloud", "Uses Minecraft's built-in narrator.",
                        () -> cfg.chatTts, Tts::set));
                r.add(new Note("The voice and speed come from your operating system."));
            }
            default -> {
                r.add(new Header("World"));
                r.add(new Toggle("Reveal invisible players", "Shows invisible players and mobs normally.",
                        () -> cfg.revealInvisible, v -> cfg.revealInvisible = v));
                r.add(new Note("Only changes what YOU see. Invisibility still works for everyone else."));
                r.add(new Toggle("Fullbright", "See clearly in the dark. Key: G.",
                        () -> cfg.fullbright, v -> cfg.fullbright = v));
                r.add(new Header("Zoom"));
                r.add(new Slider("Default zoom", "Hold C to zoom; scroll to change it.", 2, 10, 1,
                        v -> v + "x", () -> cfg.zoomDefault, v -> cfg.zoomDefault = v));
                r.add(new Toggle("Smooth camera while zooming", "Steadier aiming when zoomed in.",
                        () -> cfg.zoomSmoothCamera, v -> cfg.zoomSmoothCamera = v));
                r.add(new Header("Screenshots"));
                r.add(new Action("Clean screenshot", "Hides the HUD; player name tags stay. Key: F9.", "Take now",
                        () -> {
                            this.client.setScreen(null);
                            CleanScreenshot.request(this.client);
                        }));
                r.add(new Note("Keys can be changed under Options > Controls (search \"IRIS\")."));
            }
        }
        return r;
    }

    private static int rowHeight(Row row) {
        if (row instanceof Header) {
            return 18;
        }
        if (row instanceof Note) {
            return 11;
        }
        if (row instanceof EmoteGrid) {
            return 4 * 13 + 6;
        }
        return 28;
    }

    private double maxScroll() {
        int total = 4;
        for (Row r : rows()) {
            total += rowHeight(r);
        }
        return Math.max(0, total - (contentBottom - contentTop));
    }

    // ---------- setup ----------

    @Override
    protected void build() {
        pw = Math.min(this.width - 20, 430);
        ph = this.height - 24 - 34;
        px = (this.width - pw) / 2;
        py = 24;
        contentX = px + TAB_W + 8;
        contentW = pw - TAB_W - 14;
        contentTop = py + 22;
        contentBottom = py + ph - 4;

        int y = this.height - 28;
        int bw = Math.min(120, (pw - 8) / 3);
        int startX = px + (pw - (bw * 3 + 8)) / 2;
        button(startX, y, bw, 20, "Edit HUD layout", true, () -> this.client.setScreen(new HudEditorScreen(this)));
        button(startX + bw + 4, y, bw, 20, "Skins", false, () -> this.client.setScreen(new SkinOverrideScreen(this, null)));
        button(startX + (bw + 4) * 2, y, bw, 20, "Done", false, this::close);
    }

    // ---------- rendering ----------

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        super.render(c, mouseX, mouseY, delta);

        Ui.panel(c, px, py, pw, ph);
        Ui.header(c, this.textRenderer, "IRIS'S Overrides and Tweaks  /  Settings", px + 1, py + 1, pw - 2);

        for (int i = 0; i < TABS.length; i++) {
            int ty = py + 22 + i * 24;
            boolean sel = i == tab;
            boolean hov = Ui.inside(mouseX, mouseY, px + 4, ty, TAB_W, 20);
            Ui.roundRect(c, px + 4, ty, TAB_W, 20, sel ? 0xFF1E8F80 : hov ? Ui.BG_HOVER : Ui.BG_LIGHT);
            Ui.roundBorder(c, px + 4, ty, TAB_W, 20, sel ? Ui.ACCENT : Ui.BORDER);
            Ui.text(c, this.textRenderer, TABS[i], px + 12, ty + 6, sel ? 0xFFFFFFFF : Ui.TEXT);
        }

        List<Row> rows = rows();
        scroll = MathHelper.clamp(scroll, 0.0, maxScroll());
        boolean pickerOpen = picker != null;

        c.enableScissor(contentX, contentTop, contentX + contentW, contentBottom);
        int y = contentTop + 2 - (int) scroll;
        for (Row row : rows) {
            int h = rowHeight(row);
            if (y + h >= contentTop && y <= contentBottom) {
                drawRow(c, row, y, pickerOpen ? -1 : mouseX, pickerOpen ? -1 : mouseY);
            }
            y += h;
        }
        c.disableScissor();

        drawButtons(c, mouseX, mouseY);

        if (picker != null) {
            ColorPicker.draw(c, this.textRenderer, pickerX, pickerY, mouseX, mouseY, picker.get().getAsInt());
        }
    }

    private void drawRow(DrawContext c, Row row, int y, int mouseX, int mouseY) {
        var tr = this.textRenderer;
        int right = contentX + contentW - 6;
        switch (row) {
            case Header h -> {
                Ui.text(c, tr, h.text(), contentX + 2, y + 6, Ui.ACCENT);
                c.fill(contentX + 2, y + 16, contentX + contentW - 4, y + 17, Ui.BORDER);
            }
            case Note n -> Ui.text(c, tr, n.text(), contentX + 2, y + 1, Ui.MUTED);
            case Toggle t -> {
                boolean hov = Ui.inside(mouseX, mouseY, contentX, y, contentW, 28);
                if (hov) {
                    Ui.roundRect(c, contentX, y, contentW - 2, 26, 0x30FFFFFF);
                }
                Ui.text(c, tr, t.label(), contentX + 6, y + 4, Ui.TEXT);
                Ui.text(c, tr, t.desc(), contentX + 6, y + 15, Ui.MUTED);
                Ui.toggle(c, right - 24, y + 7, t.get().getAsBoolean(), hov);
            }
            case Slider s -> {
                Ui.text(c, tr, s.label(), contentX + 6, y + 4, Ui.TEXT);
                Ui.text(c, tr, s.desc(), contentX + 6, y + 15, Ui.MUTED);
                int sx = right - SLIDER_W;
                int v = s.get().getAsInt();
                double frac = (v - s.min()) / (double) (s.max() - s.min());
                boolean hov = dragging == s || Ui.inside(mouseX, mouseY, sx - 4, y + 4, SLIDER_W + 8, 18);
                Ui.slider(c, sx, y + 7, SLIDER_W, frac, hov);
                String label = s.format().apply(v);
                Ui.text(c, tr, label, sx - 8 - tr.getWidth(label), y + 4, Ui.ACCENT);
            }
            case Cycle cy -> {
                boolean hov = Ui.inside(mouseX, mouseY, contentX, y, contentW, 28);
                if (hov) {
                    Ui.roundRect(c, contentX, y, contentW - 2, 26, 0x30FFFFFF);
                }
                Ui.text(c, tr, cy.label(), contentX + 6, y + 4, Ui.TEXT);
                Ui.text(c, tr, cy.desc(), contentX + 6, y + 15, Ui.MUTED);
                Ui.button(c, tr, right - 74, y + 4, 74, 18, cy.options()[cy.get().getAsInt()], hov, true, false);
            }
            case ColorRow cr -> {
                boolean hov = Ui.inside(mouseX, mouseY, contentX, y, contentW, 28);
                if (hov) {
                    Ui.roundRect(c, contentX, y, contentW - 2, 26, 0x30FFFFFF);
                }
                Ui.text(c, tr, cr.label(), contentX + 6, y + 4, Ui.TEXT);
                Ui.text(c, tr, cr.desc(), contentX + 6, y + 15, Ui.MUTED);
                Ui.roundBorder(c, right - 42, y + 5, 42, 16, hov ? Ui.ACCENT : Ui.BORDER);
                ColorPicker.swatch(c, tr, right - 41, y + 6, 40, 14, cr.get().getAsInt());
            }
            case Action a -> {
                Ui.text(c, tr, a.label(), contentX + 6, y + 4, Ui.TEXT);
                Ui.text(c, tr, a.desc(), contentX + 6, y + 15, Ui.MUTED);
                boolean hov = Ui.inside(mouseX, mouseY, right - 70, y + 4, 70, 18);
                Ui.button(c, tr, right - 70, y + 4, 70, 18, a.button(), hov, true, true);
            }
            case EmoteGrid g -> {
                int cell = (contentW - 12) / 4;
                for (int i = 0; i < Emotes.NAMES.length; i++) {
                    int ex = contentX + 6 + (i % 4) * cell;
                    int ey = y + 3 + (i / 4) * 13;
                    c.drawTextWithShadow(tr, Text.literal(String.valueOf(Emotes.glyph(i))), ex, ey, 0xFFFFFFFF);
                    Ui.text(c, tr, ":" + Emotes.NAMES[i] + ":", ex + 14, ey, Ui.MUTED);
                }
            }
        }
    }

    // ---------- input ----------

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

        if (picker != null) {
            Integer chosen = ColorPicker.hit(mx, my, pickerX, pickerY);
            if (chosen != null) {
                picker.set().accept(chosen);
                cfg.save();
                clickSound();
            }
            picker = null;
            return true;
        }

        for (int i = 0; i < TABS.length; i++) {
            if (Ui.inside(mx, my, px + 4, py + 22 + i * 24, TAB_W, 20)) {
                if (tab != i) {
                    tab = i;
                    scroll = 0;
                }
                clickSound();
                return true;
            }
        }

        if (!Ui.inside(mx, my, contentX, contentTop, contentW, contentBottom - contentTop)) {
            return false;
        }
        int y = contentTop + 2 - (int) scroll;
        int right = contentX + contentW - 6;
        for (Row row : rows()) {
            int h = rowHeight(row);
            if (my >= y && my < y + h) {
                switch (row) {
                    case Toggle t -> {
                        t.set().accept(!t.get().getAsBoolean());
                        cfg.save();
                        clickSound();
                        return true;
                    }
                    case Cycle cy -> {
                        cy.set().accept((cy.get().getAsInt() + 1) % cy.options().length);
                        cfg.save();
                        clickSound();
                        return true;
                    }
                    case Slider s -> {
                        if (mx >= right - SLIDER_W - 6) {
                            dragging = s;
                            dragX = right - SLIDER_W;
                            applySlider(s, mx);
                            return true;
                        }
                    }
                    case ColorRow cr -> {
                        picker = cr;
                        pickerX = Math.max(px + 4, right - ColorPicker.width());
                        pickerY = y + 26;
                        if (pickerY + ColorPicker.height() > py + ph - 2) {
                            pickerY = Math.max(py + 20, y - ColorPicker.height() - 2);
                        }
                        clickSound();
                        return true;
                    }
                    case Action a -> {
                        if (mx >= right - 74) {
                            clickSound();
                            a.run().run();
                            return true;
                        }
                    }
                    default -> {
                    }
                }
            }
            y += h;
        }
        return false;
    }

    private void applySlider(Slider s, double mouseX) {
        double frac = MathHelper.clamp((mouseX - dragX) / SLIDER_W, 0.0, 1.0);
        int raw = s.min() + (int) Math.round((s.max() - s.min()) * frac / s.step()) * s.step();
        s.set().accept(MathHelper.clamp(raw, s.min(), s.max()));
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            applySlider(dragging, click.x());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            cfg.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        picker = null;
        scroll = MathHelper.clamp(scroll - verticalAmount * 14, 0.0, maxScroll());
        return true;
    }

    @Override
    public void close() {
        cfg.save();
        this.client.setScreen(this.parent);
    }
}
