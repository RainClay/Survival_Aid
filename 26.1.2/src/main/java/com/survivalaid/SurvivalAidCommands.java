package com.survivalaid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import carpet.patches.EntityPlayerMPFake;
import com.survivalaid.features.FakePlayerItemSearchRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.ProjectionFillExcludeRule;
import com.survivalaid.features.ProjectionFillRule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
        .then(Commands.literal("searchitem").then(Commands.argument("item", StringArgumentType.greedyString()).executes(context6 -> {
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

    private static Identifier resolveItemByName(MinecraftServer server, String name) {
        return chineseItemNameMap(server).get(name);
    }

    private static Map<String, Identifier> chineseItemNameCache = new HashMap<>();
    private static boolean chineseItemNameLoaded = false;

    private static Map<String, Identifier> chineseItemNameMap(MinecraftServer server) {
        if (!chineseItemNameLoaded) {
            chineseItemNameCache = buildChineseItemNameMap(server);
            chineseItemNameLoaded = true;
        }
        return chineseItemNameCache;
    }

    private static Map<String, Identifier> buildChineseItemNameMap(MinecraftServer server) {
        Map<String, Identifier> result = new HashMap<>();
        try {
            Optional<Resource> res = server.getResourceManager().getResource(Identifier.fromNamespaceAndPath("minecraft", "lang/zh_cn.json"));
            if (res.isPresent()) {
                JsonObject root;
                try (InputStream is = res.get().open();
                     Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    root = JsonParser.parseReader(r).getAsJsonObject();
                }
                for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                    String key = entry.getKey();
                    if (!key.startsWith("item.") && !key.startsWith("block.")) {
                        continue;
                    }
                    int first = key.indexOf('.');
                    int second = key.indexOf('.', first + 1);
                    if (second < 0) {
                        continue;
                    }
                    Identifier id = Identifier.tryParse(key.substring(first + 1, second) + ":" + key.substring(second + 1));
                    if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                        result.putIfAbsent(entry.getValue().getAsString(), id);
                    }
                }
            }
        } catch (Exception e) {
            // 语言文件解析失败则退回仅按物品 ID 搜索
        }
        return result;
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
            itemId = resolveItemByName(source.getServer(), normalizedItem);
        }
        if (itemId == null) {
            source.sendFailure(Component.literal("无效的物品 ID 或中文名: " + normalizedItem));
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
        ProjectionClientState state = ProjectionSyncStore.get(player.getUUID());
        if (state == null || state.name == null || state.name.isEmpty()) {
            source.sendFailure(Component.literal("未检测到当前投影，请在客户端 Litematica 中选中一个投影后重试。"));
            return 0;
        }
        List<Path> matches;
        try {
            matches = findSchematicFiles(dir, state.name);
        } catch (IOException e) {
            source.sendFailure(Component.literal("读取 schematics/ 目录失败。"));
            return 0;
        }
        if (matches.isEmpty()) {
            source.sendFailure(Component.literal("服务器 schematics/ 下找不到投影文件: " + state.name + "（已含子目录搜索）"));
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
        List<ProjectionContainerTarget> targets;
        try {
            targets = parseContainerTargets(file, state, parseExcludeItems(ProjectionFillExcludeRule.survivalAidFillExcludeBlocks));
        } catch (Exception e) {
            source.sendFailure(Component.literal("解析投影失败: " + e.getMessage()));
            return 0;
        }
        if (targets.isEmpty()) {
            source.sendFailure(Component.literal("投影的容器里没有物品可填充。"));
            return 0;
        }
        Map<Item, Integer> missing = new HashMap<>();
        List<String> unmatched = new ArrayList<>();
        int filledContainers = 0;
        for (ProjectionContainerTarget t : targets) {
            Container real = findContainerAt(player.level(), t.worldPos);
            if (real == null) {
                unmatched.add("(" + t.worldPos.getX() + "," + t.worldPos.getY() + "," + t.worldPos.getZ() + ")");
                continue;
            }
            if (fillContainerSlots(real, player, t.slots, missing) > 0) {
                filledContainers++;
            }
        }
        final String fileName = file.getFileName().toString();
        final int fCount = filledContainers;
        final List<String> fUnmatched = unmatched;
        final Map<Item, Integer> finalMissing = missing;
        source.sendSuccess(() -> {
            StringBuilder sb = new StringBuilder("已按投影 " + fileName + " 填充 " + fCount + " 个容器");
            if (!fUnmatched.isEmpty()) {
                sb.append("；未匹配到真实容器的投影坐标: ").append(String.join(", ", fUnmatched));
            }
            if (finalMissing.isEmpty()) {
                return Component.literal(sb.toString() + "。");
            }
            sb.append("；缺少: ");
            boolean first = true;
            for (Map.Entry<Item, Integer> e : finalMissing.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(BuiltInRegistries.ITEM.getKey(e.getKey())).append(' ').append(e.getValue());
            }
            return Component.literal(sb.toString() + "。");
        }, true);
        return filledContainers;
    }

    private static List<Path> findSchematicFiles(Path root, String name) throws IOException {
        List<Path> result = new ArrayList<>();
        String target = name.endsWith(".litematic") ? name : name + ".litematic";
        try (var stream = Files.walk(root)) {
            stream.filter(p -> Files.isRegularFile(p)).filter(p -> p.getFileName().toString().equalsIgnoreCase(target)).forEach(result::add);
        }
        return result;
    }

    private static Container findContainerAt(Level world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Container c) {
            return c;
        }
        return null;
    }

    private static int fillContainerSlots(Container container, ServerPlayer player, Map<Integer, ProjectionSlot> slots, Map<Item, Integer> missing) {
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        int filledSlots = 0;
        for (Map.Entry<Integer, ProjectionSlot> e : slots.entrySet()) {
            int slot = e.getKey();
            ProjectionSlot ps = e.getValue();
            ItemStack cur = container.getItem(slot);
            if (!cur.isEmpty() && cur.is(ps.item) && cur.getCount() >= ps.count) {
                continue;
            }
            int already = cur.is(ps.item) ? cur.getCount() : 0;
            if (already == 0 && !cur.isEmpty()) {
                inv.placeItemBackInInventory(cur);
            }
            int need = ps.count - already;
            int taken = takeFromPlayer(player, ps.item, need);
            ItemStack set = new ItemStack(ps.item, already + taken);
            container.setItem(slot, set);
            filledSlots++;
            if (taken < need) {
                missing.merge(ps.item, need - taken, Integer::sum);
            }
        }
        return filledSlots;
    }

    private static int takeFromPlayer(ServerPlayer player, Item item, int need) {
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        int taken = 0;
        for (int slot = 0; slot < inv.getContainerSize() && taken < need; slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.isEmpty() || !stack.is(item)) {
                continue;
            }
            int toTake = Math.min(stack.getCount(), need - taken);
            ItemStack removed = inv.removeItem(slot, toTake);
            if (!removed.isEmpty()) {
                taken += removed.getCount();
            }
        }
        return taken;
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
        return BuiltInRegistries.ITEM.get(ident).map(h -> h.value()).orElse(null);
    }

    private static List<ProjectionContainerTarget> parseContainerTargets(Path file, ProjectionClientState state, Set<String> excludeItems) throws IOException {
        CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        Optional<CompoundTag> regionsOpt = root.getCompound("Regions");
        if (regionsOpt.isEmpty()) {
            return new ArrayList<>();
        }
        CompoundTag regions = regionsOpt.get();
        List<ProjectionContainerTarget> result = new ArrayList<>();
        for (String regionName : regions.keySet()) {
            Optional<CompoundTag> regionOpt = regions.getCompound(regionName);
            if (regionOpt.isEmpty()) {
                continue;
            }
            CompoundTag region = regionOpt.get();
            Optional<ListTag> tilesOpt = region.getList("TileEntities");
            if (tilesOpt.isEmpty()) {
                continue;
            }
            ListTag tiles = tilesOpt.get();
            for (int i = 0; i < tiles.size(); i++) {
                Optional<CompoundTag> teOpt = tiles.getCompound(i);
                if (teOpt.isEmpty()) {
                    continue;
                }
                CompoundTag te = teOpt.get();
                int x = te.getInt("x").orElse(0);
                int y = te.getInt("y").orElse(0);
                int z = te.getInt("z").orElse(0);
                if (state.isLayerFiltered()) {
                    int worldY = state.originY + y;
                    if (worldY < state.minY || worldY > state.maxY) {
                        continue;
                    }
                }
                BlockPos worldPos = new BlockPos(state.originX + x, state.originY + y, state.originZ + z);
                Map<Integer, ProjectionSlot> slots = new HashMap<>();
                Optional<ListTag> itemsOpt = te.getList("Items");
                if (itemsOpt.isPresent()) {
                    ListTag items = itemsOpt.get();
                    for (int j = 0; j < items.size(); j++) {
                        Optional<CompoundTag> itOpt = items.getCompound(j);
                        if (itOpt.isEmpty()) {
                            continue;
                        }
                        CompoundTag it = itOpt.get();
                        String id = it.getString("id").orElse("");
                        if (id.isEmpty() || excludeItems.contains(id)) {
                            continue;
                        }
                        int slot = it.getByte("Slot").orElse((byte) 0);
                        int count = it.getByte("Count").orElse((byte) 0);
                        Item item = itemFromId(id);
                        if (item == null || item == Items.AIR) {
                            continue;
                        }
                        slots.put(slot, new ProjectionSlot(item, count));
                    }
                }
                if (!slots.isEmpty()) {
                    result.add(new ProjectionContainerTarget(worldPos, slots));
                }
            }
        }
        return result;
    }
    private static final class ProjectionSlot {
        final Item item;
        final int count;
        ProjectionSlot(Item item, int count) {
            this.item = item;
            this.count = count;
        }
    }

    private static final class ProjectionContainerTarget {
        final BlockPos worldPos;
        final Map<Integer, ProjectionSlot> slots;
        ProjectionContainerTarget(BlockPos worldPos, Map<Integer, ProjectionSlot> slots) {
            this.worldPos = worldPos;
            this.slots = slots;
        }
    }
}
