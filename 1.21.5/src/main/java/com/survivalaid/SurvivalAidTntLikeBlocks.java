package com.survivalaid;

import com.survivalaid.features.TntLikeBlocksRule;
import com.survivalaid.mixin.FallingBlockEntityMixin;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

public final class SurvivalAidTntLikeBlocks {
    private static final int FUSE_TICKS = 80;
    private static final float EXPLOSION_POWER = 4.0f;
    private static final Map<UUID, PrimedBlock> PRIMED_BLOCKS = new LinkedHashMap<>();

    private SurvivalAidTntLikeBlocks() {
    }

    public static boolean prime(World world, BlockPos pos) {
        return prime(world, pos, null);
    }

    public static boolean prime(World world, BlockPos pos, PlayerEntity player) {
        if (!TntLikeBlocksRule.survivalAidTntLikeBlocks || world.isClient()) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        FallingBlockEntity entity = FallingBlockEntity.spawnFromBlock(world, pos, state);
        entity.dropItem = false;
        entity.setNoGravity(true);
        ((FallingBlockEntityMixin) entity).survivalAid$setFuse(FUSE_TICKS);
        entity.setVelocity(0.0d, 0.0d, 0.0d);
        PRIMED_BLOCKS.put(entity.getUuid(), new PrimedBlock(entity, FUSE_TICKS));
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
                primedBlock.entity.setVelocity(0.0d, 0.0d, 0.0d);
                ((FallingBlockEntityMixin) primedBlock.entity).survivalAid$setFuse(Math.max(1, primedBlock.fuse));
                primedBlock.fuse--;
                if (primedBlock.fuse <= 0) {
                    World world = primedBlock.entity.getWorld();
                    world.createExplosion(primedBlock.entity, primedBlock.entity.getX(), primedBlock.entity.getY(), primedBlock.entity.getZ(), EXPLOSION_POWER, false, ExplosionSourceType.BLOCK);
                    primedBlock.entity.kill((ServerWorld) world);
                    iterator.remove();
                }
            }
        }
    }

    private static final class PrimedBlock {
        private final FallingBlockEntity entity;
        private int fuse;

        private PrimedBlock(FallingBlockEntity entity, int fuse) {
            this.entity = entity;
            this.fuse = fuse;
        }
    }
}
