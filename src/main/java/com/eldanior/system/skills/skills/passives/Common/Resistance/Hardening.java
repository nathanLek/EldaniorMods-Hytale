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

public class Hardening implements IPassiveCombatSkill {

    private static final float REDUCTION = 0.96f;
    private static final float REDUCTION_MASTERED = 0.955f;

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled()) return proc;

        float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
        damage.setAmount(damage.getAmount() * mult);
        proc = true;

        if (victimRef != null) {
            PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:yellow>Endurcissement : -" + (int)((1f - mult) * 100) + "% de dégâts</color>", NotificationStyle.Success);
            }
        }
        return proc;
    }
}
