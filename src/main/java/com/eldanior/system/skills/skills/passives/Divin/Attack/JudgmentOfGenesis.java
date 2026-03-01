package com.eldanior.system.skills.skills.passives.Divin.Attack;

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

public class JudgmentOfGenesis implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float CHANCE = 0.30f;
    private static final float DIVINE_MULTIPLIER = 2.50f; // +150% dégâts (x3)

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        if (Math.random() < CHANCE) {
            float currentDamage = damage.getAmount();
            float finalDamage = currentDamage * DIVINE_MULTIPLIER;

            // On applique les dégâts massifs directement
            damage.setAmount(finalDamage);

            LOGGER.atInfo().log("[Skill] JUDGMENT_OF_GENESIS : Puissance Divine libérée ! " + currentDamage + " -> " + finalDamage);

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:#ffff00><b> DÉCRET DE LA GENÈSE : +150% de dégâts </b></color>",
                            NotificationStyle.Success);
                }
            }
        }
    }
}