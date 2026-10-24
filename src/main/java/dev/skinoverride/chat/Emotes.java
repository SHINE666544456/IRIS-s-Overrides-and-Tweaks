package dev.skinoverride.chat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns :smile: (and optionally :) <3 ...) into small pixel emotes drawn by the mod's built-in font.
 * Only YOU see the emotes: other players need this mod too, otherwise they see the plain text.
 */
public final class Emotes {

    /** Order matches the sprite sheet (4 x 4), starting at U+E000. */
    public static final String[] NAMES = {
            "smile", "grin", "laugh", "wink", "cool", "cry", "angry", "wow",
            "heart", "skull", "fire", "star", "thumbsup", "eyes", "zzz", "gg"
    };

    private static final Map<String, Integer> INDEX = new HashMap<>();
    private static final Pattern NAMED = Pattern.compile(":([A-Za-z]{2,12}):");
    private static final Pattern FACES = Pattern.compile(
            "(?<![^\\s])(:\\)|:D|xD|XD|;\\)|B\\)|:\\(|>:\\(|:O|:o|<3)(?![^\\s])");
    private static final Map<String, String> FACE_NAMES = new HashMap<>();

    static {
        for (int i = 0; i < NAMES.length; i++) {
            INDEX.put(NAMES[i], i);
        }
        INDEX.put("lol", 2);
        INDEX.put("love", 8);
        INDEX.put("sleep", 14);
        INDEX.put("sleepy", 14);
        INDEX.put("like", 12);
        INDEX.put("thumbs", 12);
        INDEX.put("yes", 12);
        INDEX.put("sad", 5);
        INDEX.put("mad", 6);
        INDEX.put("omg", 7);
        INDEX.put("look", 13);
        INDEX.put("dead", 9);
        INDEX.put("lit", 10);

        FACE_NAMES.put(":)", "smile");
        FACE_NAMES.put(":D", "grin");
        FACE_NAMES.put("xD", "laugh");
        FACE_NAMES.put("XD", "laugh");
        FACE_NAMES.put(";)", "wink");
        FACE_NAMES.put("B)", "cool");
        FACE_NAMES.put(":(", "cry");
        FACE_NAMES.put(">:(", "angry");
        FACE_NAMES.put(":O", "wow");
        FACE_NAMES.put(":o", "wow");
        FACE_NAMES.put("<3", "heart");
    }

    private Emotes() {
    }

    public static char glyph(int index) {
        return (char) (0xE000 + index);
    }

    public static String replace(String s, boolean faces) {
        Matcher m = NAMED.matcher(s);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            Integer idx = INDEX.get(m.group(1).toLowerCase(Locale.ROOT));
            m.appendReplacement(sb, Matcher.quoteReplacement(idx == null ? m.group() : String.valueOf(glyph(idx))));
        }
        m.appendTail(sb);
        String result = sb.toString();
        if (faces) {
            Matcher f = FACES.matcher(result);
            StringBuilder fb = new StringBuilder();
            while (f.find()) {
                String name = FACE_NAMES.get(f.group(1));
                Integer idx = name == null ? null : INDEX.get(name);
                f.appendReplacement(fb, Matcher.quoteReplacement(idx == null ? f.group() : String.valueOf(glyph(idx))));
            }
            f.appendTail(fb);
            result = fb.toString();
        }
        return result;
    }

    /** Replaces emote words inside a Text while keeping each piece's style. */
    public static Text apply(Text text, boolean faces) {
        List<ChatColors.Seg> segs = ChatColors.segments(text);
        if (segs.isEmpty()) {
            return text;
        }
        MutableText out = Text.empty();
        boolean changed = false;
        for (ChatColors.Seg seg : segs) {
            String replaced = replace(seg.text(), faces);
            changed |= !replaced.equals(seg.text());
            out.append(Text.literal(replaced).setStyle(seg.style()));
        }
        return changed ? out : text;
    }
}
