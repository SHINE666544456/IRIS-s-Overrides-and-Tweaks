package dev.skinoverride.chat;

import dev.skinoverride.ModConfig;
import dev.skinoverride.SkinOverrideClient;
import dev.skinoverride.WorldLabels;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites every chat line the client receives: per-player name/message colors, join/leave/
 * advancement/death colors, emotes. Also feeds speech bubbles. Never throws: on any problem
 * the original message is shown untouched.
 */
public final class ChatProcessor {

    private static final Pattern PLAIN_CHAT = Pattern.compile("^<([A-Za-z0-9_]{1,16})> (.*)$", Pattern.DOTALL);

    private ChatProcessor() {
    }

    public static Text process(Text original) {
        try {
            return doProcess(original, ModConfig.get());
        } catch (RuntimeException e) {
            SkinOverrideClient.LOGGER.warn("Chat processing failed, showing original message", e);
            return original;
        }
    }

    private static Text doProcess(Text original, ModConfig cfg) {
        if (!(original.getContent() instanceof TranslatableTextContent content)) {
            return processPlainChat(original, cfg);
        }
        String key = content.getKey();
        Object[] args = content.getArgs();

        int nameIdx = -1;
        int msgIdx = -1;
        boolean bubble = false;
        switch (key) {
            case "chat.type.text", "chat.type.emote", "chat.type.announcement" -> {
                nameIdx = 0;
                msgIdx = 1;
                bubble = true;
            }
            case "commands.message.display.incoming" -> {
                nameIdx = 0;
                msgIdx = 1;
            }
            case "chat.type.team.text" -> {
                nameIdx = 1;
                msgIdx = 2;
                bubble = true;
            }
            default -> {
            }
        }
        if (nameIdx >= 0 && args.length > msgIdx) {
            return playerChat(original, key, args, nameIdx, msgIdx, bubble, cfg);
        }

        if (!cfg.chatCustom) {
            return original;
        }
        if (key.startsWith("multiplayer.player.joined")) {
            return ChatColors.apply(original, cfg.chatJoinColor);
        }
        if (key.equals("multiplayer.player.left")) {
            return ChatColors.apply(original, cfg.chatLeaveColor);
        }
        if (key.startsWith("chat.type.advancement.")) {
            return ChatColors.apply(original, cfg.chatAdvancementColor);
        }
        if (key.startsWith("death.")) {
            return ChatColors.apply(original, cfg.chatDeathColor);
        }
        return original;
    }

    private static Text playerChat(Text original, String key, Object[] args, int nameIdx, int msgIdx,
                                   boolean bubble, ModConfig cfg) {
        Text nameText = asText(args[nameIdx]);
        Text msgText = asText(args[msgIdx]);
        String sender = nameText.getString();

        int nameMode = ChatColors.DEFAULT;
        int msgMode = ChatColors.DEFAULT;
        if (cfg.chatCustom) {
            ModConfig.PlayerChat pc = lookup(sender, cfg);
            nameMode = pc != null && pc.nameColor != ChatColors.DEFAULT ? pc.nameColor : cfg.chatDefaultNameColor;
            msgMode = pc != null && pc.msgColor != ChatColors.DEFAULT ? pc.msgColor : cfg.chatDefaultMsgColor;
        }
        if (cfg.chatEmotes) {
            msgText = Emotes.apply(msgText, cfg.chatEmoteFaces);
        }
        if (bubble && cfg.chatBubbles) {
            WorldLabels.push(sender, msgText.getString());
        }
        nameText = ChatColors.apply(nameText, nameMode);
        msgText = ChatColors.apply(msgText, msgMode);

        Object[] newArgs = args.clone();
        newArgs[nameIdx] = nameText;
        newArgs[msgIdx] = msgText;
        MutableText result = Text.translatable(key, newArgs).setStyle(original.getStyle());
        for (Text sibling : original.getSiblings()) {
            result.append(sibling);
        }
        return result;
    }

    /** Servers that format chat themselves as plain "<Name> message" text. */
    private static Text processPlainChat(Text original, ModConfig cfg) {
        Matcher m = PLAIN_CHAT.matcher(original.getString());
        if (!m.matches()) {
            return original;
        }
        String sender = m.group(1);
        Text msg = Text.literal(m.group(2));
        Text name = Text.literal(sender);
        if (cfg.chatEmotes) {
            msg = Emotes.apply(msg, cfg.chatEmoteFaces);
        }
        int nameMode = ChatColors.DEFAULT;
        int msgMode = ChatColors.DEFAULT;
        if (cfg.chatCustom) {
            ModConfig.PlayerChat pc = lookup(sender, cfg);
            nameMode = pc != null && pc.nameColor != ChatColors.DEFAULT ? pc.nameColor : cfg.chatDefaultNameColor;
            msgMode = pc != null && pc.msgColor != ChatColors.DEFAULT ? pc.msgColor : cfg.chatDefaultMsgColor;
        }
        if (cfg.chatBubbles) {
            WorldLabels.push(sender, msg.getString());
        }
        return Text.literal("<").append(ChatColors.apply(name, nameMode)).append(Text.literal("> "))
                .append(ChatColors.apply(msg, msgMode));
    }

    private static Text asText(Object o) {
        return o instanceof Text t ? t : Text.literal(String.valueOf(o));
    }

    public static ModConfig.PlayerChat lookup(String sender, ModConfig cfg) {
        String low = sender.toLowerCase(Locale.ROOT);
        ModConfig.PlayerChat pc = cfg.chatPlayers.get(low);
        if (pc != null) {
            return pc;
        }
        for (String token : low.split("[^a-z0-9_]+")) {
            if (!token.isEmpty()) {
                pc = cfg.chatPlayers.get(token);
                if (pc != null) {
                    return pc;
                }
            }
        }
        return null;
    }

    /** A sample chat line for previews in the settings screens. */
    public static Text sample(String name, int nameMode, int msgMode) {
        Text msg = Emotes.apply(Text.literal("hey everyone :smile: nice build :fire:"), false);
        return Text.literal("<").append(ChatColors.apply(Text.literal(name), nameMode))
                .append(Text.literal("> ")).append(ChatColors.apply(msg, msgMode));
    }
}
