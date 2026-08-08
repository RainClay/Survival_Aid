package com.survivalaid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SurvivalAidClient implements ClientModInitializer {
    private static String lastSentName = null;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getNetworkHandler() == null) {
                return;
            }
            if (!ClientPlayNetworking.canSend(SurvivalAidProjectionPayload.TYPE)) {
                return;
            }
            if (++tickCounter < 20) {
                return;
            }
            tickCounter = 0;
            String name = ProjectionNameProvider.getCurrentProjectionFileName();
            String effective = name == null ? "" : name;
            if (!effective.equals(lastSentName)) {
                lastSentName = effective;
                ClientPlayNetworking.send(new SurvivalAidProjectionPayload(effective));
            }
        });
    }
}
