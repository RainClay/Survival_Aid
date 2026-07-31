package com.survivalaid;

import com.survivalaid.features.VisitorPlayersRule;
import net.minecraft.class_2561;
import net.minecraft.class_3222;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.4-1.0.1.jar:com/survivalaid/SurvivalAidVisitors.class */
public final class SurvivalAidVisitors {
    private SurvivalAidVisitors() {
    }

    public static boolean isVisitor(class_3222 player) {
        String configuredPlayers = VisitorPlayersRule.survivalAidVisitorPlayers;
        if (configuredPlayers == null || configuredPlayers.isBlank() || configuredPlayers.equalsIgnoreCase("none")) {
            return false;
        }
        String playerName = player.method_7334().getName();
        String playerUuid = player.method_5845();
        for (String configuredPlayer : configuredPlayers.split(",")) {
            String token = stripQuotes(configuredPlayer.trim());
            if (!token.isEmpty() && (token.equalsIgnoreCase(playerName) || token.equalsIgnoreCase(playerUuid))) {
                return true;
            }
        }
        return false;
    }

    public static void notifyBlocked(class_3222 player) {
        player.method_7353(class_2561.method_43470("你当前是访客，不能与方块或实体交互。"), true);
    }

    public static void notifyCommandBlocked(class_3222 player) {
        player.method_7353(class_2561.method_43470("你当前是访客，不能执行命令。"), true);
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
