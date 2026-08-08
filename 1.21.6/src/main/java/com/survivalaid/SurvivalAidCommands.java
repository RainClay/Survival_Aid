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
import java.util.Optional;
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
import net.minecraft.world.World;

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
            return countInTag(tag.getList("Inventory"), itemId)
                + countInTag(tag.getList("EnderItems"), itemId);
        } catch (Exception e) {
            return 0;
        }
    }

    private static boolean hasFakeTag(NbtCompound tag) {
        Optional<NbtList> tagsOpt = tag.getList("Tags");
        if (tagsOpt.isEmpty()) {
            return false;
        }
        NbtList tags = tagsOpt.get();
        for (int i = 0; i < tags.size(); i++) {
            Optional<String> t = tags.getString(i);
            if (t.isPresent() && FakePlayerItemSearchRule.SURVIVAL_AID_FAKE_TAG.equals(t.get())) {
                return true;
            }
        }
        return false;
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
            count += countStack(stackOpt.get(), itemId);
        }
        return count;
    }

    private static int countStack(NbtCompound stack, Identifier itemId) {
        int count = 0;
        Optional<String> idOpt = stack.getString("id");
        if (idOpt.isPresent() && itemId.equals(Identifier.tryParse(idOpt.get()))) {
            count += stackCount(stack);
        }
        Optional<NbtCompound> compOpt = stack.getCompound("components");
        if (compOpt.isPresent()) {
            Optional<NbtList> contOpt = compOpt.get().getList("minecraft:container");
            if (contOpt.isPresent()) {
                NbtList container = contOpt.get();
                for (int i = 0; i < container.size(); i++) {
                    Optional<NbtCompound> entryOpt = container.getCompound(i);
                    if (entryOpt.isEmpty()) {
                        continue;
                    }
                    Optional<NbtCompound> itemOpt = entryOpt.get().getCompound("item");
                    if (itemOpt.isPresent()) {
                        count += countStack(itemOpt.get(), itemId);
                    }
                }
            }
        }
        return count;
    }

    private static int stackCount(NbtCompound stack) {
        Optional<Byte> byteVal = stack.getByte("count");
        if (byteVal.isPresent()) {
            return byteVal.get();
        }
        Optional<Integer> intVal = stack.getInt("count");
        return intVal.orElse(1);
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
            required = parseContainerItems(file, state, parseExcludeItems(ProjectionFillExcludeRule.survivalAidFillExcludeBlocks));
        } catch (Exception e) {
            source.sendError(Text.literal("解析投影失败: " + e.getMessage()));
            return 0;
        }
        if (required.isEmpty()) {
            source.sendError(Text.literal("投影的容器里没有物品可填充。"));
            return 0;
        }
        List<Inventory> containers = findContainersAround(player, 5);
        if (containers.isEmpty()) {
            source.sendError(Text.literal("周围 5 格内没有容器（箱子/漏斗/潜影盒等）。"));
            return 0;
        }
        Map<Item, Integer> have = new HashMap<>();
        for (Inventory c : containers) {
            for (Map.Entry<Item, Integer> e : containerContents(c).entrySet()) {
                have.merge(e.getKey(), e.getValue(), Integer::sum);
            }
        }
        Map<Item, Integer> need = shortfall(required, have);
        if (need.isEmpty()) {
            source.sendFeedback(() -> Text.literal("投影所需物品已全部就位，无需填充。"), true);
            return 0;
        }
        Map<Item, Integer> missing = new HashMap<>();
        int totalPlaced = fillFromPlayer(player, containers, need, missing);
        final String fileName = file.getFileName().toString();
        final int fTotal = totalPlaced;
        final Map<Item, Integer> finalMissing = missing;
        source.sendFeedback(() -> {
            StringBuilder sb = new StringBuilder("已按投影 " + fileName + " 填充 " + fTotal + " 件物品到周围容器");
            if (finalMissing.isEmpty()) {
                return Text.literal(sb.toString() + "。");
            }
            sb.append("；缺少: ");
            boolean first = true;
            for (Map.Entry<Item, Integer> e : finalMissing.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(Registries.ITEM.getId(e.getKey())).append(' ').append(e.getValue());
            }
            return Text.literal(sb.toString() + "。");
        }, true);
        return totalPlaced;
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
        World world = player.getWorld();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockEntity be = world.getBlockEntity(center.add(dx, dy, dz));
                    if (be instanceof Inventory inv && !result.contains(inv)) {
                        result.add(inv);
                    }
                }
            }
        }
        return result;
    }
    private static Map<Item, Integer> containerContents(Inventory container) {
        Map<Item, Integer> have = new HashMap<>();
        for (int slot = 0; slot < container.size(); slot++) {
            ItemStack stack = container.getStack(slot);
            if (!stack.isEmpty()) {
                have.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return have;
    }

    private static Map<Item, Integer> shortfall(Map<Item, Integer> required, Map<Item, Integer> have) {
        Map<Item, Integer> need = new HashMap<>();
        for (Map.Entry<Item, Integer> e : required.entrySet()) {
            int diff = e.getValue() - have.getOrDefault(e.getKey(), 0);
            if (diff > 0) {
                need.put(e.getKey(), diff);
            }
        }
        return need;
    }
    private static int fillFromPlayer(ServerPlayerEntity player, List<Inventory> containers, Map<Item, Integer> need, Map<Item, Integer> missing) {
        net.minecraft.entity.player.PlayerInventory inv = player.getInventory();
        int totalPlaced = 0;
        for (Map.Entry<Item, Integer> e : need.entrySet()) {
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
                totalPlaced += placed;
                if (placed < removed.getCount()) {
                    ItemStack leftover = removed.copyWithCount(removed.getCount() - placed);
                    inv.offerOrDrop(leftover);
                }
            }
            if (taken < needed) {
                missing.merge(item, needed - taken, Integer::sum);
            }
        }
        return totalPlaced;
    }
    private static Set<String> parseExcludeItems(String cfg) {
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

    private static Item itemFromId(String id) {
        Identifier ident = id.indexOf(':') >= 0 ? Identifier.tryParse(id) : Identifier.tryParse("minecraft:" + id);
        if (ident == null) {
            return null;
        }
        return Registries.ITEM.get(ident);
    }
    private static Map<Item, Integer> parseContainerItems(Path file, ProjectionClientState state, Set<String> excludeItems) throws IOException {
        NbtCompound root = NbtIo.readCompressed(file, NbtSizeTracker.of(1000000000L));
        Optional<NbtCompound> regionsOpt = root.getCompound("Regions");
        if (regionsOpt.isEmpty()) {
            return new HashMap<>();
        }
        NbtCompound regions = regionsOpt.get();
        Map<Item, Integer> result = new HashMap<>();
        for (String regionName : regions.getKeys()) {
            Optional<NbtCompound> regionOpt = regions.getCompound(regionName);
            if (regionOpt.isEmpty()) {
                continue;
            }
            NbtCompound region = regionOpt.get();
            Optional<NbtList> tilesOpt = region.getList("TileEntities");
            if (tilesOpt.isEmpty()) {
                continue;
            }
            NbtList tiles = tilesOpt.get();
            for (int i = 0; i < tiles.size(); i++) {
                Optional<NbtCompound> teOpt = tiles.getCompound(i);
                if (teOpt.isEmpty()) {
                    continue;
                }
                NbtCompound te = teOpt.get();
                int y = te.getInt("y").orElse(0);
                if (state.isLayerFiltered()) {
                    int worldY = state.originY + y;
                    if (worldY < state.minY || worldY > state.maxY) {
                        continue;
                    }
                }
                Optional<NbtList> itemsOpt = te.getList("Items");
                if (itemsOpt.isEmpty()) {
                    continue;
                }
                NbtList items = itemsOpt.get();
                for (int j = 0; j < items.size(); j++) {
                    Optional<NbtCompound> itOpt = items.getCompound(j);
                    if (itOpt.isEmpty()) {
                        continue;
                    }
                    NbtCompound it = itOpt.get();
                    String id = it.getString("id").orElse("");
                    if (id.isEmpty() || excludeItems.contains(id)) {
                        continue;
                    }
                    int count = it.getByte("Count").orElse((byte) 0);
                    Item item = itemFromId(id);
                    if (item == null || item == Items.AIR) {
                        continue;
                    }
                    result.merge(item, count, Integer::sum);
                }
            }
        }
        return result;
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
