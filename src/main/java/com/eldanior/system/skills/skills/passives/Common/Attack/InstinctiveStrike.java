package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InstinctiveStrike implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float CHANCE = 0.05f;
    private static final float BONUS_MULTIPLIER = 1.10f;

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        // Vérification de chance (15%)
        if (Math.random() < CHANCE) {
            float currentDamage = damage.getAmount();
            float newDamage = currentDamage * BONUS_MULTIPLIER;
            damage.setAmount(newDamage);

            LOGGER.atInfo().log("[Skill] INSTINCTIVE_STRIKE déclenché ! " + currentDamage + " -> " + newDamage);

            // On utilise attackerRef (celui qui frappe) pour envoyer la notification
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Frappe Instinctive : +10% dégâts</color>", NotificationStyle.Success);
                }

                // Effet visuel sur l'attaquant
                TransformComponent transform = store.getComponent(attackerRef, TransformComponent.getComponentType());
                if (transform != null) {
                    Vector3d pos = transform.getPosition().add(0, 1.5, 0);
                    // Utilisation d'une particule existante (ex: VFX_Crit_Hit ou Shield_Block pour test)
                    ParticleUtil.spawnParticleEffect("VFX_Crit_Hit", pos, store);
                }
            }
        }
    }
}