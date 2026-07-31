package com.survivalaid;

import com.survivalaid.features.TntLikeBlocksRule;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1540;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.server.MinecraftServer;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/SurvivalAidTntLikeBlocks.class */
public final class SurvivalAidTntLikeBlocks {
    private static final int FUSE_TICKS = 80;
    private static final float EXPLOSION_POWER = 4.0f;
    private static final Map<UUID, PrimedBlock> PRIMED_BLOCKS = new LinkedHashMap();

    private SurvivalAidTntLikeBlocks() {
    }

    public static boolean prime(class_1937 world, class_2338 pos) {
        return prime(world, pos, null);
    }

    public static boolean prime(class_1937 world, class_2338 pos, class_1657 player) {
        if (!TntLikeBlocksRule.survivalAidTntLikeBlocks || world.method_8608()) {
            return false;
        }
        class_2680 state = world.method_8320(pos);
        if (state.method_26215()) {
            return false;
        }
        class_1540 entity = class_1540.method_40005(world, pos, state);
        entity.field_7193 = false;
        entity.method_5875(true);
        entity.method_56073(FUSE_TICKS);
        entity.method_18800(0.0d, 0.0d, 0.0d);
        PRIMED_BLOCKS.put(entity.method_5667(), new PrimedBlock(entity, FUSE_TICKS));
        return true;
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, PrimedBlock>> iterator = PRIMED_BLOCKS.entrySet().iterator();
        while (iterator.hasNext()) {
            PrimedBlock primedBlock = iterator.next().getValue();
            if (primedBlock.entity.method_31481()) {
                iterator.remove();
            } else {
                primedBlock.entity.method_5875(true);
                primedBlock.entity.method_18800(0.0d, 0.0d, 0.0d);
                primedBlock.entity.method_56073(Math.max(1, primedBlock.fuse));
                primedBlock.fuse--;
                if (primedBlock.fuse <= 0) {
                    class_1937 world = primedBlock.entity.method_37908();
                    world.method_8437(primedBlock.entity, primedBlock.entity.method_23317(), primedBlock.entity.method_23318(), primedBlock.entity.method_23321(), EXPLOSION_POWER, class_1937.class_7867.field_40891);
                    primedBlock.entity.method_31472();
                    iterator.remove();
                }
            }
        }
    }

    /* JADX INFO: loaded from: carpet-survival-aid-mc1.21.1-1.0.1.jar:com/survivalaid/SurvivalAidTntLikeBlocks$PrimedBlock.class */
    private static final class PrimedBlock {
        private final class_1540 entity;
        private int fuse;

        private PrimedBlock(class_1540 entity, int fuse) {
            this.entity = entity;
            this.fuse = fuse;
        }
    }
}
