package dev.skinoverride.hud;

import dev.skinoverride.ModConfig;
import dev.skinoverride.Ui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Your worn armor plus held items, with durability numbers and a flashing low-durability warning. */
public class ArmorWidget implements HudWidget {

    private static final int SLOT = 22;
    private static final int GAP = 2;
    private static final int GROUP_GAP = 6;
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final List<ItemStack> armorStacks = new ArrayList<>();
    private final List<ItemStack> heldStacks = new ArrayList<>();
    private List<ItemStack> demo;
    private int w;
    private int h;

    @Override
    public String id() {
        return "armor";
    }

    @Override
    public String label() {
        return "Armor & held items";
    }

    @Override
    public float defaultX() {
        return 0f;
    }

    @Override
    public float defaultY() {
        return 1f;
    }

    @Override
    public boolean enabled() {
        return ModConfig.get().armorHud;
    }

    @Override
    public boolean visible(MinecraftClient mc) {
        // The inventory already shows your armor.
        return !(mc.currentScreen instanceof HandledScreen<?>);
    }

    @Override
    public void prepare(MinecraftClient mc, boolean preview) {
        ModConfig cfg = ModConfig.get();
        armorStacks.clear();
        heldStacks.clear();
        if (preview || mc.player == null) {
            if (demo == null) {
                ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
                helmet.setDamage(helmet.getMaxDamage() * 92 / 100);
                demo = List.of(helmet, new ItemStack(Items.DIAMOND_CHESTPLATE),
                        new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS),
                        new ItemStack(Items.DIAMOND_SWORD));
            }
            armorStacks.addAll(demo.subList(0, 4));
            if (cfg.armorHeld) {
                heldStacks.add(demo.get(4));
            }
        } else {
            for (EquipmentSlot slot : ARMOR) {
                ItemStack s = mc.player.getEquippedStack(slot);
                if (!s.isEmpty()) {
                    armorStacks.add(s);
                }
            }
            if (cfg.armorHeld) {
                ItemStack s = mc.player.getEquippedStack(EquipmentSlot.MAINHAND);
                if (!s.isEmpty()) {
                    heldStacks.add(s);
                }
            }
            if (cfg.armorOffhand) {
                ItemStack s = mc.player.getEquippedStack(EquipmentSlot.OFFHAND);
                if (!s.isEmpty()) {
                    heldStacks.add(s);
                }
            }
        }
        int count = armorStacks.size() + heldStacks.size();
        if (count == 0) {
            w = 0;
            h = 0;
            return;
        }
        w = count * SLOT + (count - 1) * GAP + (!armorStacks.isEmpty() && !heldStacks.isEmpty() ? GROUP_GAP - GAP : 0);
        h = SLOT + (cfg.armorDurability != 0 ? 11 : 0);
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
        int cx = x;
        for (ItemStack s : armorStacks) {
            drawSlot(c, mc, s, cx, y);
            cx += SLOT + GAP;
        }
        if (!armorStacks.isEmpty() && !heldStacks.isEmpty()) {
            cx += GROUP_GAP - GAP;
        }
        for (ItemStack s : heldStacks) {
            drawSlot(c, mc, s, cx, y);
            cx += SLOT + GAP;
        }
    }

    private void drawSlot(DrawContext c, MinecraftClient mc, ItemStack stack, int x, int y) {
        ModConfig cfg = ModConfig.get();
        boolean damageable = stack.getMaxDamage() > 0;
        int percent = damageable ? (stack.getMaxDamage() - stack.getDamage()) * 100 / stack.getMaxDamage() : 100;
        boolean low = cfg.armorLowWarning && damageable && percent <= cfg.armorLowPercent;

        int border = Ui.BORDER;
        if (low) {
            border = Ui.blink() ? Ui.BAD : 0xFF7A2B2B;
        }
        Ui.roundRect(c, x, y, SLOT, SLOT, low && Ui.blink() ? 0xB0401818 : Ui.BG);
        Ui.roundBorder(c, x, y, SLOT, SLOT, border);

        c.drawItem(stack, x + 3, y + 3);
        c.drawStackOverlay(mc.textRenderer, stack, x + 3, y + 3);

        if (cfg.armorDurability != 0 && damageable) {
            String text = cfg.armorDurability == 2
                    ? percent + "%"
                    : String.valueOf(stack.getMaxDamage() - stack.getDamage());
            int color = low ? (Ui.blink() ? Ui.BAD : 0xFFB04040) : percent <= 50 ? Ui.WARN : Ui.MUTED;
            int tw = mc.textRenderer.getWidth(text);
            c.drawTextWithShadow(mc.textRenderer, text, x + (SLOT - tw) / 2, y + SLOT + 2, color);
        }
    }
}
