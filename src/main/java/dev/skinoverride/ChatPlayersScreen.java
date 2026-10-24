package dev.skinoverride;

import dev.skinoverride.chat.ChatColors;
import dev.skinoverride.chat.ChatProcessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Pick a player, then pick their name color and message color (solid, rainbow or cycling). */
public class ChatPlayersScreen extends ThemedScreen {

    private static final int ROW_H = 14;

    private record Entry(String key, String display) {
    }

    private final Screen parent;
    private final ModConfig cfg = ModConfig.get();
    private final List<Entry> entries = new ArrayList<>();

    private String selectedKey;
    private double scroll;
    private TextFieldWidget addField;

    private int px, py, pw, ph, listW, listTop, listBottom, rx, nameY, msgY;

    public ChatPlayersScreen(Screen parent) {
        super(Text.literal("Per-player chat colors"));
        this.parent = parent;
    }

    @Override
    protected void build() {
        pw = Math.min(this.width - 20, 440);
        ph = this.height - 24 - 34;
        px = (this.width - pw) / 2;
        py = 24;
        listW = 130;
        listTop = py + 44;
        listBottom = py + ph - 4;
        rx = px + listW + 14;
        nameY = py + 36;
        msgY = nameY + ColorPicker.height() + 16;

        this.addField = new TextFieldWidget(this.textRenderer, px + 4, py + 22, listW - 34, 16, Text.literal("Player name"));
        this.addField.setPlaceholder(Text.literal("Add player...").formatted(Formatting.DARK_GRAY));
        this.addField.setMaxLength(16);
        this.addDrawableChild(this.addField);

        button(px + listW - 26, py + 22, 30, 16, "Add", false, this::addPlayer);

        int y = this.height - 28;
        int bw = 110;
        int startX = px + (pw - (bw * 2 + 4)) / 2;
        button(startX, y, bw, 20, () -> "Reset player", () -> selectedKey != null && cfg.chatPlayers.containsKey(selectedKey),
                false, () -> {
                    cfg.chatPlayers.remove(selectedKey);
                    cfg.save();
                });
        button(startX + bw + 4, y, bw, 20, "Done", true, this::close);

        refreshEntries();
    }

