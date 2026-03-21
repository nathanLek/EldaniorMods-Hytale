package com.eldanior.system.skills.skills.passives.Common.Chance;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MiracleDodge implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;

        // 3% de chance (0.03) d'esquiver miraculeusement
        if (Math.random() <= 0.03f) {
            damage.setCancelled(true); // On annule totalement les dégâts

            if (victimRef != null) {
                PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:yellow>Esquive Miraculeuse ! ✧</color>", NotificationStyle.Success);
                }
            }
        }
    }
}