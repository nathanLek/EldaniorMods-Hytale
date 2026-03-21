package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MinorParry implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;

        // 15% de chance de déclencher la parade
        if (Math.random() <= 0.15f) {
            float newDamage = damage.getAmount() * 0.85f; // Réduit les dégâts de 30%
            damage.setAmount(newDamage);

            if (victimRef != null) {
                PlayerRef playerRef = store.getComponent(victimRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:yellow>Parade réussie ! (-15%)</color>", NotificationStyle.Success);
                }

                // Particule d'étincelles pour signifier le choc des armes
                TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
                if (transform != null) {
                    Vector3d pos = transform.getPosition().add(0, 1.2, 0);
                    ParticleUtil.spawnParticleEffect("Hit_Sparks", pos, store);
                }
            }
        }
    }
}