package com.survivalaid.mixin;

import com.survivalaid.SurvivalAidRuntime;
import com.survivalaid.features.WorkstationHighLightRule;
import java.util.Optional;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1299;
import net.minecraft.class_1646;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_4140;
import net.minecraft.class_4208;
import net.minecraft.class_8113;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/mixin/VillagerEntityMixin.class */
@Mixin({class_1646.class})
public abstract class VillagerEntityMixin {
    @Inject(method = {"method_5992"}, at = {@At("HEAD")}, cancellable = true)
    private void survivalAid$highlightWorkstation(class_1657 player, class_1268 hand, CallbackInfoReturnable<class_1269> cir) {
        if (!WorkstationHighLightRule.survivalAidWorkstationHighLight || !player.method_5715()) {
            return;
        }
        class_1646 villager = (class_1646) this;
        class_3218 class_3218VarMethod_37908 = villager.method_37908();
        if (!(class_3218VarMethod_37908 instanceof class_3218)) {
            return;
        }
        class_3218 serverWorld = class_3218VarMethod_37908;
        Optional<class_4208> jobSite = villager.method_18868().method_46873(class_4140.field_18439);
        if (jobSite.isEmpty()) {
            return;
        }
        class_2338 workstation = jobSite.get().comp_2208();
        class_2680 blockState = serverWorld.method_8320(workstation);
        BlockDisplayEntityAccessor class_8115Var = new class_8113.class_8115(class_1299.field_42460, serverWorld);
        class_8115Var.method_5814(workstation.method_10263(), workstation.method_10264(), workstation.method_10260());
        class_8115Var.survivalAid$setBlockState(blockState);
        class_8115Var.method_5834(true);
        class_8115Var.method_5875(true);
        class_8115Var.method_5684(true);
        class_8115Var.method_5803(true);
        serverWorld.method_8649(class_8115Var);
        SurvivalAidRuntime.trackWorkstationHighlight(class_8115Var);
        cir.setReturnValue(class_1269.field_5812);
    }
}
