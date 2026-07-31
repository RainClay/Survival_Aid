package com.survivalaid;

import com.survivalaid.features.NoCrammingEntitiesRule;
import com.survivalaid.features.StackingOptimizedEntitiesRule;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21.5-1.0.1.jar:com/survivalaid/SurvivalAidRules.class */
public final class SurvivalAidRules {
    private SurvivalAidRules() {
    }

    public static boolean ignoresEntityCramming(class_1297 entity) {
        return containsEntity(NoCrammingEntitiesRule.survivalAidNoCrammingEntities, entity);
    }

    public static boolean skipsStackingPush(class_1297 entity, class_1297 otherEntity) {
        return entity.method_5864() == otherEntity.method_5864() && containsEntity(StackingOptimizedEntitiesRule.survivalAidStackingOptimizedEntities, entity);
    }

    private static boolean containsEntity(String configuredEntities, class_1297 entity) {
        if (configuredEntities == null || configuredEntities.isBlank()) {
            return false;
        }
        class_2960 entityId = class_7923.field_41177.method_10221(entity.method_5864());
        for (String configuredEntity : configuredEntities.split(",")) {
            class_2960 configuredId = parseEntityId(configuredEntity.trim());
            if (configuredId != null && entityId.equals(configuredId)) {
                return true;
            }
        }
        return false;
    }

    private static class_2960 parseEntityId(String rawEntityId) {
        if (rawEntityId.isEmpty()) {
            return null;
        }
        class_2960 parsedId = class_2960.method_12829(rawEntityId);
        return (parsedId != null || rawEntityId.indexOf(58) >= 0) ? parsedId : class_2960.method_12829("minecraft:" + rawEntityId);
    }
}
