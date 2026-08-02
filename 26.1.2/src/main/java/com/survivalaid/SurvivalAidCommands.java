package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import com.survivalaid.features.ItemPickupFilterRule;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidCommands.class */
public final class SurvivalAidCommands {
    private SurvivalAidCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("survivalaid").requires(source -> {
            return source.permissions() != PermissionSet.NO_PERMISSIONS;
        }).then(Commands.literal("pickup").then(Commands.literal("allow").then(Commands.argument("item", StringArgumentType.word()).then(Commands.argument("player", StringArgumentType.word()).executes(context -> {
            return allow((CommandSourceStack) context.getSource(), StringArgumentType.getString(context, "item"), StringArgumentType.getString(context, "player"));
        })))).then(Commands.literal("deny").then(Commands.argument("item", StringArgumentType.word()).then(Commands.argument("player", StringArgumentType.word()).executes(context2 -> {
            return deny((CommandSourceStack) context2.getSource(), StringArgumentType.getString(context2, "item"), StringArgumentType.getString(context2, "player"));
        })))).then(Commands.literal("clear").executes(context3 -> {
            return clearAll((CommandSourceStack) context3.getSource());
        }).then(Commands.argument("item", StringArgumentType.word()).executes(context4 -> {
            return clearItem((CommandSourceStack) context4.getSource(), StringArgumentType.getString(context4, "item"));
        }))).then(Commands.literal("list").executes(context5 -> {
            return list((CommandSourceStack) context5.getSource());
        })))).then(Commands.literal("searchitem").then(Commands.argument("item", StringArgumentType.word()).executes(context6 -> {
            return searchItem((CommandSourceStack) context6.getSource(), StringArgumentType.getString(context6, "item"));
        }))));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int allow(CommandSourceStack source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        if (normalizedItem.isEmpty() || normalizedPlayer.isEmpty()) {
            source.sendFailure(Component.literal("Usage: /survivalaid pickup allow <item> <player>"));
            return 0;
        }
        List<String> entries = entries();
        String newEntry = normalizedItem + "=" + normalizedPlayer;
        for (String entry : entries) {
            String[] parsed = parseEntry(entry);
            if (parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer)) {
                source.sendSuccess(() -> {
                    return Component.literal("Already allowed: " + normalizedItem + " -> " + normalizedPlayer);
                }, false);
                return 0;
            }
        }
        entries.add(newEntry);
        save(entries);
        source.sendSuccess(() -> {
            return Component.literal("Allowed pickup: " + normalizedItem + " -> " + normalizedPlayer + "; current=" + ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist);
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int deny(CommandSourceStack source, String item, String player) {
        String normalizedItem = normalizeToken(item);
        String normalizedPlayer = normalizeToken(player);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer);
        });
        save(entries);
        source.sendSuccess(() -> {
            return Component.literal((removed ? "Removed pickup rule: " : "No pickup rule found: ") + normalizedItem + " -> " + normalizedPlayer);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearItem(CommandSourceStack source, String item) {
        String normalizedItem = normalizeToken(item);
        List<String> entries = entries();
        boolean removed = entries.removeIf(entry -> {
            String[] parsed = parseEntry(entry);
            return parsed != null && parsed[0].equalsIgnoreCase(normalizedItem);
        });
        save(entries);
        source.sendSuccess(() -> {
            return Component.literal((removed ? "Cleared pickup rules for " : "No pickup rules for ") + normalizedItem);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearAll(CommandSourceStack source) {
        ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist = "";
        source.sendSuccess(() -> {
            return Component.literal("Cleared all SurvivalAid pickup rules.");
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int list(CommandSourceStack source) {
        String value = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist.trim();
        if (value.isEmpty() || value.equalsIgnoreCase("none")) {
            source.sendSuccess(() -> {
                return Component.literal("No SurvivalAid pickup rules.");
            }, false);
            return 0;
        }
        source.sendSuccess(() -> {
            return Component.literal("SurvivalAid pickup rules: " + value);
        }, false);
        return entries().size();
    }

    private static int searchItem(CommandSourceStack source, String itemName) {
        if (!FakePlayerItemSearchRule.survivalAidFakePlayerItemSearch) {
            source.sendFailure(Component.literal("SurvivalAid 假人物品搜索规则未开启（/carpet survivalAidFakePlayerItemSearch true）。"));
            return 0;
        }
        String normalizedItem = normalizeToken(itemName);
        Identifier itemId = Identifier.tryParse(normalizedItem);
        if (itemId == null) {
            itemId = Identifier.tryParse("minecraft:" + normalizedItem);
        }
        if (itemId == null) {
            source.sendFailure(Component.literal("无效的物品 ID: " + normalizedItem));
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null) {
            source.sendFailure(Component.literal("找不到物品: " + itemId));
            return 0;
        }
        MinecraftServer server = source.getServer();
        final Identifier finalItemId = itemId;
        List<String> found = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(((Object) player) instanceof EntityPlayerMPFake)) {
                continue;
            }
            int count = countItemInInventory(player, item);
            if (count > 0) {
                found.add(player.getName().getString() + " (" + count + " 个)");
            }
        }
        if (found.isEmpty()) {
            source.sendSuccess(() -> {
                return Component.literal("没有假人携带 " + finalItemId + "。");
            }, false);
            return 0;
        }
        source.sendSuccess(() -> {
            return Component.literal("携带 " + finalItemId + " 的假人: " + String.join(", ", found));
        }, false);
        return found.size();
    }

    private static int countItemInInventory(ServerPlayer player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
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
