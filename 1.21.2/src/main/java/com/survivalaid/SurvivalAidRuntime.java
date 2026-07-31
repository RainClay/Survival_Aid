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
import net.minecraft.class_10124;
import net.minecraft.class_10132;
import net.minecraft.class_10134;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1671;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_4174;
import net.minecraft.class_9334;
import net.minecraft.server.MinecraftServer;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.2-1.0.1.jar:com/survivalaid/SurvivalAidRuntime.class */
public final class SurvivalAidRuntime {
    private static final Map<UUID, Boolean> LAST_DEAD_STATE = new HashMap();
    private static final Map<UUID, Integer> LAST_DURABILITY_WARNING_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_AUTO_EAT_DEBUG_TICK = new HashMap();
    private static final Map<UUID, Integer> LAST_VOID_RESCUE_TICK = new HashMap();
    private static final Map<class_1297, Integer> WORKSTATION_HIGHLIGHTS = new HashMap();

    private SurvivalAidRuntime() {
    }

    public static void tick(MinecraftServer server) {
        SurvivalAidTntLikeBlocks.tick(server);
        tickWorkstationHighlights();
        for (class_3222 player : server.method_3760().method_14571()) {
            handleDeathCoordinateMessage(player);
            handleLowHealthGlow(player);
            handleLowDurabilityWarning(server, player);
            handleAutoEatFood(server, player);
            handleVoidPlayerRescue(server, player);
        }
    }

    private static void handleVoidPlayerRescue(MinecraftServer server, class_3222 player) {
        if (!VoidPlayerRescueRule.survivalAidVoidPlayerRescue || player.method_7337() || player.method_7325() || player.method_29504() || player.method_23318() > VoidPlayerRescueYRule.survivalAidVoidPlayerRescueY) {
            return;
        }
        UUID playerId = player.method_5667();
        int currentTick = server.method_3780();
        int lastRescueTick = LAST_VOID_RESCUE_TICK.getOrDefault(playerId, -200).intValue();
        if (lastRescueTick > currentTick) {
            lastRescueTick = -200;
        }
        int cooldown = Math.max(1, VoidPlayerRescueCooldownRule.survivalAidVoidPlayerRescueCooldown);
        if (currentTick - lastRescueTick < cooldown) {
            return;
        }
        boolean equippedElytra = ensureElytraEquipped(player);
        if (!equippedElytra && !player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
            LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
            player.method_7353(class_2561.method_43470("虚空救援：背包中没有可用鞘翅。"), true);
            return;
        }
        int rocketSlot = findItemSlot(player, class_1802.field_8639);
        if (rocketSlot < 0) {
            LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
            player.method_7353(class_2561.method_43470("虚空救援：背包中没有火箭烟花。"), true);
            return;
        }
        class_1799 rocket = player.method_31548().method_5438(rocketSlot);
        class_1799 rocketForUse = rocket.method_46651(1);
        rocket.method_7934(1);
        class_1671 firework = new class_1671(player.method_37908(), rocketForUse, player);
        player.method_37908().method_8649(firework);
        player.method_5762(0.0d, 0.6d, 0.0d);
        player.field_6037 = true;
        LAST_VOID_RESCUE_TICK.put(playerId, Integer.valueOf(currentTick));
        player.method_7353(class_2561.method_43470("虚空救援：已自动" + (equippedElytra ? "穿上鞘翅并" : "") + "使用火箭烟花。"), true);
    }

    private static boolean ensureElytraEquipped(class_3222 player) {
        int elytraSlot;
        class_1799 chestStack = player.method_6118(class_1304.field_6174);
        if (chestStack.method_31574(class_1802.field_8833) || (elytraSlot = findItemSlot(player, class_1802.field_8833)) < 0) {
            return false;
        }
        class_1799 elytra = player.method_31548().method_5438(elytraSlot);
        player.method_5673(class_1304.field_6174, elytra.method_7972());
        player.method_31548().method_5447(elytraSlot, chestStack);
        return true;
    }

    private static int findItemSlot(class_3222 player, class_1792 item) {
        for (int slot = 0; slot < player.method_31548().method_5439(); slot++) {
            class_1799 stack = player.method_31548().method_5438(slot);
            if (!stack.method_7960() && stack.method_31574(item)) {
                return slot;
            }
        }
        return -1;
    }

