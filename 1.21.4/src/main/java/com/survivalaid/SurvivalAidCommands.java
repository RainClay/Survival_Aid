package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.ProjectionFillExcludeRule;
import com.survivalaid.features.ProjectionFillRule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

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
        })))
        .then(CommandManager.literal("fill").executes(context7 -> {
            return fill((ServerCommandSource) context7.getSource());
        })));
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
            count += countStackOnline(player.getInventory().getStack(slot), item);
        }
        return count;
    }

    private static int countStackOnline(ItemStack stack, Item item) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int count = stack.isOf(item) ? stack.getCount() : 0;
        ContainerComponent container = stack.getComponents().get(DataComponentTypes.CONTAINER);
        if (container != null) {
            for (ItemStack inner : container.streamNonEmpty().toList()) {
                count += countStackOnline(inner, item);
            }
        }
        return count;
    }

    private static List<String> searchOfflineFakePlayers(MinecraftServer server, Identifier itemId, Set<UUID> onlineUuids) {
        List<String> found = new ArrayList<>();
        try {
            Path playerDataDir = server.getSavePath(WorldSavePath.PLAYERDATA);
            if (!Files.isDirectory(playerDataDir)) {
                return found;
            }
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
                        String name = resolveFakePlayerName(server, uuid, uuidStr);
                        found.add(name + " (离线 " + count + " 个)");
                    }
                });
            }
        } catch (IOException e) {
            // 离线搜索为尽力而为，失败不影响在线结果
        }
        return found;
    }

    private static String resolveFakePlayerName(MinecraftServer server, UUID uuid, String fallback) {
        try {
            return server.getUserCache().getByUuid(uuid).map(profile -> profile.getName()).orElse(fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static int countItemInPlayerData(Path file, Identifier itemId) {
        try {
            NbtCompound tag = NbtIo.readCompressed(file, NbtSizeTracker.of(1000000000L));
            if (!FakePlayerItemSearchRule.survivalAidFakePlayerScanAll && !hasFakeTag(tag)) {
                return 0;
            }
            return countInTag(tag.getList("Inventory", NbtElement.COMPOUND_TYPE), itemId)
                + countInTag(tag.getList("EnderItems", NbtElement.COMPOUND_TYPE), itemId);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean hasFakeTag(NbtCompound tag) {
        if (!tag.contains("Tags", NbtElement.LIST_TYPE)) {
            return false;
        }
        NbtList tags = tag.getList("Tags", NbtElement.STRING_TYPE);
        for (int i = 0; i < tags.size(); i++) {
            if (FakePlayerItemSearchRule.SURVIVAL_AID_FAKE_TAG.equals(tags.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static int countInTag(NbtList list, Identifier itemId) {
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            count += countStack(list.getCompound(i), itemId);
        }
        return count;
    }

    private static int countStack(NbtCompound stack, Identifier itemId) {
        if (stack == null || !stack.contains("id")) {
            return 0;
        }
        int count = 0;
        if (itemId.equals(Identifier.tryParse(stack.getString("id")))) {
            count += stackCount(stack);
        }
        if (stack.contains("components")) {
            NbtCompound components = stack.getCompound("components");
            if (components.contains("minecraft:container")) {
                NbtList container = components.getList("minecraft:container", NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < container.size(); i++) {
                    NbtCompound entry = container.getCompound(i);
                    if (entry.contains("item")) {
                        count += countStack(entry.getCompound("item"), itemId);
                    }
                }
            }
        }
        return count;
    }

    private static int stackCount(NbtCompound stack) {
        if (stack.contains("count", NbtElement.BYTE_TYPE)) {
            return stack.getByte("count");
        }
        if (stack.contains("count", NbtElement.INT_TYPE)) {
            return stack.getInt("count");
        }
        return 1;
    }

    private static int fill(ServerCommandSource source) {
        if (!ProjectionFillRule.survivalAidProjectionFill) {
            source.sendError(Text.literal("投影填充规则未开启（/carpet survivalAidProjectionFill true）。"));
            return 0;
        }
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("须由玩家执行此命令。"));
            return 0;
        }
        Path dir = player.getServer().getRunDirectory().resolve("schematics");
        if (!Files.isDirectory(dir)) {
            source.sendError(Text.literal("schematics/ 目录不存在（请放到服务器 schematics/ 目录）。"));
            return 0;
        }
        ProjectionClientState state = ProjectionSyncStore.get(player.getUuid());
        if (state == null || state.name == null || state.name.isEmpty()) {
            source.sendError(Text.literal("未检测到当前投影，请在客户端 Litematica 中选中一个投影后重试。"));
            return 0;
        }
        List<Path> matches;
        try {
            matches = findSchematicFiles(dir, state.name);
        } catch (IOException e) {
            source.sendError(Text.literal("读取 schematics/ 目录失败。"));
            return 0;
        }
        if (matches.isEmpty()) {
            source.sendError(Text.literal("服务器 schematics/ 下找不到投影文件: " + state.name + "（已含子目录搜索）"));
            return 0;
        }
        if (matches.size() > 1) {
            StringBuilder sb = new StringBuilder("schematics/ 下有多个同名投影文件，请只保留要填充的那个:");
            for (Path p : matches) {
                sb.append("\n").append(dir.relativize(p));
            }
            source.sendError(Text.literal(sb.toString()));
            return 0;
        }
        Path file = matches.get(0);
        Map<Item, Integer> required;
        try {
            required = parseSchematicItems(file, state, parseExcludeBlocks(ProjectionFillExcludeRule.survivalAidFillExcludeBlocks));
        } catch (Exception e) {
            source.sendError(Text.literal("解析投影失败: " + e.getMessage()));
            return 0;
        }
        if (required.isEmpty()) {
            source.sendError(Text.literal("投影中没有需要填充的方块物品。"));
            return 0;
        }
        List<Inventory> containers = findContainersAround(player, 4);
        if (containers.isEmpty()) {
            source.sendError(Text.literal("周围 4 格内没有容器（箱子/桶等）。"));
            return 0;
        }
        Map<Item, Integer> missing = new HashMap<>();
        int filledTypes = fillContainersFromPlayer(containers, player, required, missing);
        final String fileName = file.getFileName().toString();
        final Map<Item, Integer> finalMissing = missing;
        source.sendFeedback(() -> {
            String msg = "已按投影 " + fileName + " 填充 " + filledTypes + " 种物品到周围容器";
            if (finalMissing.isEmpty()) {
                return Text.literal(msg + "。");
            }
            StringBuilder sb = new StringBuilder(msg + "，缺少: ");
            boolean first = true;
            for (Map.Entry<Item, Integer> e : finalMissing.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(Registries.ITEM.getId(e.getKey())).append(' ').append(e.getValue());
            }
            return Text.literal(sb.toString());
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

    private static List<Inventory> findContainersAround(ServerPlayerEntity player, int radius) {
        List<Inventory> result = new ArrayList<>();
        BlockPos center = player.getBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.add(dx, dy, dz);
                    BlockEntity be = player.getWorld().getBlockEntity(p);
                    if (be instanceof Inventory inv) {
                        result.add(inv);
                    }
                }
            }
        }
        return result;
    }


    private static long regionVolume(NbtCompound region) {
        if (region.contains("Size", NbtElement.LIST_TYPE)) {
            NbtList size = region.getList("Size", NbtElement.INT_TYPE);
            if (size.size() >= 3) {
                return Math.abs((long) size.getInt(0) * size.getInt(1) * size.getInt(2));
            }
        }
        if (region.contains("Size", NbtElement.COMPOUND_TYPE)) {
            NbtCompound size = region.getCompound("Size");
            long sx = size.contains("x") ? size.getInt("x") : 0;
            long sy = size.contains("y") ? size.getInt("y") : 0;
            long sz = size.contains("z") ? size.getInt("z") : 0;
            return Math.abs(sx * sy * sz);
        }
        return 0;
    }

    private static int[] regionAxes(NbtCompound region) {
        int sx = 0;
        int sz = 0;
        int posY = 0;
        int sizeY = 0;
        if (region.contains("Size", NbtElement.COMPOUND_TYPE)) {
            NbtCompound size = region.getCompound("Size");
            sx = Math.abs(size.getInt("x"));
            sz = Math.abs(size.getInt("z"));
            sizeY = size.getInt("y");
        } else if (region.contains("Size", NbtElement.LIST_TYPE)) {
            NbtList size = region.getList("Size", NbtElement.INT_TYPE);
            if (size.size() >= 3) {
                sx = Math.abs(size.getInt(0));
                sz = Math.abs(size.getInt(2));
                sizeY = size.getInt(1);
            }
        }
        if (region.contains("Position", NbtElement.COMPOUND_TYPE)) {
            NbtCompound pos = region.getCompound("Position");
            posY = pos.getInt("y");
        } else if (region.contains("Position", NbtElement.LIST_TYPE)) {
            NbtList pos = region.getList("Position", NbtElement.INT_TYPE);
            if (pos.size() >= 3) {
                posY = pos.getInt(1);
            }
        }
        return new int[]{sx, sz, posY, sizeY};
    }

    private static Set<String> parseExcludeBlocks(String cfg) {
        Set<String> result = new HashSet<>();
        if (cfg == null || cfg.isEmpty()) {
            return result;
        }
        for (String t : cfg.split(",")) {
            String id = t.trim();
            if (id.isEmpty()) {
                continue;
            }
            if (!id.contains(":")) {
                id = "minecraft:" + id;
            }
            result.add(id);
        }
        return result;
    }

    private static Map<Item, Integer> parseSchematicItems(Path file, ProjectionClientState state, Set<String> excludeBlocks) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file, NbtSizeTracker.of(1000000000L));
        NbtCompound regions = root.getCompound("Regions");
        Map<String, Integer> blockCounts = new LinkedHashMap<>();
        for (String regionName : regions.getKeys()) {
            NbtCompound region = regions.getCompound(regionName);
            long volume = regionVolume(region);
            if (volume <= 0) {
                continue;
            }
            NbtList palette = region.getList("BlockStatePalette", NbtElement.COMPOUND_TYPE);
            if (palette.isEmpty()) {
                continue;
            }
            List<String> paletteIds = new ArrayList<>();
            for (int i = 0; i < palette.size(); i++) {
                paletteIds.add(palette.getCompound(i).getString("Name"));
            }
            long[] blockStates = region.getLongArray("BlockStates");
            if (blockStates.length == 0) {
                continue;
            }
            int bits = Math.max(1, (int) Math.ceil(Math.log(paletteIds.size()) / Math.log(2)));
            long mask = bits >= 32 ? -1L : ((1L << bits) - 1L);
            int[] axes = regionAxes(region);
            int slab = axes[0] * axes[1];
            boolean filter = state.isLayerFiltered();
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
                    if (filter) {
                        int localY = (int) (i / slab);
                        int schedY = (axes[3] < 0) ? (axes[2] + axes[3] + 1 + localY) : (axes[2] + localY);
                        int worldY = state.originY + schedY;
                        if (worldY < state.minY || worldY > state.maxY) {
                            continue;
                        }
                    }
                    counts[idx]++;
                }
            }
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] > 0 && !excludeBlocks.contains(paletteIds.get(i))) {
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
            Block block = Registries.BLOCK.get(id);
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

    private static int fillContainersFromPlayer(List<Inventory> containers, ServerPlayerEntity player, Map<Item, Integer> required, Map<Item, Integer> missing) {
        net.minecraft.entity.player.PlayerInventory inv = player.getInventory();
        int filledTypes = 0;
        for (Map.Entry<Item, Integer> e : required.entrySet()) {
            Item item = e.getKey();
            int needed = e.getValue();
            int taken = 0;
            for (int slot = 0; slot < inv.size() && taken < needed; slot++) {
                ItemStack stack = inv.getStack(slot);
                if (stack.isEmpty() || !stack.isOf(item)) {
                    continue;
                }
                int toTake = Math.min(stack.getCount(), needed - taken);
                ItemStack removed = inv.removeStack(slot, toTake);
                if (removed.isEmpty()) {
                    continue;
                }
                int placed = placeIntoContainers(containers, removed);
                taken += placed;
                if (placed < removed.getCount()) {
                    ItemStack leftover = removed.copyWithCount(removed.getCount() - placed);
                    inv.offerOrDrop(leftover);
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

    private static int placeIntoContainers(List<Inventory> containers, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        int count = stack.getCount();
        int max = stack.getMaxCount();
        int placed = 0;
        for (Inventory container : containers) {
            for (int slot = 0; slot < container.size() && count > 0; slot++) {
                ItemStack cur = container.getStack(slot);
                if (!cur.isEmpty() && cur.isOf(item) && cur.getCount() < max) {
                    int add = Math.min(max - cur.getCount(), count);
                    cur.increment(add);
                    count -= add;
                    placed += add;
                }
            }
        }
        for (Inventory container : containers) {
            for (int slot = 0; slot < container.size() && count > 0; slot++) {
                if (container.getStack(slot).isEmpty()) {
                    int put = Math.min(max, count);
                    container.setStack(slot, stack.copyWithCount(put));
                    count -= put;
                    placed += put;
                }
            }
        }
        for (Inventory container : containers) {
            container.markDirty();
        }
        return placed;
    }
}
