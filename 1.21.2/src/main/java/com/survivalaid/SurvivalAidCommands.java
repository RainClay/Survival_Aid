package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.survivalaid.features.ItemPickupFilterRule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2561;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.2-1.0.1.jar:com/survivalaid/SurvivalAidCommands.class */
public final class SurvivalAidCommands {
    private SurvivalAidCommands() {
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(class_2170.method_9247("survivalaid").requires(source -> {
            return source.method_9259(2);
        }).then(class_2170.method_9247("pickup").then(class_2170.method_9247("allow").then(class_2170.method_9244("item", StringArgumentType.word()).then(class_2170.method_9244("player", StringArgumentType.word()).executes(context -> {
            return allow((class_2168) context.getSource(), StringArgumentType.getString(context, "item"), StringArgumentType.getString(context, "player"));
        })))).then(class_2170.method_9247("deny").then(class_2170.method_9244("item", StringArgumentType.word()).then(class_2170.method_9244("player", StringArgumentType.word()).executes(context2 -> {
            return deny((class_2168) context2.getSource(), StringArgumentType.getString(context2, "item"), StringArgumentType.getString(context2, "player"));
        })))).then(class_2170.method_9247("clear").executes(context3 -> {
            return clearAll((class_2168) context3.getSource());
        }).then(class_2170.method_9244("item", StringArgumentType.word()).executes(context4 -> {
            return clearItem((class_2168) context4.getSource(), StringArgumentType.getString(context4, "item"));
        }))).then(class_2170.method_9247("list").executes(context5 -> {
            return list((class_2168) context5.getSource());
        }))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int allow(class_2168 source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        if (normalizedItem.isEmpty() || normalizedPlayer.isEmpty()) {
            source.method_9213(class_2561.method_43470("Usage: /survivalaid pickup allow <item> <player>"));
            return 0;
        }
        List<String> entries = entries();
        String newEntry = normalizedItem + "=" + normalizedPlayer;
        for (String entry : entries) {
            String[] parsed = parseEntry(entry);
            if (parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer)) {
                source.method_9226(() -> {
                    return class_2561.method_43470("Already allowed: " + normalizedItem + " -> " + normalizedPlayer);
                }, false);
                return 0;
            }
        }
        entries.add(newEntry);
        save(entries);
        source.method_9226(() -> {
            return class_2561.method_43470("Allowed pickup: " + normalizedItem + " -> " + normalizedPlayer + "; current=" + ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist);
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int deny(class_2168 source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer);
        });
        save(entries);
        source.method_9226(() -> {
            return class_2561.method_43470((removed ? "Removed pickup rule: " : "No pickup rule found: ") + normalizedItem + " -> " + normalizedPlayer);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearItem(class_2168 source, String item) {
        String normalizedItem = normalizeToken(item);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem);
        });
        save(entries);
        source.method_9226(() -> {
            return class_2561.method_43470((removed ? "Cleared pickup rules for " : "No pickup rules for ") + normalizedItem);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearAll(class_2168 source) {
        ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist = "";
        source.method_9226(() -> {
            return class_2561.method_43470("Cleared all SurvivalAid pickup rules.");
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int list(class_2168 source) {
        String value = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist.trim();
        if (value.isEmpty() || value.equalsIgnoreCase("none")) {
            source.method_9226(() -> {
                return class_2561.method_43470("No SurvivalAid pickup rules.");
            }, false);
            return 0;
        }
        source.method_9226(() -> {
            return class_2561.method_43470("SurvivalAid pickup rules: " + value);
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
