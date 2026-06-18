package com.eldanior.system.skills.skills.passives.Common.Resistance;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PainTolerance implements IPassiveCombatSkill {

    private static final float FLAT_REDUCTION = 2.0f;
    private static final float FLAT_REDUCTION_MASTERED = 2.5f;

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        float reduction = mastered ? FLAT_REDUCTION_MASTERED : FLAT_REDUCTION;
        float currentDamage = damage.getAmount();
        float newDamage = Math.max(1.0f, currentDamage - reduction);
        damage.setAmount(newDamage);
        proc = true;

        if (victimRef != null) {
            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:yellow>Tolérance : -" + String.format("%.1f", reduction) + " dégâts</color>", NotificationStyle.Success);
            }
        }
        return proc;
    }
}
