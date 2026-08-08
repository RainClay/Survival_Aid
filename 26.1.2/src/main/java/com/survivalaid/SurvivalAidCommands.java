package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.ProjectionFillRule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidCommands.class */
public final class SurvivalAidCommands {
    private SurvivalAidCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("survivalaid").requires(source -> {
            return source.permissions() != PermissionSet.NO_PERMISSIONS;
        }).then(Commands.literal("pickup")
            .then(Commands.literal("allow").then(Commands.argument("item", StringArgumentType.word()).then(Commands.argument("player", StringArgumentType.word()).executes(context -> {
                return allow((CommandSourceStack) context.getSource(), StringArgumentType.getString(context, "item"), StringArgumentType.getString(context, "player"));
            }))))
            .then(Commands.literal("deny").then(Commands.argument("item", StringArgumentType.word()).then(Commands.argument("player", StringArgumentType.word()).executes(context2 -> {
                return deny((CommandSourceStack) context2.getSource(), StringArgumentType.getString(context2, "item"), StringArgumentType.getString(context2, "player"));
            }))))
            .then(Commands.literal("clear").executes(context3 -> {
                return clearAll((CommandSourceStack) context3.getSource());
            }).then(Commands.argument("item", StringArgumentType.word()).executes(context4 -> {
                return clearItem((CommandSourceStack) context4.getSource(), StringArgumentType.getString(context4, "item"));
            })))
            .then(Commands.literal("list").executes(context5 -> {
                return list((CommandSourceStack) context5.getSource());
            })))
        .then(Commands.literal("searchitem").then(Commands.argument("item", StringArgumentType.word()).executes(context6 -> {
            return searchItem((CommandSourceStack) context6.getSource(), StringArgumentType.getString(context6, "item"));
        })))
        .then(Commands.literal("fill").executes(context7 -> {
            return fill((CommandSourceStack) context7.getSource());
        })));
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
        Item item = BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(null);
        if (item == null) {
            source.sendFailure(Component.literal("找不到物品: " + itemId));
            return 0;
        }
        MinecraftServer server = source.getServer();
        final Identifier finalItemId = itemId;
        List<String> found = new ArrayList<>();
        Set<UUID> onlineUuids = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(((Object) player) instanceof EntityPlayerMPFake)) {
                continue;
            }
            onlineUuids.add(player.getUUID());
            int count = countItemInInventory(player, item);
            if (count > 0) {
                found.add(player.getName().getString() + " (" + count + " 个)");
            }
        }
        found.addAll(searchOfflineFakePlayers(server, itemId, onlineUuids));
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
            count += countStackOnline(player.getInventory().getItem(slot), item);
        }
        return count;
    }

    private static int countStackOnline(ItemStack stack, Item item) {
        if (stack.isEmpty()) {
            return 0;
        }
        int count = stack.is(item) ? stack.getCount() : 0;
        ItemContainerContents container = stack.getComponents().get(DataComponents.CONTAINER);
        if (container != null) {
            for (ItemStack inner : container.nonEmptyItemCopyStream().toList()) {
                count += countStackOnline(inner, item);
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
    private static List<String> searchOfflineFakePlayers(MinecraftServer server, Identifier itemId, Set<UUID> onlineUuids) {
        List<String> found = new ArrayList<>();
        try {
            Path playerDataDir = server.getWorldPath(LevelResource.PLAYER_DATA_DIR);
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
                    if (onlineUuids.contains(uuid)) {
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
            Path usercache = server.getServerDirectory().resolve("usercache.json");
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
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            if (!FakePlayerItemSearchRule.survivalAidFakePlayerScanAll && !hasFakeTag(tag)) {
                return 0;
            }
            return countInTag(tag.getList("Inventory"), itemId)
                + countInTag(tag.getList("EnderItems"), itemId);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean hasFakeTag(CompoundTag tag) {
        Optional<ListTag> tagsOpt = tag.getList("Tags");
        if (tagsOpt.isEmpty()) {
            return false;
        }
        ListTag tags = tagsOpt.get();
        for (int i = 0; i < tags.size(); i++) {
            Optional<String> t = tags.getString(i);
            if (t.isPresent() && FakePlayerItemSearchRule.SURVIVAL_AID_FAKE_TAG.equals(t.get())) {
                return true;
            }
        }
        return false;
    }

    private static int countInTag(Optional<ListTag> listOpt, Identifier itemId) {
        if (listOpt.isEmpty()) {
            return 0;
        }
        ListTag list = listOpt.get();
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Optional<CompoundTag> stackOpt = list.getCompound(i);
            if (stackOpt.isEmpty()) {
                continue;
            }
            count += countStack(stackOpt.get(), itemId);
        }
        return count;
    }

    private static int countStack(CompoundTag stack, Identifier itemId) {
        int count = 0;
        Optional<String> idOpt = stack.getString("id");
        if (idOpt.isPresent() && itemId.equals(Identifier.tryParse(idOpt.get()))) {
            count += stackCount(stack);
        }
        Optional<CompoundTag> compOpt = stack.getCompound("components");
        if (compOpt.isPresent()) {
            Optional<ListTag> contOpt = compOpt.get().getList("minecraft:container");
            if (contOpt.isPresent()) {
                ListTag container = contOpt.get();
                for (int i = 0; i < container.size(); i++) {
                    Optional<CompoundTag> entryOpt = container.getCompound(i);
                    if (entryOpt.isEmpty()) {
                        continue;
                    }
                    Optional<CompoundTag> itemOpt = entryOpt.get().getCompound("item");
                    if (itemOpt.isPresent()) {
                        count += countStack(itemOpt.get(), itemId);
                    }
                }
            }
        }
        return count;
    }

    private static int stackCount(CompoundTag stack) {
        Optional<Byte> byteVal = stack.getByte("count");
        if (byteVal.isPresent()) {
            return byteVal.get();
        }
        Optional<Integer> intVal = stack.getInt("count");
        return intVal.orElse(1);
    }

    private static int fill(CommandSourceStack source) {
        if (!ProjectionFillRule.survivalAidProjectionFill) {
            source.sendFailure(Component.literal("投影填充规则未开启（/carpet survivalAidProjectionFill true）。"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("须由玩家执行此命令。"));
            return 0;
        }
        Path dir = source.getServer().getServerDirectory().resolve("schematics");
        if (!Files.isDirectory(dir)) {
            source.sendFailure(Component.literal("schematics/ 目录不存在（请放到服务器 schematics/ 目录）。"));
            return 0;
        }
        String proj = ProjectionSyncStore.get(player.getUUID());
        if (proj == null || proj.isEmpty()) {
            source.sendFailure(Component.literal("未检测到当前投影，请在客户端 Litematica 中选中一个投影后重试。"));
            return 0;
        }
        List<Path> matches;
        try {
            matches = findSchematicFiles(dir, proj);
        } catch (IOException e) {
            source.sendFailure(Component.literal("读取 schematics/ 目录失败。"));
            return 0;
        }
        if (matches.isEmpty()) {
            source.sendFailure(Component.literal("服务器 schematics/ 下找不到投影文件: " + proj + "（已含子目录搜索）"));
            return 0;
        }
        if (matches.size() > 1) {
            StringBuilder sb = new StringBuilder("schematics/ 下有多个同名投影文件，请只保留要填充的那个:");
            for (Path p : matches) {
                sb.append("\n").append(dir.relativize(p));
            }
            source.sendFailure(Component.literal(sb.toString()));
            return 0;
        }
        Path file = matches.get(0);
        Map<Item, Integer> required;
        try {
            required = parseSchematicItems(file);
        } catch (Exception e) {
            source.sendFailure(Component.literal("解析投影失败: " + e.getMessage()));
            return 0;
        }
        if (required.isEmpty()) {
            source.sendFailure(Component.literal("投影中没有需要填充的方块物品。"));
            return 0;
        }
        List<Container> containers = findContainersAround(player, 4);
        if (containers.isEmpty()) {
            source.sendFailure(Component.literal("周围 4 格内没有容器（箱子/桶等）。"));
            return 0;
        }
        Map<Item, Integer> missing = new HashMap<>();
        int filledTypes = fillContainersFromPlayer(containers, player, required, missing);
        final String fileName = file.getFileName().toString();
        final Map<Item, Integer> finalMissing = missing;
        source.sendSuccess(() -> {
            String msg = "已按投影 " + fileName + " 填充 " + filledTypes + " 种物品到周围容器";
            if (finalMissing.isEmpty()) {
                return Component.literal(msg + "。");
            }
            StringBuilder sb = new StringBuilder(msg + "，缺少: ");
            boolean first = true;
            for (Map.Entry<Item, Integer> e : finalMissing.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(BuiltInRegistries.ITEM.getKey(e.getKey())).append(' ').append(e.getValue());
            }
            return Component.literal(sb.toString());
        }, true);
        return filledTypes;
    }

    private static List<Path> findSchematicFiles(Path root, String name) throws IOException {
        List<Path> result = new ArrayList<>();
        String target = name.endsWith(".litematic") ? name : name + ".litematic";
        try (var stream = Files.walk(root)) {
            stream.filter(p -> Files.isRegularFile(p)).filter(p -> p.getFileName().toString().equalsIgnoreCase(target)).forEach(result::add);
        }
        return result;
    }

    private static List<Container> findContainersAround(ServerPlayer player, int radius) {
        List<Container> result = new ArrayList<>();
        BlockPos center = player.blockPosition();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockEntity be = player.level().getBlockEntity(p);
                    if (be instanceof Container container) {
                        result.add(container);
                    }
                }
            }
        }
        return result;
    }

    private static long regionVolume(CompoundTag region) {
        Optional<CompoundTag> sizeComp = region.getCompound("Size");
        if (sizeComp.isPresent()) {
            CompoundTag size = sizeComp.get();
            long sx = size.getInt("x").orElse(0);
            long sy = size.getInt("y").orElse(0);
            long sz = size.getInt("z").orElse(0);
            return Math.abs(sx * sy * sz);
        }
        Optional<ListTag> sizeList = region.getList("Size");
        if (sizeList.isPresent()) {
            ListTag size = sizeList.get();
            if (size.size() >= 3) {
                return Math.abs((long) size.getIntOr(0, 0) * size.getIntOr(1, 0) * size.getIntOr(2, 0));
            }
        }
        return 0;
    }

    private static Map<Item, Integer> parseSchematicItems(Path file) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        Optional<CompoundTag> regionsOpt = root.getCompound("Regions");
        Map<String, Integer> blockCounts = new LinkedHashMap<>();
        if (regionsOpt.isEmpty()) {
            return new LinkedHashMap<>();
        }
        CompoundTag regions = regionsOpt.get();
        for (String regionName : regions.keySet()) {
            Optional<CompoundTag> regionOpt = regions.getCompound(regionName);
            if (regionOpt.isEmpty()) {
                continue;
            }
            CompoundTag region = regionOpt.get();
            long volume = regionVolume(region);
            if (volume <= 0) {
                continue;
            }
            Optional<ListTag> paletteOpt = region.getList("BlockStatePalette");
            if (paletteOpt.isEmpty()) {
                continue;
            }
            ListTag palette = paletteOpt.get();
            if (palette.isEmpty()) {
                continue;
            }
            List<String> paletteIds = new ArrayList<>();
            for (int i = 0; i < palette.size(); i++) {
                Optional<CompoundTag> entryOpt = palette.getCompound(i);
                if (entryOpt.isEmpty()) {
                    continue;
                }
                Optional<String> nameOpt = entryOpt.get().getString("Name");
                nameOpt.ifPresent(paletteIds::add);
            }
            if (paletteIds.isEmpty()) {
                continue;
            }
            Optional<long[]> statesOpt = region.getLongArray("BlockStates");
            if (statesOpt.isEmpty()) {
                continue;
            }
            long[] blockStates = statesOpt.get();
            if (blockStates.length == 0) {
                continue;
            }
            int bits = Math.max(1, (int) Math.ceil(Math.log(paletteIds.size()) / Math.log(2)));
            long mask = bits >= 32 ? -1L : ((1L << bits) - 1L);
            int[] counts = new int[paletteIds.size()];
            for (int i = 0; i < volume; i++) {
                int startBit = i * bits;
                int arr = startBit >> 6;
                int off = startBit & 63;
                if (arr >= blockStates.length) {
                    break;
                }
                long value;
                if (off + bits <= 64) {
                    value = (blockStates[arr] >>> off) & mask;
                } else if (arr + 1 < blockStates.length) {
                    value = ((blockStates[arr] >>> off) | (blockStates[arr + 1] << (64 - off))) & mask;
                } else {
                    value = (blockStates[arr] >>> off) & mask;
                }
                int idx = (int) value;
                if (idx >= 0 && idx < counts.length) {
                    counts[idx]++;
                }
            }
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] > 0) {
                    blockCounts.merge(paletteIds.get(i), counts[i], Integer::sum);
                }
            }
        }
        Map<Item, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : blockCounts.entrySet()) {
            Identifier id = Identifier.tryParse(e.getKey());
            if (id == null && e.getKey().indexOf(':') < 0) {
                id = Identifier.tryParse("minecraft:" + e.getKey());
            }
            if (id == null) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(id).map(h -> h.value()).orElse(null);
            if (block == null) {
                continue;
            }
            Item item = block.asItem();
            if (item == null || item == Items.AIR) {
                continue;
            }
            result.merge(item, e.getValue(), Integer::sum);
        }
        return result;
    }

    private static int fillContainersFromPlayer(List<Container> containers, ServerPlayer player, Map<Item, Integer> required, Map<Item, Integer> missing) {
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        int filledTypes = 0;
        for (Map.Entry<Item, Integer> e : required.entrySet()) {
            Item item = e.getKey();
            int needed = e.getValue();
            int taken = 0;
            for (int slot = 0; slot < inv.getContainerSize() && taken < needed; slot++) {
                ItemStack stack = inv.getItem(slot);
                if (stack.isEmpty() || !stack.is(item)) {
                    continue;
                }
                int toTake = Math.min(stack.getCount(), needed - taken);
                ItemStack removed = inv.removeItem(slot, toTake);
                if (removed.isEmpty()) {
                    continue;
                }
                int placed = placeIntoContainers(containers, removed);
                taken += placed;
                if (placed < removed.getCount()) {
                    ItemStack leftover = removed.copyWithCount(removed.getCount() - placed);
                    inv.placeItemBackInInventory(leftover);
                }
            }
            if (taken > 0) {
                filledTypes++;
            }
            if (taken < needed) {
                missing.put(item, needed - taken);
            }
        }
        return filledTypes;
    }

    private static int placeIntoContainers(List<Container> containers, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        int count = stack.getCount();
        int max = item.getDefaultMaxStackSize();
        int placed = 0;
        for (Container container : containers) {
            for (int slot = 0; slot < container.getContainerSize() && count > 0; slot++) {
                ItemStack cur = container.getItem(slot);
                if (!cur.isEmpty() && cur.is(item) && cur.getCount() < max) {
                    int add = Math.min(max - cur.getCount(), count);
                    cur.grow(add);
                    count -= add;
                    placed += add;
                }
            }
        }
        for (Container container : containers) {
            for (int slot = 0; slot < container.getContainerSize() && count > 0; slot++) {
                if (container.getItem(slot).isEmpty()) {
                    int put = Math.min(max, count);
                    container.setItem(slot, stack.copyWithCount(put));
                    count -= put;
                    placed += put;
                }
            }
        }
        for (Container container : containers) {
            container.setChanged();
        }
        return placed;
    }
}
