package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InstinctiveStrike implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float CHANCE = 0.10f;
    private static final float BONUS_MULTIPLIER = 1.10f;
    private static final float BONUS_MULTIPLIER_MASTERED = 1.11f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;

        if (Math.random() < CHANCE) {
            proc = true;
            float multiplier = mastered ? BONUS_MULTIPLIER_MASTERED : BONUS_MULTIPLIER;
            float currentDamage = damage.getAmount();
            float newDamage = currentDamage * multiplier;
            damage.setAmount(newDamage);

            int percent = mastered ? 11 : 10;
            LOGGER.atInfo().log("[Skill] INSTINCTIVE_STRIKE : " + currentDamage + " -> " + newDamage);

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Frappe Instinctive : +" + percent + "% dégâts</color>", NotificationStyle.Success);
                }
            }
        }
        return proc;
    }
}
