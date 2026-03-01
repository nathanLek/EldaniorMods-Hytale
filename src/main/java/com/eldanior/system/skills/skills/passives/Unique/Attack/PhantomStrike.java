package com.eldanior.system.skills.skills.passives.Unique.Attack;

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

public class PhantomStrike implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float CHANCE = 0.18f; // 18% de chance
    private static final float BONUS_MULTIPLIER = 1.50f; // +50% dégâts

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        if (Math.random() < CHANCE) {
            float currentDamage = damage.getAmount();
            float newDamage = currentDamage * BONUS_MULTIPLIER;

            // On applique les dégâts augmentés
            damage.setAmount(newDamage);

            LOGGER.atInfo().log("[Skill] PHANTOM_STRIKE : " + currentDamage + " -> " + newDamage + " (UNIQUE)");

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:#b300ff><b>✧ FRAPPE FANTÔME : +50% DÉGÂTS ✧</b></color>",
                            NotificationStyle.Success);
                }
            }
        }
    }
}