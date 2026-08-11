package com.survivalaid;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

public class SurvivalAidClient implements ClientModInitializer {
    private static String lastSent = null;
    private int tickCounter = 0;
    private static Map<String, String> nameToIdCache = new HashMap<>();
    private static boolean nameMapBuilt = false;

    private static String resolveItemId(String name) {
        if (!nameMapBuilt) {
            Map<String, String> m = new HashMap<>();
            for (Item item : Registries.ITEM) {
                try {
                    String display = item.getName().getString();
                    if (display != null && !display.isEmpty()) {
                        m.putIfAbsent(display, Registries.ITEM.getId(item).toString());
                    }
                } catch (Exception ignored) {
                }
            }
            nameToIdCache = m;
            nameMapBuilt = true;
        }
        return nameToIdCache.get(name);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SearchItemRequestPayload.TYPE, (payload, context) -> {
            String id = resolveItemId(payload.name());
            ClientPlayNetworking.send(new SearchItemResultPayload(id == null ? "" : id));
        });
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
            ProjectionClientState st = ProjectionNameProvider.getCurrentProjectionState();
            String effective = st == null ? "" : (st.name + "\u0001" + st.rangeType + "\u0001" + st.minY + "\u0001" + st.maxY + "\u0001" + st.originX + "\u0001" + st.originY + "\u0001" + st.originZ);
            if (!effective.equals(lastSent)) {
                lastSent = effective;
                ClientPlayNetworking.send(new SurvivalAidProjectionPayload(effective));
            }
        });
    }
}
