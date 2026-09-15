package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidVisitors;
import com.survivalaid.features.InstantItemPickupRule;
import com.survivalaid.features.ItemPickupFilterRule;
import com.survivalaid.features.NoItemDespawnRule;
import com.survivalaid.features.VisitorNoItemPickupRule;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void survivalAid$handleItemPickup(PlayerEntity player, CallbackInfo ci) {
        if (VisitorNoItemPickupRule.survivalAidVisitorNoItemPickup && (player instanceof ServerPlayerEntity serverPlayer)) {
            if (SurvivalAidVisitors.isVisitor(serverPlayer)) {
                SurvivalAidVisitors.notifyBlocked(serverPlayer);
                ci.cancel();
                return;
            }
        }
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        if (!survivalAid$canPlayerPickConfiguredItem(player, itemId, stack)) {
            ci.cancel();
            return;
        }
        if (InstantItemPickupRule.survivalAidInstantItemPickup) {
            self.setPickupDelay(0);
        }
        String filter = ItemPickupFilterRule.survivalAidItemPickupFilter;
        String trimmedFilter = filter.trim();
        if (!trimmedFilter.isEmpty() && !trimmedFilter.equalsIgnoreCase("none")) {
            boolean allowed = false;
            for (String entry : filter.split(",")) {
                String trimmed = entry.trim();
                if (!trimmed.isEmpty()) {
                    Identifier parsedId = Identifier.tryParse(trimmed);
                    if (parsedId == null && trimmed.indexOf(':') < 0) {
                        parsedId = Identifier.tryParse("minecraft:" + trimmed);
                    }
                    if (parsedId != null && itemId.equals(parsedId)) {
                        allowed = true;
                        break;
                    }
                }
            }
            if (!allowed) {
                ci.cancel();
            }
        }
    }

    private boolean survivalAid$canPlayerPickConfiguredItem(PlayerEntity player, Identifier itemId, ItemStack stack) {
        String whitelist = ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelist;
        String trimmedWhitelist = whitelist.trim();
        if (trimmedWhitelist.isEmpty() || trimmedWhitelist.equalsIgnoreCase("none")) {
            return true;
        }
        String playerName = player.getName().getString();
        String playerUuid = player.getUuidAsString();
        boolean playerHasRule = false;
        boolean playerAllowedForItem = false;
        for (String entry : whitelist.split(",")) {
            String trimmedEntry = entry.trim();
            if (!trimmedEntry.isEmpty()) {
                String[] rule = survivalAid$parsePlayerItemPickupRule(trimmedEntry, itemId);
                if (rule != null && survivalAid$matchesPlayer(rule[1], playerName, playerUuid)) {
                    playerHasRule = true;
                    if (survivalAid$matchesConfiguredItem(rule[0], itemId)) {
                        playerAllowedForItem = true;
                        break;
                    }
                }
            }
        }
        if (ItemPickupFilterRule.survivalAidPlayerItemPickupWhitelistDebug) {
            player.sendMessage(Text.translatable("survival_aid.msg.pickup_debug", itemId, stack.getName().getString(), playerName, playerHasRule, !playerHasRule || playerAllowedForItem), true);
        }
        return !playerHasRule || playerAllowedForItem;
    }

    private String[] survivalAid$parsePlayerItemPickupRule(String entry, Identifier currentItemId) {
        int separatorIndex = entry.indexOf('=');
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

    private boolean survivalAid$matchesConfiguredItem(String configuredItem, Identifier itemId) {
        Identifier configuredItemId = Identifier.tryParse(configuredItem);
        if (configuredItemId != null && configuredItem.indexOf(':') >= 0) {
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
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void survivalAid$preventDespawn(CallbackInfo ci) {
        if (NoItemDespawnRule.survivalAidNoItemDespawn && !((ItemEntity) (Object) this).getWorld().isClient()) {
            ((ItemEntity) (Object) this).setNeverDespawn();
        }
    }
}
