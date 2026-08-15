package com.survivalaid;

import com.survivalaid.features.AutoEatFoodRule;
import com.survivalaid.features.AutoEatFoodThresholdRule;
import com.survivalaid.features.DeathCoordinateMessageRule;
import com.survivalaid.features.LowDurabilityWarningRule;
import com.survivalaid.features.LowHealthGlowRule;
import com.survivalaid.features.LowHealthGlowThresholdRule;
import com.survivalaid.features.VoidPlayerRescueCooldownRule;
import com.survivalaid.features.VoidPlayerRescueRule;
import com.survivalaid.features.VoidPlayerRescueYRule;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class SurvivalAidRuntime {
    private static final Map<UUID, Boolean> LAST_DEAD_STATE = new HashMap<>();
    private static final Map<UUID, Integer> LAST_DURABILITY_WARNING_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_DEBUG_TICK = new HashMap<>();
    private static final Map<UUID, Integer> LAST_VOID_RESCUE_TICK = new HashMap<>();
    private static final Map<Entity, Integer> WORKSTATION_HIGHLIGHTS = new HashMap<>();

    private SurvivalAidRuntime() {
    }

    public static void tick(MinecraftServer server) {
        SurvivalAidTntLikeBlocks.tick(server);
        tickWorkstationHighlights();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handleDeathCoordinateMessage(player);
            handleLowHealthGlow(player);
            handleLowDurabilityWarning(server, player);
            handleAutoEatFood(server, player);
            handleVoidPlayerRescue(server, player);
        }
    }

    private static void handleVoidPlayerRescue(MinecraftServer server, ServerPlayerEntity player) {
        if (!VoidPlayerRescueRule.survivalAidVoidPlayerRescue || player.isCreative() || player.isSpectator() || player.isDead() || player.getY() > VoidPlayerRescueYRule.survivalAidVoidPlayerRescueY) {
            return;
        }
        UUID playerId = player.getUuid();
        int currentTick = server.getTicks();
        int lastRescueTick = LAST_VOID_RESCUE_TICK.getOrDefault(playerId, -200);
        if (lastRescueTick > currentTick) {
            lastRescueTick = -200;
        }
        int cooldown = Math.max(1, VoidPlayerRescueCooldownRule.survivalAidVoidPlayerRescueCooldown);
        if (currentTick - lastRescueTick < cooldown) {
            return;
        }
        boolean equippedElytra = ensureElytraEquipped(player);
        if (!equippedElytra && !player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)) {
            LAST_VOID_RESCUE_TICK.put(playerId, currentTick);
            player.sendMessage(Text.literal("虚空救援：背包中没有可用鞘翅。"), true);
            return;
        }
        int rocketSlot = findItemSlot(player, Items.FIREWORK_ROCKET);
        if (rocketSlot < 0) {
            LAST_VOID_RESCUE_TICK.put(playerId, currentTick);
            player.sendMessage(Text.literal("虚空救援：背包中没有火箭烟花。"), true);
            return;
        }
        ItemStack rocketStack = player.getInventory().getStack(rocketSlot);
        ItemStack rocketForUse = rocketStack.split(1);
        FireworkRocketEntity firework = new FireworkRocketEntity(player.getEntityWorld(), rocketForUse, player);
        player.getEntityWorld().spawnEntity(firework);
        player.addVelocity(0.0d, 0.6d, 0.0d);
        player.startGliding();
        LAST_VOID_RESCUE_TICK.put(playerId, currentTick);
        player.sendMessage(Text.literal("虚空救援：已自动" + (equippedElytra ? "穿上鞘翅并" : "") + "使用火箭烟花。"), true);
    }

    private static boolean ensureElytraEquipped(ServerPlayerEntity player) {
        int elytraSlot;
        ItemStack chestStack = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestStack.isOf(Items.ELYTRA) || (elytraSlot = findItemSlot(player, Items.ELYTRA)) < 0) {
            return false;
        }
        ItemStack elytra = player.getInventory().getStack(elytraSlot);
        player.getInventory().setStack(5 + EquipmentSlot.CHEST.ordinal(), elytra.copy());
        player.getInventory().setStack(elytraSlot, chestStack);
        return true;
    }

    private static int findItemSlot(ServerPlayerEntity player, Item item) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty() && stack.isOf(item)) {
                return slot;
            }
        }
        return -1;
    }

    public static void trackWorkstationHighlight(Entity entity) {
        WORKSTATION_HIGHLIGHTS.put(entity, 40);
    }

    private static void tickWorkstationHighlights() {
        WORKSTATION_HIGHLIGHTS.entrySet().removeIf(entry -> {
            int remaining = entry.getValue() - 1;
            Entity entity = entry.getKey();
            if (!entity.isAlive() || remaining <= 0) {
                if (entity.isAlive()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return true;
                }
                return true;
            }
            entry.setValue(remaining);
            return false;
        });
    }

    private static void handleDeathCoordinateMessage(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        boolean isDead = player.isDead();
        boolean wasDead = LAST_DEAD_STATE.getOrDefault(playerId, false);
        if (DeathCoordinateMessageRule.survivalAidDeathCoordinateMessage && isDead && !wasDead) {
            BlockPos pos = player.getBlockPos();
            String dimension = player.getEntityWorld().getRegistryKey().getValue().toString();
            player.sendMessage(Text.literal("死亡位置：" + dimension + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
        }
        LAST_DEAD_STATE.put(playerId, isDead);
    }

    private static void handleLowHealthGlow(ServerPlayerEntity player) {
        if (!LowHealthGlowRule.survivalAidLowHealthGlow) {
            if (player.isGlowing()) {
                player.setGlowing(false);
            }
        } else {
            float threshold = Math.max(1.0f, LowHealthGlowThresholdRule.survivalAidLowHealthGlowThreshold);
            boolean shouldGlow = !player.isDead() && player.getHealth() <= threshold;
            if (player.isGlowing() != shouldGlow) {
                player.setGlowing(shouldGlow);
            }
        }
    }

    private static void handleLowDurabilityWarning(MinecraftServer server, ServerPlayerEntity player) {
        int remainingDurability;
        if (LowDurabilityWarningRule.survivalAidLowDurabilityWarning <= 0) {
            return;
        }
        ItemStack stack = player.getMainHandStack();
        if (stack.isEmpty() || !stack.isDamageable() || (remainingDurability = stack.getMaxDamage() - stack.getDamage()) > LowDurabilityWarningRule.survivalAidLowDurabilityWarning) {
            return;
        }
        UUID playerId = player.getUuid();
        int currentTick = server.getTicks();
        int lastWarningTick = LAST_DURABILITY_WARNING_TICK.getOrDefault(playerId, -200);
        if (currentTick - lastWarningTick < 200) {
            return;
        }
        LAST_DURABILITY_WARNING_TICK.put(playerId, currentTick);
        player.sendMessage(Text.literal("耐久不足：" + stack.getName().getString() + " 剩余 " + remainingDurability + " 点耐久。"), true);
    }

    private static void handleAutoEatFood(MinecraftServer server, ServerPlayerEntity player) {
        if (!AutoEatFoodRule.survivalAidAutoEatFood || player.isSpectator() || player.isDead()) {
            debugAutoEat(server, player, "跳过：规则=" + AutoEatFoodRule.survivalAidAutoEatFood + "，创造=" + player.isCreative() + "，旁观=" + player.isSpectator() + "，死亡=" + player.isDead());
            return;
        }
        UUID playerId = player.getUuid();
        int currentTick = server.getTicks();
        int lastAutoEatTick = LAST_AUTO_EAT_TICK.getOrDefault(playerId, -40);
        if (lastAutoEatTick > currentTick) {
            lastAutoEatTick = -40;
        }
        if (currentTick - lastAutoEatTick < 40) {
            debugAutoEat(server, player, "跳过：自动进食冷却中，剩余 " + (40 - (currentTick - lastAutoEatTick)) + " tick");
            return;
        }
        int threshold = Math.max(0, Math.min(19, AutoEatFoodThresholdRule.survivalAidAutoEatFoodThreshold));
        int currentFoodLevel = player.getHungerManager().getFoodLevel();
        int foodStackCount = 0;
        int skippedEnchantedGoldenAppleCount = 0;
        if (currentFoodLevel >= 20) {
            return;
        }
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
                    skippedEnchantedGoldenAppleCount++;
                } else {
                    FoodComponent foodComponent = stack.getComponents().get(DataComponentTypes.FOOD);
                    if (foodComponent == null) {
                        continue;
                    }
                    foodStackCount++;
                    if (currentFoodLevel <= threshold) {
                        player.getHungerManager().eat(foodComponent);
                        LAST_AUTO_EAT_TICK.put(playerId, currentTick);
                        stack.decrement(1);
                        player.sendMessage(Text.literal("自动进食：已吃 " + stack.getName().getString() + "。"), true);
                        return;
                    }
                }
            }
        }
        debugAutoEat(server, player, "未进食：可用食物组=" + foodStackCount + "，跳过附魔金苹果组=" + skippedEnchantedGoldenAppleCount + "，饥饿值=" + player.getHungerManager().getFoodLevel());
    }

    private static void debugAutoEat(MinecraftServer server, ServerPlayerEntity player, String message) {
        if (!AutoEatFoodRule.survivalAidAutoEatFoodDebug) {
            return;
        }
        UUID playerId = player.getUuid();
        int currentTick = server.getTicks();
        int lastDebugTick = LAST_AUTO_EAT_DEBUG_TICK.getOrDefault(playerId, -20);
        if (currentTick - lastDebugTick < 20) {
            return;
        }
        LAST_AUTO_EAT_DEBUG_TICK.put(playerId, currentTick);
        player.sendMessage(Text.literal("自动进食调试：" + message), false);
    }
}
