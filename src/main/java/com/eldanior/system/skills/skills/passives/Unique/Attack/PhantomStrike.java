package com.eldanior.system.skills.skills.passives.Unique.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
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
    private static final float MASTERY_BONUS = 0.10f; // +10% supplémentaire si maîtrisé

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;

        if (Math.random() < CHANCE) {
            proc = true;
            float multiplier = mastered ? BONUS_MULTIPLIER + MASTERY_BONUS : BONUS_MULTIPLIER;
            float currentDamage = damage.getAmount();
            float newDamage = currentDamage * multiplier;

            damage.setAmount(newDamage);

            String masteryTag = mastered ? " ★" : "";
            int percentBonus = mastered ? 60 : 50;
            LOGGER.atInfo().log("[Skill] PHANTOM_STRIKE : " + currentDamage + " -> " + newDamage + " (UNIQUE" + masteryTag + ")");

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:#b300ff><b>FRAPPE FANTÔME : +" + percentBonus + "% DÉGÂTS" + masteryTag + "</b></color>",
                            NotificationStyle.Success);
                }
            }
        }
        return proc;
    }
}