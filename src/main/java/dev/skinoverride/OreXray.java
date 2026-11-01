package dev.skinoverride;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ore outlines that show through walls, just for fun. Every second it looks at the blocks around you (only the
 * ones your game has loaded) and draws a colored box around each ore. Many servers treat this as cheating: use it
 * in singleplayer or where it is allowed.
 */
public final class OreXray {

    private record Ore(int x, int y, int z, int category, double distSq) {
    }

    private static final String[] NAMES = {"Coal", "Iron", "Copper", "Gold", "Redstone", "Lapis", "Diamond", "Emerald",
            "Quartz", "Ancient debris"};
    private static final int[] COLORS = {0xFF9AA0A6, 0xFFD8AF93, 0xFFE07B39, 0xFFFFD700, 0xFFFF2A2A, 0xFF2F5BFF,
            0xFF40E6FF, 0xFF2EE66B, 0xFFF2EDE3, 0xFFB05A3C};

    private static final Map<Block, Integer> KIND = new HashMap<>();
    private static boolean built;
    private static List<Ore> ores = new ArrayList<>();
    private static long lastScan;
    private static BlockPos lastCenter = BlockPos.ORIGIN;

    private OreXray() {
    }

    public static String name(int category) {
        return NAMES[category];
    }

    private static int categorize(String path) {
        if (path.equals("ancient_debris")) {
            return 9;
        }
        if (!path.endsWith("_ore")) {
            return -1;
        }
        if (path.contains("coal")) {
            return 0;
        }
        if (path.contains("iron")) {
            return 1;
        }
        if (path.contains("copper")) {
            return 2;
        }
        if (path.contains("gold")) {
            return 3;
        }
        if (path.contains("redstone")) {
            return 4;
        }
        if (path.contains("lapis")) {
            return 5;
        }
        if (path.contains("diamond")) {
            return 6;
        }
        if (path.contains("emerald")) {
            return 7;
        }
        if (path.contains("quartz")) {
            return 8;
        }
        return -1;
    }

    private static void build() {
        built = true;
        for (Block block : Registries.BLOCK) {
            int cat = categorize(Registries.BLOCK.getId(block).getPath());
            if (cat >= 0) {
                KIND.put(block, cat);
            }
        }
    }

    private static boolean[] enabled(ModConfig cfg) {
        return new boolean[]{cfg.xrayCoal, cfg.xrayIron, cfg.xrayCopper, cfg.xrayGold, cfg.xrayRedstone, cfg.xrayLapis,
                cfg.xrayDiamond, cfg.xrayEmerald, cfg.xrayQuartz, cfg.xrayDebris};
    }

    private static void scan(MinecraftClient mc, ModConfig cfg) {
        if (!built) {
            build();
        }
        boolean[] on = enabled(cfg);
        int r = Math.max(4, Math.min(24, cfg.xrayRadius));
        BlockPos center = mc.player.getBlockPos();
        BlockPos.Mutable pos = new BlockPos.Mutable();
        List<Ore> found = new ArrayList<>();
        int bottom = mc.world.getBottomY();
        int top = bottom + mc.world.getHeight() - 1;
        for (int y = Math.max(bottom, center.getY() - r); y <= Math.min(top, center.getY() + r); y++) {
            for (int x = center.getX() - r; x <= center.getX() + r; x++) {
                for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
                    BlockState state = mc.world.getBlockState(pos.set(x, y, z));
                    Integer cat = KIND.get(state.getBlock());
                    if (cat == null || !on[cat]) {
                        continue;
                    }
                    double dx = x - mc.player.getX();
                    double dy = y - mc.player.getY();
                    double dz = z - mc.player.getZ();
                    found.add(new Ore(x, y, z, cat, dx * dx + dy * dy + dz * dz));
                }
            }
        }
        found.sort((a, b) -> Double.compare(a.distSq(), b.distSq()));
        ores = found.size() > 300 ? new ArrayList<>(found.subList(0, 300)) : found;
        lastCenter = center;
        lastScan = System.currentTimeMillis();
    }

    /** Draws the outlines. Called from the HUD layer, so it is on top of the world. */
    public static void render(DrawContext c, MinecraftClient mc) {
        ModConfig cfg = ModConfig.get();
        if (!cfg.oreXray || mc.player == null || mc.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastScan > 900 || lastCenter.getSquaredDistance(mc.player.getBlockPos()) > 9) {
            scan(mc, cfg);
        }
        if (ores.isEmpty()) {
            return;
        }
        Projection proj = Projection.create(mc, mc.getRenderTickCounter().getTickProgress(false));
        if (proj == null) {
            return;
        }
        int labels = 0;
        for (Ore ore : ores) {
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;
            int seen = 0;
            for (int i = 0; i < 8; i++) {
                double[] s = proj.project(new Vec3d(ore.x() + (i & 1), ore.y() + ((i >> 1) & 1), ore.z() + ((i >> 2) & 1)));
                if (s == null) {
                    continue;
                }
                seen++;
                minX = Math.min(minX, s[0]);
                minY = Math.min(minY, s[1]);
                maxX = Math.max(maxX, s[0]);
                maxY = Math.max(maxY, s[1]);
            }
            if (seen < 4) {
                continue; // mostly behind you
            }
            int x0 = (int) Math.round(minX);
            int y0 = (int) Math.round(minY);
            int x1 = Math.max((int) Math.round(maxX), x0 + 4);
            int y1 = Math.max((int) Math.round(maxY), y0 + 4);
            int color = COLORS[ore.category()];
            int shade = 0xFF000000;
            // dark edge first, colored line on top: readable on any background
            c.fill(x0 - 1, y0 - 1, x1 + 1, y0 + 1, shade);
            c.fill(x0 - 1, y1 - 1, x1 + 1, y1 + 1, shade);
            c.fill(x0 - 1, y0 - 1, x0 + 1, y1 + 1, shade);
            c.fill(x1 - 1, y0 - 1, x1 + 1, y1 + 1, shade);
            c.fill(x0, y0, x1, y0 + 1, color);
            c.fill(x0, y1 - 1, x1, y1, color);
            c.fill(x0, y0, x0 + 1, y1, color);
            c.fill(x1 - 1, y0, x1, y1, color);
            if (cfg.xrayLabels && labels < 25) {
                labels++;
                String text = NAMES[ore.category()] + " " + Math.round(Math.sqrt(ore.distSq())) + "m";
                c.drawTextWithShadow(mc.textRenderer, text, x0, y0 - 10, color);
            }
        }
    }
}
