package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import com.survivalaid.features.InstantItemPickupRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.NoItemDespawnRule;
import com.survivalaid.features.VisitorNoItemPickupRule;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/mixin/ItemEntityMixin.class */
@Mixin({class_1542.class})
public abstract class ItemEntityMixin {
    @Inject(method = {"method_5694"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$handleItemPickup(class_1657 player, CallbackInfo ci) {
        if (VisitorNoItemPickupRule.survivalAidVisitorNoItemPickup && (player instanceof class_3222)) {
            class_3222 serverPlayer = (class_3222) player;
            if (SurvivalAidVisitors.isVisitor(serverPlayer)) {
                SurvivalAidVisitors.notifyBlocked(serverPlayer);
                ci.cancel();
                return;
            }
        }
        class_1542 self = (class_1542) this;
        class_1799 stack = self.method_6983();
        class_2960 itemId = class_7923.field_41178.method_10221(stack.method_7909());
        if (!survivalAid$canPlayerPickConfiguredItem(player, itemId, stack)) {
            ci.cancel();
            return;
        }
        if (InstantItemPickupRule.survivalAidInstantItemPickup) {
            self.method_6982(0);
        }
        String filter = ItemPickupFilterRule.survivalAidItemPickupFilter;
        String trimmedFilter = filter.trim();
        if (!trimmedFilter.isEmpty() && !trimmedFilter.equalsIgnoreCase("none")) {
            boolean allowed = false;
            String[] strArrSplit = filter.split(",");
            int length = strArrSplit.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String entry = strArrSplit[i];
                String trimmed = entry.trim();
                if (!trimmed.isEmpty()) {
                    class_2960 parsedId = class_2960.method_12829(trimmed);
                    if (parsedId == null && trimmed.indexOf(58) < 0) {
                        parsedId = class_2960.method_12829("minecraft:" + trimmed);
                    }
                    if (parsedId != null && itemId.equals(parsedId)) {
                        allowed = true;
                        break;
                    }
                }
                i++;
            }
            if (!allowed) {
                ci.cancel();
            }
        }
    }

    private boolean survivalAid$canPlayerPickConfiguredItem(class_1657 player, class_2960 itemId, class_1799 stack) {
        String[] rule;
        String whitelist = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist;
        String trimmedWhitelist = whitelist.trim();
        if (trimmedWhitelist.isEmpty() || trimmedWhitelist.equalsIgnoreCase("none")) {
            return true;
        }
        String playerName = player.method_7334().getName();
        String playerUuid = player.method_5845();
        boolean playerHasRule = false;
        boolean playerAllowedForItem = false;
        String[] strArrSplit = whitelist.split(",");
        int length = strArrSplit.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String entry = strArrSplit[i];
            String trimmedEntry = entry.trim();
            if (!trimmedEntry.isEmpty() && (rule = survivalAid$parsePlayerItemPickupRule(trimmedEntry, itemId)) != null && survivalAid$matchesPlayer(rule[1], playerName, playerUuid)) {
                playerHasRule = true;
                if (survivalAid$matchesConfiguredItem(rule[0], itemId)) {
                    playerAllowedForItem = true;
                    break;
                }
            }
            i++;
        }
        if (ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelistDebug) {
            player.method_7353(class_2561.method_43470("SurvivalAid pickup: item=" + String.valueOf(itemId) + ", name=" + stack.method_7964().getString() + ", player=" + playerName + ", playerHasRule=" + playerHasRule + ", allowed=" + (!playerHasRule || playerAllowedForItem)), true);
        }
        return !playerHasRule || playerAllowedForItem;
    }

    private String[] survivalAid$parsePlayerItemPickupRule(String entry, class_2960 currentItemId) {
        int separatorIndex = entry.indexOf(61);
        if (separatorIndex > 0 && separatorIndex < entry.length() - 1) {
            String item = survivalAid$stripQuotes(entry.substring(0, separatorIndex).trim());
            String player = survivalAid$stripQuotes(entry.substring(separatorIndex + 1).trim());
            if (!item.isEmpty() && !player.isEmpty()) {
                return new String[]{item, player};
            }
        }
        String[] parts = entry.split("\\s+", 2);
        if (parts.length != 2) {
            return null;
        }
        String first = survivalAid$stripQuotes(parts[0].trim());
        String second = survivalAid$stripQuotes(parts[1].trim());
        if (first.isEmpty() || second.isEmpty()) {
            return null;
        }
        if (survivalAid$matchesConfiguredItem(first, currentItemId)) {
            return new String[]{first, second};
        }
        if (survivalAid$matchesConfiguredItem(second, currentItemId)) {
            return new String[]{second, first};
        }
        return new String[]{first, second};
    }

    private boolean survivalAid$ruleMentionsItem(String entry, class_2960 currentItemId) {
        for (String token : entry.split("\\s+|=")) {
            String cleanedToken = survivalAid$stripQuotes(token.trim());
            if (!cleanedToken.isEmpty() && survivalAid$matchesConfiguredItem(cleanedToken, currentItemId)) {
                return true;
            }
        }
        return false;
    }

    private boolean survivalAid$matchesConfiguredItem(String configuredItem, class_2960 itemId) {
        class_2960 configuredItemId = class_2960.method_12829(configuredItem);
        if (configuredItemId != null && configuredItem.indexOf(58) >= 0) {
            return itemId.equals(configuredItemId);
        }
        return itemId.method_12832().equalsIgnoreCase(configuredItem);
    }

    private boolean survivalAid$matchesPlayer(String configuredPlayer, String playerName, String playerUuid) {
        return configuredPlayer.equalsIgnoreCase(playerName) || configuredPlayer.equalsIgnoreCase(playerUuid);
    }

    private String survivalAid$stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    @Inject(method = {"method_5773"}, at = {@At("HEAD")})
    private void survivalAid$preventDespawn(CallbackInfo ci) {
        if (NoItemDespawnRule.survivalAidNoItemDespawn && !((class_1542) this).method_37908().method_8608()) {
            ((class_1542) this).method_35190();
        }
    }
}
