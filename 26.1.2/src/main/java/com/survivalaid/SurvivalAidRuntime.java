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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidRuntime.class */
public final class SurvivalAidRuntime {
    private static final Map<UUID, Boolean> LAST_DEAD_STATE = new HashMap();
    private static final Map<UUID, Integer> LAST_DURABILITY_WARNING_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_DEBUG_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_VOID_RESCUE_TICK = new HashMap();
    private static final Map<Entity, Integer> WORKSTATION_HIGHLIGHTS = new HashMap();

    private SurvivalAidRuntime() {
    }

    public static void tick(MinecraftServer server) {
        SurvivalAidTntLikeBlocks.tick(server);
        tickWorkstationHighlights();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            handleDeathCoordinateMessage(player);
            handleLowHealthGlow(player);
            handleLowDurabilityWarning(server, player);
            handleAutoEatFood(server, player);
            handleVoidPlayerRescue(server, player);
        }
    }

    private static void handleVoidPlayerRescue(MinecraftServer server, ServerPlayer player) {
        if (!VoidPlayerRescueRule.survivalAidVoidPlayerRescue || player.isCreative() || player.isSpectator() || player.isDeadOrDying() || player.getY() > VoidPlayerRescueYRule.survivalAidVoidPlayerRescueY) {
            return;
        }
        UUID playerId = player.getUUID();
        int currentTick = server.getTickCount();
        int lastRescueTick = LAST_VOID_RESCUE_TICK.getOrDefault(playerId, -200).intValue();
        if (lastRescueTick > currentTick) {
            lastRescueTick = -200;
        }
        int cooldown = Math.max(1, VoidPlayerRescueCooldownRule.survivalAidVoidPlayerRescueCooldown);
        if (currentTick - lastRescueTick < cooldown) {
            return;
        }
        boolean equippedElytra = ensureElytraEquipped(player);
        if (!equippedElytra && !player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
            player.sendSystemMessage(Component.translatable("survival_aid.message.void_rescue_no_elytra"), true);
            return;
        }
        int rocketSlot = findItemSlot(player, Items.FIREWORK_ROCKET);
        if (rocketSlot < 0) {
            LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
            player.sendSystemMessage(Component.translatable("survival_aid.message.void_rescue_no_rocket"), true);
            return;
        }
        ItemStack rocket = player.getInventory().getItem(rocketSlot);
        ItemStack rocketForUse = rocket.copyWithCount(1);
        rocket.shrink(1);
        FireworkRocketEntity firework = new FireworkRocketEntity(player.level(), rocketForUse, player);
        player.level().addFreshEntity(firework);
        player.push(0.0d, 0.6d, 0.0d);
        LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
        player.sendSystemMessage(Component.translatable("survival_aid.message.void_rescue_used", equippedElytra ? Component.translatable("survival_aid.message.void_rescue_equipped_elytra") : ""), true);
    }

    private static boolean ensureElytraEquipped(ServerPlayer player) {
        int elytraSlot;
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.is(Items.ELYTRA) || (elytraSlot = findItemSlot(player, Items.ELYTRA)) < 0) {
            return false;
        }
        ItemStack elytra = player.getInventory().getItem(elytraSlot);
        player.setItemSlot(EquipmentSlot.CHEST, elytra.copy());
        player.getInventory().setItem(elytraSlot, chestStack);
        return true;
    }

    private static int findItemSlot(ServerPlayer player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item)) {
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
            int remaining = ((Integer) entry.getValue()).intValue() - 1;
            Entity entity = (Entity) entry.getKey();
            if (!entity.isAlive() || remaining <= 0) {
                if (entity.isAlive()) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                    return true;
                }
                return true;
            }
            entry.setValue(Integer.valueOf(remaining));
            return false;
        });
    }

    private static void handleDeathCoordinateMessage(ServerPlayer player) {
        UUID playerId = player.getUUID();
        boolean isDead = player.isDeadOrDying();
        boolean wasDead = LAST_DEAD_STATE.getOrDefault(playerId, false).booleanValue();
        if (DeathCoordinateMessageRule.survivalAidDeathCoordinateMessage && isDead && !wasDead) {
            BlockPos pos = player.blockPosition();
            String dimension = player.level().dimension().identifier().toString();
            player.sendSystemMessage(Component.translatable("survival_aid.message.death_position", dimension, pos.getX(), pos.getY(), pos.getZ()), false);
        }
        LAST_DEAD_STATE.put(playerId, Boolean.valueOf(isDead));
    }

    private static void handleLowHealthGlow(ServerPlayer player) {
        if (!LowHealthGlowRule.survivalAidLowHealthGlow) {
            if (player.isCurrentlyGlowing()) {
                player.setGlowingTag(false);
            }
        } else {
            float threshold = Math.max(1.0f, LowHealthGlowThresholdRule.survivalAidLowHealthGlowThreshold);
            boolean shouldGlow = !player.isDeadOrDying() && player.getHealth() <= threshold;
            if (player.isCurrentlyGlowing() != shouldGlow) {
                player.setGlowingTag(shouldGlow);
            }
        }
    }

    private static void handleLowDurabilityWarning(MinecraftServer server, ServerPlayer player) {
        int remainingDurability;
        if (LowDurabilityWarningRule.survivalAidLowDurabilityWarning <= 0) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !stack.isDamageableItem() || (remainingDurability = stack.getMaxDamage() - stack.getDamageValue()) > LowDurabilityWarningRule.survivalAidLowDurabilityWarning) {
            return;
        }
        UUID playerId = player.getUUID();
        int currentTick = server.getTickCount();
        int lastWarningTick = LAST_DURABILITY_WARNING_TICK.getOrDefault(playerId, -200).intValue();
        if (currentTick - lastWarningTick < 200) {
            return;
        }
        LAST_DURABILITY_WARNING_TICK.put(playerId, Integer.valueOf(currentTick));
        player.sendSystemMessage(Component.translatable("survival_aid.message.low_durability", stack.getItemName().getString(), remainingDurability), true);
    }

    private static void handleAutoEatFood(MinecraftServer server, ServerPlayer player) {
        Consumable consumable;
        if (!AutoEatFoodRule.survivalAidAutoEatFood || player.isCreative() || player.isSpectator() || player.isDeadOrDying()) {
            debugAutoEat(server, player, Component.translatable("survival_aid.message.auto_eat_skip_rule", AutoEatFoodRule.survivalAidAutoEatFood, player.isCreative(), player.isSpectator(), player.isDeadOrDying()));
            return;
        }
        UUID playerId = player.getUUID();
        int currentTick = server.getTickCount();
        int lastAutoEatTick = LAST_AUTO_EAT_TICK.getOrDefault(playerId, -40).intValue();
        if (currentTick - lastAutoEatTick < 40) {
            debugAutoEat(server, player, Component.translatable("survival_aid.message.auto_eat_skip_cooldown", 40 - (currentTick - lastAutoEatTick)));
            return;
        }
        int threshold = Math.max(0, Math.min(19, AutoEatFoodThresholdRule.survivalAidAutoEatFoodThreshold));
        int currentFoodLevel = player.getFoodData().getFoodLevel();
        int foodStackCount = 0;
        int skippedEnchantedGoldenAppleCount = 0;
        if (currentFoodLevel >= 20) {
            return;
        }
        debugAutoEat(server, player, Component.translatable("survival_aid.message.auto_eat_checking", currentFoodLevel, threshold));
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    skippedEnchantedGoldenAppleCount++;
                } else {
                    FoodProperties food = (FoodProperties) stack.get(DataComponents.FOOD);
                    if (food != null && (consumable = (Consumable) stack.get(DataComponents.CONSUMABLE)) != null) {
                        boolean hasNonFoodEffect = false;
                        Iterator it = consumable.onConsumeEffects().iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            ConsumeEffect effect = (ConsumeEffect) it.next();
                            if (!(effect instanceof ApplyStatusEffectsConsumeEffect)) {
                                hasNonFoodEffect = true;
                                break;
                            }
                        }
                        if (hasNonFoodEffect) {
                            continue;
                        } else {
                            foodStackCount++;
                            if (currentFoodLevel > threshold) {
                                debugAutoEat(server, player, Component.translatable("survival_aid.message.auto_eat_skip_above_threshold", currentFoodLevel, threshold));
                            } else {
                                player.getFoodData().eat(food);
                                List<MobEffectInstance> foodEffects = new ArrayList<>();
                                for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
                                    if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect) {
                                        ApplyStatusEffectsConsumeEffect applyEffect = (ApplyStatusEffectsConsumeEffect) consumeEffect;
                                        if (applyEffect.probability() >= 1.0f || player.getRandom().nextFloat() < applyEffect.probability()) {
                                            for (MobEffectInstance effect2 : applyEffect.effects()) {
                                                foodEffects.add(effect2);
                                            }
                                        }
                                    }
                                }
                                for (MobEffectInstance effect3 : foodEffects) {
                                    player.addEffect(new MobEffectInstance(effect3));
                                }
                                stack.consume(1, player);
                                LAST_AUTO_EAT_TICK.put(playerId, Integer.valueOf(currentTick));
                                player.sendSystemMessage(Component.translatable("survival_aid.message.auto_eat", stack.getItemName().getString()), true);
                                return;
                            }
                        }
                    }
                }
            }
        }
        debugAutoEat(server, player, Component.translatable("survival_aid.message.auto_eat_no_food", foodStackCount, skippedEnchantedGoldenAppleCount, player.getFoodData().getFoodLevel()));
    }

    private static void debugAutoEat(MinecraftServer server, ServerPlayer player, Component message) {
        if (!AutoEatFoodRule.survivalAidAutoEatFoodDebug) {
            return;
        }
        UUID playerId = player.getUUID();
        int currentTick = server.getTickCount();
        int lastDebugTick = LAST_AUTO_EAT_DEBUG_TICK.getOrDefault(playerId, -20).intValue();
        if (currentTick - lastDebugTick < 20) {
            return;
        }
        LAST_AUTO_EAT_DEBUG_TICK.put(playerId, Integer.valueOf(currentTick));
        player.sendSystemMessage(Component.translatable("survival_aid.message.auto_eat_debug", message), false);
    }
}