    public static void trackWorkstationHighlight(class_1297 entity) {
        WORKSTATION_HIGHLIGHTS.put(entity, 40);
    }

    private static void tickWorkstationHighlights() {
        WORKSTATION_HIGHLIGHTS.entrySet().removeIf(entry -> {
            int remaining = ((Integer) entry.getValue()).intValue() - 1;
            class_1297 entity = (class_1297) entry.getKey();
            if (!entity.method_5805() || remaining <= 0) {
                if (entity.method_5805()) {
                    entity.method_5650(class_1297.class_5529.field_26999);
                    return true;
                }
                return true;
            }
            entry.setValue(Integer.valueOf(remaining));
            return false;
        });
    }

    private static void handleDeathCoordinateMessage(class_3222 player) {
        UUID playerId = player.method_5667();
        boolean isDead = player.method_29504();
        boolean wasDead = LAST_DEAD_STATE.getOrDefault(playerId, false).booleanValue();
        if (DeathCoordinateMessageRule.survivalAidDeathCoordinateMessage && isDead && !wasDead) {
            class_2338 pos = player.method_24515();
            String dimension = player.method_37908().method_27983().method_29177().toString();
            player.method_7353(class_2561.method_43470("死亡位置：" + dimension + " " + pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260()), false);
        }
        LAST_DEAD_STATE.put(playerId, Boolean.valueOf(isDead));
    }

    private static void handleLowHealthGlow(class_3222 player) {
        if (!LowHealthGlowRule.survivalAidLowHealthGlow) {
            if (player.method_5851()) {
                player.method_5834(false);
            }
        } else {
            float threshold = Math.max(1.0f, LowHealthGlowThresholdRule.survivalAidLowHealthGlowThreshold);
            boolean shouldGlow = !player.method_29504() && player.method_6032() <= threshold;
            if (player.method_5851() != shouldGlow) {
                player.method_5834(shouldGlow);
            }
        }
    }

    private static void handleLowDurabilityWarning(MinecraftServer server, class_3222 player) {
        int remainingDurability;
        if (LowDurabilityWarningRule.survivalAidLowDurabilityWarning <= 0) {
            return;
        }
        class_1799 stack = player.method_6047();
        if (stack.method_7960() || !stack.method_7963() || (remainingDurability = stack.method_7936() - stack.method_7919()) > LowDurabilityWarningRule.survivalAidLowDurabilityWarning) {
            return;
        }
        UUID playerId = player.method_5667();
        int currentTick = server.method_3780();
        int lastWarningTick = LAST_DURABILITY_WARNING_TICK.getOrDefault(playerId, -200).intValue();
        if (currentTick - lastWarningTick < 200) {
            return;
        }
        LAST_DURABILITY_WARNING_TICK.put(playerId, Integer.valueOf(currentTick));
        player.method_7353(class_2561.method_43470("耐久不足：" + stack.method_7964().getString() + " 剩余 " + remainingDurability + " 点耐久。"), true);
    }

