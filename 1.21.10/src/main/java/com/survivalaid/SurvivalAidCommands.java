package com.survivalaid;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import com.survivalaid.features.ItemPickupFilterRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/SurvivalAidCommands.class */
public final class SurvivalAidCommands {
    private SurvivalAidCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("survivalaid").requires(source -> {
            return source.hasPermissionLevel(2);
        }).then(CommandManager.literal("pickup")
            .then(CommandManager.literal("allow").then(CommandManager.argument("item", StringArgumentType.word()).then(CommandManager.argument("player", StringArgumentType.word()).executes(context -> {
                return allow((ServerCommandSource) context.getSource(), StringArgumentType.getString(context, "item"), StringArgumentType.getString(context, "player"));
            }))))
            .then(CommandManager.literal("deny").then(CommandManager.argument("item", StringArgumentType.word()).then(CommandManager.argument("player", StringArgumentType.word()).executes(context2 -> {
                return deny((ServerCommandSource) context2.getSource(), StringArgumentType.getString(context2, "item"), StringArgumentType.getString(context2, "player"));
            }))))
            .then(CommandManager.literal("clear").executes(context3 -> {
                return clearAll((ServerCommandSource) context3.getSource());
            }).then(CommandManager.argument("item", StringArgumentType.word()).executes(context4 -> {
                return clearItem((ServerCommandSource) context4.getSource(), StringArgumentType.getString(context4, "item"));
            })))
            .then(CommandManager.literal("list").executes(context5 -> {
                return list((ServerCommandSource) context5.getSource());
            })))
        .then(CommandManager.literal("searchitem").then(CommandManager.argument("item", StringArgumentType.word()).executes(context6 -> {
            return searchItem((ServerCommandSource) context6.getSource(), StringArgumentType.getString(context6, "item"));
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

    private static int searchItem(ServerCommandSource source, String itemName) {
        if (!FakePlayerItemSearchRule.survivalAidFakePlayerItemSearch) {
            source.sendError(Text.literal("SurvivalAid 假人物品搜索规则未开启（/carpet survivalAidFakePlayerItemSearch true）。"));
            return 0;
        }
        String normalizedItem = normalizeToken(itemName);
        Identifier itemId = Identifier.tryParse(normalizedItem);
        if (itemId == null) {
            itemId = Identifier.tryParse("minecraft:" + normalizedItem);
        }
        if (itemId == null) {
            source.sendError(Text.literal("无效的物品 ID: " + normalizedItem));
            return 0;
        }
        Item item = Registries.ITEM.get(itemId);
        if (item == null) {
            source.sendError(Text.literal("找不到物品: " + itemId));
            return 0;
        }
        MinecraftServer server = source.getServer();
        final Identifier finalItemId = itemId;
        List<String> found = new ArrayList<>();
        Set<UUID> onlineUuids = new HashSet<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!(((Object) player) instanceof EntityPlayerMPFake)) {
                continue;
            }
            onlineUuids.add(player.getUuid());
            int count = countItemInInventory(player, item);
            if (count > 0) {
                found.add(player.getName().getString() + " (" + count + " 个)");
            }
        }
        found.addAll(searchOfflineFakePlayers(server, itemId, onlineUuids));
        if (found.isEmpty()) {
            source.sendFeedback(() -> {
                return Text.literal("没有假人携带 " + finalItemId + "。");
            }, false);
            return 0;
        }
        source.sendFeedback(() -> {
            return Text.literal("携带 " + finalItemId + " 的假人: " + String.join(", ", found));
        }, false);
        return found.size();
    }

    private static int countItemInInventory(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static List<String> searchOfflineFakePlayers(MinecraftServer server, Identifier itemId, Set<UUID> onlineUuids) {
        List<String> found = new ArrayList<>();
        try {
            Path playerDataDir = server.getSavePath(WorldSavePath.PLAYERS);
            if (!Files.isDirectory(playerDataDir)) {
                return found;
            }
            Map<String, String> uuidToName = loadUsercache(server);
            try (var stream = Files.list(playerDataDir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".dat")).forEach(p -> {
                    String fileName = p.getFileName().toString();
                    String uuidStr = fileName.substring(0, fileName.length() - 4);
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        return;
                    }
                    if (uuid.version() != 3 || onlineUuids.contains(uuid)) {
                        return;
                    }
                    int count = countItemInPlayerData(p, itemId);
                    if (count > 0) {
                        String name = uuidToName.getOrDefault(uuidStr.toLowerCase(), uuidStr);
                        found.add(name + " (离线 " + count + " 个)");
                    }
                });
            }
        } catch (IOException e) {
            // 离线搜索为尽力而为，失败不影响在线结果
        }
        return found;
    }

    private static Map<String, String> loadUsercache(MinecraftServer server) {
        Map<String, String> map = new HashMap<>();
        try {
            Path usercache = server.getSavePath(WorldSavePath.ROOT).resolve("usercache.json");
            if (!Files.isReadable(usercache)) {
                return map;
            }
            try (var reader = Files.newBufferedReader(usercache)) {
                JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement element : arr) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("name") && obj.has("uuid")) {
                        map.put(obj.get("uuid").getAsString().toLowerCase(), obj.get("name").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            // 尽力而为
        }
        return map;
    }

    private static int countItemInPlayerData(Path file, Identifier itemId) {
        try {
            NbtCompound tag = NbtIo.readCompressed(file, NbtSizeTracker.of(1000000000L));
            return countInTag(tag.getList("Inventory"), itemId)
                + countInTag(tag.getList("EnderItems"), itemId);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int countInTag(Optional<NbtList> listOpt, Identifier itemId) {
        if (listOpt.isEmpty()) {
            return 0;
        }
        NbtList list = listOpt.get();
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Optional<NbtCompound> stackOpt = list.getCompound(i);
            if (stackOpt.isEmpty()) {
                continue;
            }
            NbtCompound stack = stackOpt.get();
            Optional<String> idOpt = stack.getString("id");
            if (idOpt.isEmpty()) {
                continue;
            }
            Identifier stackId = Identifier.tryParse(idOpt.get());
            if (!itemId.equals(stackId)) {
                continue;
            }
            int amount = 1;
            Optional<Byte> byteVal = stack.getByte("count");
            if (byteVal.isPresent()) {
                amount = byteVal.get();
            } else {
                Optional<Integer> intVal = stack.getInt("count");
                if (intVal.isPresent()) {
                    amount = intVal.get();
                }
            }
            count += amount;
        }
        return count;
    }
}
