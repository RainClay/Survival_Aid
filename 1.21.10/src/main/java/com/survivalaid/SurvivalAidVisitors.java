package com.survivalaid;

import com.survivalaid.features.VisitorPlayersRule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class SurvivalAidVisitors {
    private SurvivalAidVisitors() {
    }

    public static boolean isVisitor(ServerPlayerEntity player) {
        String configuredPlayers = VisitorPlayersRule.survivalAidVisitorPlayers;
        if (configuredPlayers == null || configuredPlayers.isBlank() || configuredPlayers.equalsIgnoreCase("none")) {
            return false;
        }
        String playerName = player.getName().getString();
        String playerUuid = player.getUuidAsString();
        for (String configuredPlayer : configuredPlayers.split(",")) {
            String token = stripQuotes(configuredPlayer.trim());
            if (!token.isEmpty() && (token.equalsIgnoreCase(playerName) || token.equalsIgnoreCase(playerUuid))) {
                return true;
            }
        }
        return false;
    }

    public static void notifyBlocked(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("survival_aid.message.visitor_blocked"), true);
    }

    public static void notifyCommandBlocked(ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("survival_aid.message.visitor_command_blocked"), true);
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