    private static void handleAutoEatFood(MinecraftServer server, class_3222 player) {
        if (!AutoEatFoodRule.survivalAidAutoEatFood || player.method_7337() || player.method_7325()) {
            debugAutoEat(server, player, "跳过：规则=" + AutoEatFoodRule.survivalAidAutoEatFood + "，创造=" + player.method_7337() + "，旁观=" + player.method_7325());
            return;
        }
        int threshold = Math.max(0, Math.min(19, AutoEatFoodThresholdRule.survivalAidAutoEatFoodThreshold));
        if (player.method_7344().method_7586() > threshold || !player.method_7332(false)) {
            debugAutoEat(server, player, "跳过：饥饿值=" + player.method_7344().method_7586() + "，阈值=" + threshold + "，canConsume=" + player.method_7332(false));
            return;
        }
        UUID playerId = player.method_5667();
        int currentTick = server.method_3780();
        int lastAutoEatTick = LAST_AUTO_EAT_TICK.getOrDefault(playerId, -40).intValue();
        if (lastAutoEatTick > currentTick) {
            lastAutoEatTick = -40;
        }
        if (currentTick - lastAutoEatTick < 40) {
            debugAutoEat(server, player, "跳过：自动进食冷却中，剩余 " + (40 - (currentTick - lastAutoEatTick)) + " tick");
            return;
        }
        int expectedFoodLevel = player.method_7344().method_7586();
        int eatenCount = 0;
        int foodStackCount = 0;
        int skippedEnchantedGoldenAppleCount = 0;
        String lastFoodName = "";
        for (int slot = 0; slot < player.method_31548().method_5439(); slot++) {
            class_1799 stack = player.method_31548().method_5438(slot);
            if (!stack.method_7960()) {
                if (stack.method_31574(class_1802.field_8367)) {
                    skippedEnchantedGoldenAppleCount++;
                } else {
                    class_4174 food = (class_4174) stack.method_57824(class_9334.field_50075);
                    if (food != null) {
                        foodStackCount++;
                        class_10124 consumable = (class_10124) stack.method_57824(class_9334.field_53964);
                        if (consumable != null) {
                            boolean hasNonFoodEffect = false;
                            Iterator it = consumable.comp_3089().iterator();
                            while (true) {
                                if (!it.hasNext()) {
                                    break;
                                }
                                class_10134 effect = (class_10134) it.next();
                                if (!(effect instanceof class_10132)) {
                                    hasNonFoodEffect = true;
                                    break;
                                }
                            }
                            if (hasNonFoodEffect) {
                                continue;
                            }
                        }
                        int amountToEat = Math.min(stack.method_7947(), Math.max(1, (((20 - expectedFoodLevel) + food.comp_2491()) - 1) / food.comp_2491()));
                        for (int eatenFromStack = 0; eatenFromStack < amountToEat && player.method_7332(false); eatenFromStack++) {
                            player.method_7344().method_7579(food);
                            List<class_1293> foodEffects = new ArrayList<>();
                            if (consumable != null) {
                                for (class_10132 class_10132Var : consumable.comp_3089()) {
                                    if (class_10132Var instanceof class_10132) {
                                        class_10132 effects = class_10132Var;
                                        if (effects.comp_3095() >= 1.0f || player.method_59922().method_43057() < effects.comp_3095()) {
                                            foodEffects.addAll(effects.comp_3094());
                                        }
                                    }
                                }
                            }
                            for (class_1293 instance : foodEffects) {
                                player.method_6092(new class_1293(instance));
                            }
                            expectedFoodLevel = Math.min(20, expectedFoodLevel + food.comp_2491());
                            eatenCount++;
                            stack.method_7934(1);
                            lastFoodName = stack.method_7964().getString();
                            if (expectedFoodLevel >= 20 || eatenCount >= 8) {
                                break;
                            }
                        }
                        if (expectedFoodLevel >= 20 || eatenCount >= 8) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }
        if (eatenCount > 0) {
            LAST_AUTO_EAT_TICK.put(playerId, Integer.valueOf(currentTick));
            player.method_7353(class_2561.method_43470("自动进食：已从背包消耗 " + eatenCount + " 个食物，最后食物：" + lastFoodName + "。"), true);
        } else {
            debugAutoEat(server, player, "未进食：可用食物组=" + foodStackCount + "，跳过附魔金苹果组=" + skippedEnchantedGoldenAppleCount + "，饥饿值=" + player.method_7344().method_7586() + "，canConsume=" + player.method_7332(false));
        }
    }

    private static void debugAutoEat(MinecraftServer server, class_3222 player, String message) {
        if (!AutoEatFoodRule.survivalAidAutoEatFoodDebug) {
            return;
        }
        UUID playerId = player.method_5667();
        int currentTick = server.method_3780();
        int lastDebugTick = LAST_AUTO_EAT_DEBUG_TICK.getOrDefault(playerId, -20).intValue();
        if (currentTick - lastDebugTick < 20) {
            return;
        }
        LAST_AUTO_EAT_DEBUG_TICK.put(playerId, Integer.valueOf(currentTick));
        player.method_7353(class_2561.method_43470("自动进食调试：" + message), false);
    }
}
