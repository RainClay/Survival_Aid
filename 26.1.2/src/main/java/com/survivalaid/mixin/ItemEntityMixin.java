package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import com.survivalaid.features.InstantItemPickupRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.NoItemDespawnRule;
import com.survivalaid.features.VisitorNoItemPickupRule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/mixin/ItemEntityMixin.class */
@Mixin({ItemEntity.class})
public abstract class ItemEntityMixin {
    @Inject(method = {"playerTouch"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$handleItemPickup(Player player, CallbackInfo ci) {
        if (VisitorNoItemPickupRule.survivalAidVisitorNoItemPickup && (player instanceof ServerPlayer)) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (SurvivalAidVisitors.isVisitor(serverPlayer)) {
                SurvivalAidVisitors.notifyBlocked(serverPlayer);
                ci.cancel();
                return;
            }
        }
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getItem();
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!survivalAid$canPlayerPickConfiguredItem(player, itemId, stack)) {
            ci.cancel();
            return;
        }
        if (InstantItemPickupRule.survivalAidInstantItemPickup) {
            self.setPickUpDelay(0);
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
                    Identifier parsedId = Identifier.tryParse(trimmed);
                    if (parsedId == null && trimmed.indexOf(58) < 0) {
                        parsedId = Identifier.tryParse("minecraft:" + trimmed);
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

    private boolean survivalAid$canPlayerPickConfiguredItem(Player player, Identifier itemId, ItemStack stack) {
        String[] rule;
        String whitelist = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist;
        String trimmedWhitelist = whitelist.trim();
        if (trimmedWhitelist.isEmpty() || trimmedWhitelist.equalsIgnoreCase("none")) {
            return true;
        }
        String playerName = player.getGameProfile().name();
        String playerUuid = player.getUUID().toString();
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
            player.sendSystemMessage(Component.translatable("survival_aid.msg.pickup_debug", itemId, stack.getItemName().getString(), playerName, playerHasRule, !playerHasRule || playerAllowedForItem));
        }
        return !playerHasRule || playerAllowedForItem;
    }

    private String[] survivalAid$parsePlayerItemPickupRule(String entry, Identifier currentItemId) {
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

    private boolean survivalAid$ruleMentionsItem(String entry, Identifier currentItemId) {
        for (String token : entry.split("\\s+|=")) {
            String cleanedToken = survivalAid$stripQuotes(token.trim());
            if (!cleanedToken.isEmpty() && survivalAid$matchesConfiguredItem(cleanedToken, currentItemId)) {
                return true;
            }
        }
        return false;
    }

    private boolean survivalAid$matchesConfiguredItem(String configuredItem, Identifier itemId) {
        Identifier configuredItemId = Identifier.tryParse(configuredItem);
        if (configuredItemId != null && configuredItem.indexOf(58) >= 0) {
            return itemId.equals(configuredItemId);
        }
        return itemId.getPath().equalsIgnoreCase(configuredItem);
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

    @Inject(method = {"tick"}, at = {@At("HEAD")})
    private void survivalAid$preventDespawn(CallbackInfo ci) {
        if (NoItemDespawnRule.survivalAidNoItemDespawn && !((ItemEntity) (Object) this).level().isClientSide()) {
            ((ItemEntity) (Object) this).setUnlimitedLifetime();
        }
    }
}
