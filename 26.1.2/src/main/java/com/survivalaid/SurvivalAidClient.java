package com.survivalaid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class SurvivalAidClient implements ClientModInitializer {
    private static String lastSent = null;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.getConnection() == null) {
                return;
            }
            if (!ClientPlayNetworking.canSend(SurvivalAidProjectionPayload.TYPE)) {
                return;
            }
            if (++tickCounter < 20) {
                return;
            }
            tickCounter = 0;
            ProjectionClientState st = ProjectionNameProvider.getCurrentProjectionState();
            String effective = st == null ? "" : (st.name + "\u0001" + st.rangeType + "\u0001" + st.minY + "\u0001" + st.maxY + "\u0001" + st.originY);
            if (!effective.equals(lastSent)) {
                lastSent = effective;
                ClientPlayNetworking.send(new SurvivalAidProjectionPayload(effective));
            }
        });
    }
}
