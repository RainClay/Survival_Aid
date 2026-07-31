package com.survivalaid.mixin;

import net.minecraft.class_2680;
import net.minecraft.class_8113;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.6-1.0.1.jar:com/survivalaid/mixin/BlockDisplayEntityAccessor.class */
@Mixin({class_8113.class_8115.class})
public interface BlockDisplayEntityAccessor {
    @Invoker("method_48883")
    void survivalAid$setBlockState(class_2680 class_2680Var);
}
