package com.survivalaid;

import java.util.LinkedHashMap;
import java.util.Map;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidTranslations.class */
public final class SurvivalAidTranslations {
    private static final Map<String, String> ZH_CN = createZhCnTranslations();

    private SurvivalAidTranslations() {
    }

    public static Map<String, String> get(String lang) {
        return ZH_CN;
    }

    private static Map<String, String> createZhCnTranslations() {
        Map<String, String> translations = new LinkedHashMap<>();
        translations.put("survival_aid", "Survival Aid");
        translations.put("survival_aid.category.survival_aid", "Survival Aid");
        translations.put("carpet.category.survival_aid", "Survival Aid");
        translations.put("survival_aid.rule.survivalAidCreeperGriefingControl.name", "苦力怕爆炸不破坏方块");
        translations.put("survival_aid.rule.survivalAidCreeperGriefingControl.desc", "苦力怕爆炸仍会造成实体伤害和击退，但不会破坏方块。");
        translations.put("survival_aid.rule.survivalAidDeathCoordinateMessage.name", "死亡坐标提示");
        translations.put("survival_aid.rule.survivalAidDeathCoordinateMessage.desc", "玩家死亡时向本人发送死亡维度和坐标。");
        translations.put("survival_aid.rule.survivalAidAutoEatFood.name", "低饥饿值自动进食");
        translations.put("survival_aid.rule.survivalAidAutoEatFood.desc", "饥饿值低于或等于 survivalAidAutoEatFoodThreshold 时，自动从背包里消耗食物。");
        translations.put("survival_aid.rule.survivalAidAutoEatFoodThreshold.name", "自动进食饥饿值阈值");
        translations.put("survival_aid.rule.survivalAidAutoEatFoodThreshold.desc", "survivalAidAutoEatFood 的饥饿值阈值。");
        translations.put("survival_aid.rule.survivalAidAutoEatFoodDebug.name", "自动进食调试提示");
        translations.put("survival_aid.rule.survivalAidAutoEatFoodDebug.desc", "开启后，每秒提示自动进食执行或跳过的原因，用于排查游戏过程中突然不吃的问题。");
        translations.put("survival_aid.rule.survivalAidDisableVillageCatSpawn.name", "禁止村庄床触发猫生成");
        translations.put("survival_aid.rule.survivalAidDisableVillageCatSpawn.desc", "禁止村庄因占用床数量达到 5 张而自然生成猫，不影响女巫小屋猫。");
        translations.put("survival_aid.rule.survivalAidLowDurabilityWarning.name", "低耐久提醒");
        translations.put("survival_aid.rule.survivalAidLowDurabilityWarning.desc", "主手物品耐久低于或等于该值时提醒玩家；设为 0 关闭。");
        translations.put("survival_aid.rule.survivalAidLowHealthGlow.name", "低血量发光提醒");
        translations.put("survival_aid.rule.survivalAidLowHealthGlow.desc", "玩家血量低于或等于 survivalAidLowHealthGlowThreshold 时自动发光。");
        translations.put("survival_aid.rule.survivalAidLowHealthGlowThreshold.name", "低血量发光阈值");
        translations.put("survival_aid.rule.survivalAidLowHealthGlowThreshold.desc", "survivalAidLowHealthGlow 的血量阈值，单位为血量点。");
        translations.put("survival_aid.rule.survivalAidNoCrammingEntities.name", "指定实体免疫挤压伤害");
        translations.put("survival_aid.rule.survivalAidNoCrammingEntities.desc", "逗号分隔的实体 ID 列表，列表内实体不会受到实体挤压伤害，例如：minecraft:cow,minecraft:sheep。");
        translations.put("survival_aid.rule.survivalAidStackingOptimizedEntities.name", "指定实体堆叠优化");
        translations.put("survival_aid.rule.survivalAidStackingOptimizedEntities.desc", "逗号分隔的实体 ID 列表，列表内同种实体堆叠时跳过互推计算，例如：minecraft:cow,minecraft:sheep。");
        translations.put("survival_aid.rule.survivalAidInstantItemPickup.name", "掉落物即时拾取");
        translations.put("survival_aid.rule.survivalAidInstantItemPickup.desc", "掉落物即时拾取：开启后掉落物无延迟（0 tick），碰到即拾取。");
        translations.put("survival_aid.rule.survivalAidItemPickupFilter.name", "物品拾取过滤白名单");
        translations.put("survival_aid.rule.survivalAidItemPickupFilter.desc", "物品拾取过滤白名单（逗号分隔的物品 ID，如 minecraft:diamond）。设为 none 或空时允许拾取所有物品。");
        translations.put("survival_aid.rule.survivalAidPlayerItemPickupWhitelist.name", "指定玩家拾取物品白名单");
        translations.put("survival_aid.rule.survivalAidPlayerItemPickupWhitelist.desc", "指定某个玩家只能拾取白名单中的物品。建议使用 /survivalaid pickup allow <物品名> <玩家名> 添加，例如 /survivalaid pickup allow diamond awa。");
        translations.put("survival_aid.rule.survivalAidPlayerItemPickupWhitelistDebug.name", "指定玩家拾取物品调试提示");
        translations.put("survival_aid.rule.survivalAidPlayerItemPickupWhitelistDebug.desc", "开启后，玩家碰到掉落物时会提示实际检测到的物品 ID、玩家名和是否允许拾取，用于排查白名单配置。");
        translations.put("survival_aid.rule.survivalAidNoItemDespawn.name", "禁止物品消失");
        translations.put("survival_aid.rule.survivalAidNoItemDespawn.desc", "掉落物不会消失：开启后掉落物永不自然消失（年龄重置，不触发 discard）。");
        translations.put("survival_aid.rule.survivalAidTntLikeBlocks.name", "所有方块 TNT 化");
        translations.put("survival_aid.rule.survivalAidTntLikeBlocks.desc", "开启后，方块被红石、打火石、火焰弹或燃烧投射物触发时，会生成一个显示原方块材质的点燃方块，倒计时后像 TNT 一样爆炸。");
        translations.put("survival_aid.rule.survivalAidVisitorPlayers.name", "访客玩家名单");
        translations.put("survival_aid.rule.survivalAidVisitorPlayers.desc", "逗号分隔的玩家名或 UUID 列表。名单内玩家不能破坏、放置、使用方块，不能使用物品交互，也不能攻击或交互实体。");
        translations.put("survival_aid.rule.survivalAidNoEndermanGriefing.name", "禁止末影人搬运方块");
        translations.put("survival_aid.rule.survivalAidNoEndermanGriefing.desc", "末影人不会搬起或放下任何方块。");
        translations.put("survival_aid.rule.survivalAidPreventToolBreak.name", "防止工具损坏");
        translations.put("survival_aid.rule.survivalAidPreventToolBreak.desc", "工具耐久低于或等于 survivalAidPreventToolBreakThreshold 时停止使用，防止损坏。");
        translations.put("survival_aid.rule.survivalAidPreventToolBreakThreshold.name", "工具保护耐久阈值");
        translations.put("survival_aid.rule.survivalAidPreventToolBreakThreshold.desc", "survivalAidPreventToolBreak 的耐久阈值。");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescue.name", "虚空救援");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescue.desc", "玩家掉入虚空时自动传送回上次地面位置。");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescueY.name", "虚空救援 Y 坐标");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescueY.desc", "玩家掉落到该 Y 坐标以下时触发虚空救援。");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescueCooldown.name", "虚空救援冷却");
        translations.put("survival_aid.rule.survivalAidVoidPlayerRescueCooldown.desc", "虚空救援的冷却时间，单位为 tick（1 秒 = 20 tick）。");
        translations.put("survival_aid.rule.survivalAidVisitorNoItemPickup.name", "访客禁止捡拾物品");
        translations.put("survival_aid.rule.survivalAidVisitorNoItemPickup.desc", "访客玩家不能捡拾掉落物。");
        translations.put("survival_aid.rule.survivalAidWorkstationHighLight.name", "村民工作站高亮");
        translations.put("survival_aid.rule.survivalAidWorkstationHighLight.desc", "潜行右键村民时以发光方块实体高亮其绑定的工作站位置。");
        translations.put("survival_aid.rule.survivalAidNetherPortalSolid.name", "地狱门传送方块完整轮廓");
        translations.put("survival_aid.rule.survivalAidNetherPortalSolid.desc", "将地狱门传送方块的轮廓箱恢复为完整方块，实体仍能穿过但可正常触发传送。");
        translations.put("survival_aid.rule.survivalAidBotMinecartPreserve.name", "假人矿车保护");
        translations.put("survival_aid.rule.survivalAidBotMinecartPreserve.desc", "开启后，假人下线/被kill时先自动下车再移除，防止矿车消失或重复生成。");
        addCarpetTranslationAliases(translations);
        return Map.copyOf(translations);
    }

    private static void addCarpetTranslationAliases(Map<String, String> translations) {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("survival_aid.rule.")) {
                aliases.put("carpet.rule." + key.substring("survival_aid.rule.".length()), entry.getValue());
            } else if (key.startsWith("survival_aid.category.")) {
                aliases.put("carpet.category." + key.substring("survival_aid.category.".length()), entry.getValue());
            }
        }
        translations.putAll(aliases);
    }
}
