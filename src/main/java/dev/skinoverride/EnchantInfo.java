package dev.skinoverride;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a librarian villager can sell: every enchantment's highest level and its cheapest and dearest price.
 * An enchanted book costs 2 + (0 to 4 + 10 x level) + 3 x level emeralds, doubled for treasure enchantments
 * (Mending, Frost Walker and the curses), and never more than 64.
 */
public final class EnchantInfo {

    private record Info(int maxLevel, boolean treasure) {
    }

    private static final Map<String, Info> ALL = new LinkedHashMap<>();

    static {
        put("aqua_affinity", 1, false);
        put("bane_of_arthropods", 5, false);
        put("binding_curse", 1, true);
        put("blast_protection", 4, false);
        put("channeling", 1, false);
        put("depth_strider", 3, false);
        put("efficiency", 5, false);
        put("feather_falling", 4, false);
        put("fire_aspect", 2, false);
        put("fire_protection", 4, false);
        put("flame", 1, false);
        put("fortune", 3, false);
        put("frost_walker", 2, true);
        put("impaling", 5, false);
        put("infinity", 1, false);
        put("knockback", 2, false);
        put("looting", 3, false);
        put("loyalty", 3, false);
        put("luck_of_the_sea", 3, false);
        put("lure", 3, false);
        put("mending", 1, true);
        put("multishot", 1, false);
        put("piercing", 4, false);
        put("power", 5, false);
        put("projectile_protection", 4, false);
        put("protection", 4, false);
        put("punch", 2, false);
        put("quick_charge", 3, false);
        put("respiration", 3, false);
        put("riptide", 3, false);
        put("sharpness", 5, false);
        put("silk_touch", 1, false);
        put("smite", 5, false);
        put("sweeping_edge", 3, false);
        put("thorns", 3, false);
        put("unbreaking", 3, false);
        put("vanishing_curse", 1, true);
    }

    private EnchantInfo() {
    }

    private static void put(String id, int maxLevel, boolean treasure) {
        ALL.put(id, new Info(maxLevel, treasure));
    }

    public static List<String> ids() {
        return new ArrayList<>(ALL.keySet());
    }

    public static boolean known(String id) {
        return ALL.containsKey(id);
    }

    public static int maxLevel(String id) {
        Info info = ALL.get(id);
        return info == null ? 5 : info.maxLevel();
    }

    public static boolean treasure(String id) {
        Info info = ALL.get(id);
        return info != null && info.treasure();
    }

    /** The cheapest this book can ever be at this level. */
    public static int minPrice(String id, int level) {
        int base = 2 + 3 * level;
        return Math.min(64, treasure(id) ? base * 2 : base);
    }

    /** The dearest this book can be at this level. */
    public static int maxPrice(String id, int level) {
        int base = 2 + (4 + 10 * level) + 3 * level;
        return Math.min(64, treasure(id) ? base * 2 : base);
    }

    public static String pretty(String id) {
        StringBuilder sb = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
