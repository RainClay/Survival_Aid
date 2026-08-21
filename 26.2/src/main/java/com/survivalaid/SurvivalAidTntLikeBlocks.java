package com.survivalaid;

import com.survivalaid.features.TntLikeBlocksRule;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidTntLikeBlocks.class */
public final class SurvivalAidTntLikeBlocks {
    private static final int FUSE_TICKS = 80;
    private static final float EXPLOSION_POWER = 4.0f;
    private static final Map<UUID, PrimedBlock> PRIMED_BLOCKS = new LinkedHashMap();

    private SurvivalAidTntLikeBlocks() {
    }

    public static boolean prime(Level world, BlockPos pos) {
        return prime(world, pos, null);
    }

    public static boolean prime(Level world, BlockPos pos, Player player) {
        if (!TntLikeBlocksRule.survivalAidTntLikeBlocks || world.isClientSide()) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        FallingBlockEntity entity = FallingBlockEntity.fall(world, pos, state);
        entity.dropItem = false;
        entity.setNoGravity(true);
        entity.setRemainingFireTicks(FUSE_TICKS);
        entity.setDeltaMovement(0.0d, 0.0d, 0.0d);
        PRIMED_BLOCKS.put(entity.getUUID(), new PrimedBlock(entity, FUSE_TICKS));
        return true;
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, PrimedBlock>> iterator = PRIMED_BLOCKS.entrySet().iterator();
        while (iterator.hasNext()) {
            PrimedBlock primedBlock = iterator.next().getValue();
            if (primedBlock.entity.isRemoved()) {
                iterator.remove();
            } else {
                primedBlock.entity.setNoGravity(true);
                primedBlock.entity.setDeltaMovement(0.0d, 0.0d, 0.0d);
                primedBlock.entity.setRemainingFireTicks(Math.max(1, primedBlock.fuse));
                primedBlock.fuse--;
                if (primedBlock.fuse <= 0) {
                    primedBlock.entity.level().explode(primedBlock.entity, (DamageSource) null, (ExplosionDamageCalculator) null, primedBlock.entity.getX(), primedBlock.entity.getY(), primedBlock.entity.getZ(), EXPLOSION_POWER, true, Level.ExplosionInteraction.TNT, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, WeightedList.of(new ExplosionParticleInfo(ParticleTypes.EXPLOSION, 1.0f, 1.0f)), SoundEvents.GENERIC_EXPLODE);
                    primedBlock.entity.discard();
                    iterator.remove();
                }
            }
        }
    }

    /* JADX INFO: loaded from: carpet-survival-aid-mc26.1.2-Carpet-SurvivalAid-1.0.1.jar:com/survivalaid/SurvivalAidTntLikeBlocks$PrimedBlock.class */
    private static final class PrimedBlock {
        private final FallingBlockEntity entity;
        private int fuse;

        private PrimedBlock(FallingBlockEntity entity, int fuse) {
            this.entity = entity;
            this.fuse = fuse;
        }
    }
}
