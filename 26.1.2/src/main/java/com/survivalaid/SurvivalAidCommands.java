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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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
            source.sendFailure(Component.translatable("survival_aid.cmd.pickup_usage"));
            return 0;
        }
        List<String> entries = entries();
        String newEntry = normalizedItem + "=" + normalizedPlayer;
        for (String entry : entries) {
            String[] parsed = parseEntry(entry);
            if (parsed != null && parsed[0].equalsIgnoreCase(normalizedItem) && parsed[1].equalsIgnoreCase(normalizedPlayer)) {
                source.sendSuccess(() -> {
                    return Component.translatable("survival_aid.cmd.pickup_already_allowed", normalizedItem, normalizedPlayer);
                }, false);
                return 0;
            }
        }
        entries.add(newEntry);
        save(entries);
        source.sendSuccess(() -> {
            return Component.translatable("survival_aid.cmd.pickup_allowed", normalizedItem, normalizedPlayer, ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist);
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
            return removed ? Component.translatable("survival_aid.cmd.pickup_removed", normalizedItem, normalizedPlayer) : Component.translatable("survival_aid.cmd.pickup_rule_missing", normalizedItem, normalizedPlayer);
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
            return removed ? Component.translatable("survival_aid.cmd.pickup_item_cleared", normalizedItem) : Component.translatable("survival_aid.cmd.pickup_item_none", normalizedItem);
        }, true);
        return removed ? 1 : 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int clearAll(CommandSourceStack source) {
        ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist = "";
        source.sendSuccess(() -> {
            return Component.translatable("survival_aid.cmd.pickup_all_cleared");
        }, true);
        return 1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static int list(CommandSourceStack source) {
        String value = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist.trim();
        if (value.isEmpty() || value.equalsIgnoreCase("none")) {
            source.sendSuccess(() -> {
                return Component.translatable("survival_aid.cmd.pickup_none");
            }, false);
            return 0;
        }
        source.sendSuccess(() -> {
            return Component.translatable("survival_aid.cmd.pickup_list", value);
        }, false);
        return entries().size();
    }

    private static Identifier tryParseItemId(String value) {
        Identifier id = Identifier.tryParse(value);
        if (id == null) {
            id = Identifier.tryParse("minecraft:" + value);
        }
        return id;
    }

    private static int searchItem(CommandSourceStack source, String itemName) {
        if (!FakePlayerItemSearchRule.survivalAidFakePlayerItemSearch) {
            source.sendFailure(Component.translatable("survival_aid.cmd.search_rule_off"));
            return 0;
        }
        String normalizedItem = normalizeToken(itemName);
        Identifier itemId = tryParseItemId(normalizedItem);
        if (itemId == null) {
            ServerPlayer player = source.getPlayer();
            if (player != null && ServerPlayNetworking.canSend(player, SearchItemRequestPayload.TYPE)) {
                ServerPlayNetworking.send(player, new SearchItemRequestPayload(normalizedItem));
                source.sendSuccess(() -> {
                    return Component.translatable("survival_aid.cmd.search_resolving", normalizedItem);
                }, false);
                return 1;
            }
            source.sendFailure(Component.translatable("survival_aid.cmd.search_invalid_item", normalizedItem));
            return 0;
        }
        return performSearch(source, itemId);
    }

    public static void handleSearchResult(ServerPlayer player, String itemId) {
        CommandSourceStack source = player.createCommandSourceStack();
        MinecraftServer server = source.getServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            if (itemId == null || itemId.isEmpty()) {
                source.sendFailure(Component.translatable("survival_aid.cmd.search_unresolved"));
                return;
            }
            Identifier id = tryParseItemId(itemId);
            if (id == null) {
                source.sendFailure(Component.translatable("survival_aid.cmd.search_unresolved_named", itemId));
                return;
            }
            performSearch(source, id);
        });
    }

    private static int performSearch(CommandSourceStack source, Identifier itemId) {
        if (!FakePlayerItemSearchRule.survivalAidFakePlayerItemSearch) {
            source.sendFailure(Component.translatable("survival_aid.cmd.search_rule_off"));
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId).map(h -> h.value()).orElse(null);
        if (item == null) {
            source.sendFailure(Component.translatable("survival_aid.cmd.search_item_not_found", itemId));
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
                return Component.translatable("survival_aid.cmd.search_none_carrying", finalItemId);
            }, false);
            return 0;
        }
        source.sendSuccess(() -> {
            return Component.translatable("survival_aid.cmd.search_found", finalItemId, String.join(", ", found));
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
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_rule_off"));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_player_only"));
            return 0;
        }
        Path dir = source.getServer().getServerDirectory().resolve("schematics");
        if (!Files.isDirectory(dir)) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_schematics_dir_missing"));
            return 0;
        }
        ProjectionClientState state = ProjectionSyncStore.get(player.getUUID());
        if (state == null || state.name == null || state.name.isEmpty()) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_no_projection"));
            return 0;
        }
        List<Path> matches;
        try {
            matches = findSchematicFiles(dir, state.name);
        } catch (IOException e) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_read_failed"));
            return 0;
        }
        if (matches.isEmpty()) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_projection_not_found", state.name));
            return 0;
        }
        if (matches.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (Path p : matches) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(dir.relativize(p));
            }
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_multiple_projections", sb.toString()));
            return 0;
        }
        Path file = matches.get(0);
        List<ProjectionContainerTarget> targets;
        try {
            targets = parseContainerTargets(file, state, parseExcludeItems(ProjectionFillExcludeRule.survivalAidFillExcludeBlocks));
        } catch (Exception e) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_parse_failed", e.getMessage()));
            return 0;
        }
        if (targets.isEmpty()) {
            source.sendFailure(Component.translatable("survival_aid.cmd.fill_no_items"));
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
        MutableComponent reportBuilder = Component.translatable("survival_aid.cmd.fill_done", fileName, fCount);
        if (!fUnmatched.isEmpty()) {
            reportBuilder = reportBuilder.append(Component.translatable("survival_aid.cmd.fill_done_unmatched", String.join(", ", fUnmatched)));
        }
        if (!finalMissing.isEmpty()) {
            StringBuilder miss = new StringBuilder();
            boolean first = true;
            for (Map.Entry<Item, Integer> e : finalMissing.entrySet()) {
                if (!first) {
                    miss.append(", ");
                }
                first = false;
                miss.append(BuiltInRegistries.ITEM.getKey(e.getKey())).append(' ').append(e.getValue());
            }
            reportBuilder = reportBuilder.append(Component.translatable("survival_aid.cmd.fill_done_missing", miss.toString()));
        }
        final Component report = reportBuilder;
        source.sendSuccess(() -> report, true);
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
