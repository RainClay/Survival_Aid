package com.survivalaid;

import com.survivalaid.features.NoCrammingEntitiesRule;
import com.survivalaid.features.StackingOptimizedEntitiesRule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

/* JADX INFO: loaded from: carpet-survival-aid-mc1.21-1.0.1.jar:com/survivalaid/SurvivalAidRules.class */
public final class SurvivalAidRules {
    private SurvivalAidRules() {
    }

    public static boolean ignoresEntityCramming(Entity entity) {
        return containsEntity(NoCrammingEntitiesRule.survivalAidNoCrammingEntities, entity);
    }

    public static boolean skipsStackingPush(Entity entity, Entity otherEntity) {
        return entity.getType() == otherEntity.getType() && containsEntity(StackingOptimizedEntitiesRule.survivalAidStackingOptimizedEntities, entity);
    }

    private static boolean containsEntity(String configuredEntities, Entity entity) {
        if (configuredEntities == null || configuredEntities.isBlank()) {
            return false;
        }
        Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
        for (String configuredEntity : configuredEntities.split(",")) {
            Identifier configuredId = parseEntityId(configuredEntity.trim());
            if (configuredId != null && entityId.equals(configuredId)) {
                return true;
            }
        }
        return false;
    }

    private static Identifier parseEntityId(String rawEntityId) {
        if (rawEntityId.isEmpty()) {
            return null;
        }
        Identifier parsedId = Identifier.tryParse(rawEntityId);
        return (parsedId != null || rawEntityId.indexOf(':') >= 0) ? parsedId : Identifier.tryParse("minecraft:" + rawEntityId);
    }
}
