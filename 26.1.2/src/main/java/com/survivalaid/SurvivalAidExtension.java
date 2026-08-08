package com.survivalaid;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidExtension.class */
public class SurvivalAidExtension implements CarpetExtension, ModInitializer {
    public static final String MOD_ID = "survival_aid";
    public static final String MOD_NAME = "Carpet SurvivalAid";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    static final SettingsManager SETTINGS_MANAGER = new SettingsManager("1.0.0", "survival_aid", MOD_NAME);

    public void onInitialize() {
        LOGGER.info("{} loaded by Fabric entrypoint, registering Carpet extension and callbacks.", MOD_NAME);
        CarpetServer.manageExtension(this);
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            SurvivalAidRuntime.tick(server);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SurvivalAidCommands.register(dispatcher);
        });
        PayloadTypeRegistry.serverboundPlay().register(SurvivalAidProjectionPayload.TYPE, SurvivalAidProjectionPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SurvivalAidProjectionPayload.TYPE, (payload, context) -> {
            ProjectionSyncStore.set(context.player().getUUID(), payload.name());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ProjectionSyncStore.remove(handler.getPlayer().getUUID());
        });
    }

    public void onGameStarted() {
        int ruleCount = SurvivalAidSettings.registerRules();
        LOGGER.info("{} Carpet extension started, {} extension rules registered: {}", new Object[]{MOD_NAME, Integer.valueOf(ruleCount), SurvivalAidSettings.getRuleNames()});
    }

    public SettingsManager extensionSettingsManager() {
        return SETTINGS_MANAGER;
    }

    public String version() {
        return "1.0.0";
    }

    public Map<String, String> canHasTranslations(String lang) {
        return SurvivalAidTranslations.get(lang);
    }
}
