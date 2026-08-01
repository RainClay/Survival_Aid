package com.survivalaid.mixin;

import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityMixin {
    @Accessor("fuse")
    void survivalAid$setFuse(int fuse);
}
