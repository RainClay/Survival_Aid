package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.survivalaid.features.ItemPickupFilterRule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/SurvivalAidCommands.class */
public final class SurvivalAidCommands {
    private SurvivalAidCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("survivalaid").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)).then(CommandManager.literal("pickup").then(CommandManager.literal("allow").then(CommandManager.argument("item", StringArgumentType.word()).then(CommandManager.argument("player", StringArgumentType.word()).executes(context -> {
            return allow((ServerCommandSource) context.getSource(), StringArgumentType.getString(context, "item"), StringArgumentType.getString(context, "player"));
        })))).then(CommandManager.literal("deny").then(CommandManager.argument("item", StringArgumentType.word()).then(CommandManager.argument("player", StringArgumentType.word()).executes(context2 -> {
            return deny((ServerCommandSource) context2.getSource(), StringArgumentType.getString(context2, "item"), StringArgumentType.getString(context2, "player"));
        })))).then(CommandManager.literal("clear").executes(context3 -> {
            return clearAll((ServerCommandSource) context3.getSource());
        }).then(CommandManager.argument("item", StringArgumentType.word()).executes(context4 -> {
            return clearItem((ServerCommandSource) context4.getSource(), StringArgumentType.getString(context4, "item"));
        }))).then(CommandManager.literal("list").executes(context5 -> {
            return list((ServerCommandSource) context5.getSource());
        }))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int allow(ServerCommandSource source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        if (normalizedItem.isEmpty() || normalizedPlayer.isEmpty()) {
            source.sendError(Text.literal("Usage: /survivalaid pickup allow <item> <player>"));
            return 0;
        }
        List<String> entries = entries();
        String newEntry = normalizedItem + "=" + normalizedPlayer;
        for (String entry : entries) {
            String[] parsed = parseEntry(entry);
            if (parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer)) {
                source.sendFeedback(() -> {
                    return Text.literal("Already allowed: " + normalizedItem + " -> " + normalizedPlayer);
                }, false);
                return 0;
            }
        }
        entries.add(newEntry);
        save(entries);
        source.sendFeedback(() -> {
            return Text.literal("Allowed pickup: " + normalizedItem + " -> " + normalizedPlayer + "; current=" + ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist);
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int deny(ServerCommandSource source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer);
        });
        save(entries);
        source.sendFeedback(() -> {
            return Text.literal((removed ? "Removed pickup rule: " : "No pickup rule found: ") + normalizedItem + " -> " + normalizedPlayer);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearItem(ServerCommandSource source, String item) {
        String normalizedItem = normalizeToken(item);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem);
        });
        save(entries);
        source.sendFeedback(() -> {
            return Text.literal((removed ? "Cleared pickup rules for " : "No pickup rules for ") + normalizedItem);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearAll(ServerCommandSource source) {
        ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist = "";
        source.sendFeedback(() -> {
            return Text.literal("Cleared all SurvivalAid pickup rules.");
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int list(ServerCommandSource source) {
        String value = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist.trim();
        if (value.isEmpty() || value.equalsIgnoreCase("none")) {
            source.sendFeedback(() -> {
                return Text.literal("No SurvivalAid pickup rules.");
            }, false);
            return 0;
        }
        source.sendFeedback(() -> {
            return Text.literal("SurvivalAid pickup rules: " + value);
        }, false);
        return entries().size();
    }

    private static List<String> entries() {
        List<String> result = new ArrayList<>();
        String value = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist.trim();
        if (value.isEmpty() || value.equalsIgnoreCase("none")) {
            return result;
        }
        for (String entry : value.split(",")) {
            String trimmed = entry.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private static void save(List<String> entries) {
        ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist = String.join(",", entries);
    }

    private static String[] parseEntry(String entry) {
        int separator = entry.indexOf(61);
        if (separator <= 0 || separator >= entry.length() - 1) {
            return null;
        }
        return new String[]{normalizeToken(entry.substring(0, separator)), normalizeToken(entry.substring(separator + 1))};
    }

    private static String normalizeToken(String token) {
        String value = token.trim();
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