    private void refreshEntries() {
        Map<String, String> map = new LinkedHashMap<>();
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            for (PlayerListEntry e : handler.getPlayerList()) {
                String name = e.getProfile().name();
                map.put(name.toLowerCase(Locale.ROOT), name);
            }
        }
        for (Map.Entry<String, ModConfig.PlayerChat> e : cfg.chatPlayers.entrySet()) {
            String display = e.getValue().name == null || e.getValue().name.isEmpty() ? e.getKey() : e.getValue().name;
            map.put(e.getKey(), display);
        }
        entries.clear();
        for (Map.Entry<String, String> e : map.entrySet()) {
            entries.add(new Entry(e.getKey(), e.getValue()));
        }
        entries.sort(Comparator.comparing(e -> e.display().toLowerCase(Locale.ROOT)));
    }

    private void addPlayer() {
        String name = addField.getText().trim();
        if (!name.matches("[A-Za-z0-9_]{1,16}")) {
            return;
        }
        String key = name.toLowerCase(Locale.ROOT);
        ModConfig.PlayerChat pc = cfg.chatPlayers.computeIfAbsent(key, k -> new ModConfig.PlayerChat());
        pc.name = name;
        cfg.save();
        addField.setText("");
        selectedKey = key;
        refreshEntries();
    }

    private ModConfig.PlayerChat selectedChat(boolean create) {
        if (selectedKey == null) {
            return null;
        }
        ModConfig.PlayerChat pc = cfg.chatPlayers.get(selectedKey);
        if (pc == null && create) {
            pc = new ModConfig.PlayerChat();
            for (Entry e : entries) {
                if (e.key().equals(selectedKey)) {
                    pc.name = e.display();
                }
            }
            cfg.chatPlayers.put(selectedKey, pc);
        }
        return pc;
    }

    private String selectedDisplay() {
        for (Entry e : entries) {
            if (e.key().equals(selectedKey)) {
                return e.display();
            }
        }
        return selectedKey == null ? "" : selectedKey;
    }

    private double maxScroll() {
        return Math.max(0, entries.size() * ROW_H + 4 - (listBottom - listTop));
    }

    // ---------- rendering ----------

    @Override
    public void render(DrawContext c, int mouseX, int mouseY, float delta) {
        super.render(c, mouseX, mouseY, delta);

        Ui.panel(c, px, py, pw, ph);
        Ui.header(c, this.textRenderer, "Per-player chat colors", px + 1, py + 1, pw - 2);

        // player list
        Ui.panel(c, px + 4, listTop, listW, listBottom - listTop);
        scroll = MathHelper.clamp(scroll, 0.0, maxScroll());
        c.enableScissor(px + 5, listTop + 1, px + 3 + listW, listBottom - 1);
        for (int i = 0; i < entries.size(); i++) {
            int y = listTop + 3 + i * ROW_H - (int) scroll;
            if (y + ROW_H < listTop || y > listBottom) {
                continue;
            }
            Entry e = entries.get(i);
            boolean sel = e.key().equals(selectedKey);
            boolean hov = Ui.inside(mouseX, mouseY, px + 4, y, listW, ROW_H) && mouseY >= listTop && mouseY < listBottom;
            if (sel) {
                Ui.roundRect(c, px + 7, y, listW - 6, ROW_H, 0x8035D0BA);
            } else if (hov) {
                Ui.roundRect(c, px + 7, y, listW - 6, ROW_H, 0x40FFFFFF);
            }
            Ui.text(c, this.textRenderer, e.display(), px + 11, y + 3, Ui.TEXT);
            ModConfig.PlayerChat pc = cfg.chatPlayers.get(e.key());
            if (pc != null) {
                int dot = pc.nameColor >= 0 ? pc.nameColor : pc.msgColor >= 0 ? pc.msgColor : 0x35D0BA;
                c.fill(px + listW - 8, y + 5, px + listW - 3, y + 10, 0xFF000000 | dot);
            }
        }
        c.disableScissor();

        // right side
        if (selectedKey == null) {
            Ui.text(c, this.textRenderer, "Pick a player on the left,", rx, nameY + 6, Ui.MUTED);
            Ui.text(c, this.textRenderer, "or type a name and press Add.", rx, nameY + 18, Ui.MUTED);
        } else {
            ModConfig.PlayerChat pc = selectedChat(false);
            int nameMode = pc == null ? ChatColors.DEFAULT : pc.nameColor;
            int msgMode = pc == null ? ChatColors.DEFAULT : pc.msgColor;

            Ui.text(c, this.textRenderer, selectedDisplay() + " - name color", rx, nameY - 12, Ui.ACCENT);
            ColorPicker.draw(c, this.textRenderer, rx, nameY, mouseX, mouseY, nameMode);
            Ui.text(c, this.textRenderer, selectedDisplay() + " - message color", rx, msgY - 12, Ui.ACCENT);
            ColorPicker.draw(c, this.textRenderer, rx, msgY, mouseX, mouseY, msgMode);

            int effName = nameMode != ChatColors.DEFAULT ? nameMode : cfg.chatDefaultNameColor;
            int effMsg = msgMode != ChatColors.DEFAULT ? msgMode : cfg.chatDefaultMsgColor;
            Ui.text(c, this.textRenderer, "Preview", rx, msgY + ColorPicker.height() + 6, Ui.MUTED);
            c.drawTextWithShadow(this.textRenderer, ChatProcessor.sample(selectedDisplay(), effName, effMsg),
                    rx, msgY + ColorPicker.height() + 17, 0xFFFFFFFF);
        }

        drawButtons(c, mouseX, mouseY);
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

        if (selectedKey != null) {
            Integer name = ColorPicker.hit(mx, my, rx, nameY);
            if (name != null) {
                selectedChat(true).nameColor = name;
                finishEdit();
                return true;
            }
            Integer msg = ColorPicker.hit(mx, my, rx, msgY);
            if (msg != null) {
                selectedChat(true).msgColor = msg;
                finishEdit();
                return true;
            }
        }

        if (Ui.inside(mx, my, px + 4, listTop, listW, listBottom - listTop)) {
            int index = (int) ((my - listTop - 3 + scroll) / ROW_H);
            if (index >= 0 && index < entries.size()) {
                selectedKey = entries.get(index).key();
                clickSound();
                return true;
            }
        }
        return false;
    }

    private void finishEdit() {
        ModConfig.PlayerChat pc = selectedChat(false);
        if (pc != null && pc.nameColor == ChatColors.DEFAULT && pc.msgColor == ChatColors.DEFAULT) {
            cfg.chatPlayers.remove(selectedKey);
        }
        cfg.save();
        clickSound();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll = MathHelper.clamp(scroll - verticalAmount * ROW_H * 2, 0.0, maxScroll());
        return true;
    }

    @Override
    public void close() {
        cfg.save();
        this.client.setScreen(this.parent);
    }
}
