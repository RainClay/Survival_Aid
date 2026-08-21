package com.survivalaid;

import com.survivalaid.features.VisitorPlayersRule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidVisitors.class */
public final class SurvivalAidVisitors {
    private SurvivalAidVisitors() {
    }

    public static boolean isVisitor(ServerPlayer player) {
        String configuredPlayers = VisitorPlayersRule.survivalAidVisitorPlayers;
        if (configuredPlayers == null || configuredPlayers.isBlank() || configuredPlayers.equalsIgnoreCase("none")) {
            return false;
        }
        String playerName = player.getGameProfile().name();
        String playerUuid = player.getUUID().toString();
        for (String configuredPlayer : configuredPlayers.split(",")) {
            String token = stripQuotes(configuredPlayer.trim());
            if (!token.isEmpty() && (token.equalsIgnoreCase(playerName) || token.equalsIgnoreCase(playerUuid))) {
                return true;
            }
        }
        return false;
    }

    public static void notifyBlocked(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("你当前是访客，不能与方块或实体交互。"));
    }

    public static void notifyCommandBlocked(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("你当前是访客，不能执行命令。"));
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '\"' && last == '\"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
