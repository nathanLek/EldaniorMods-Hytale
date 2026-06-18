package com.eldanior.system.skills.skills.passives.Common.Magique;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ArcaneStrike implements IPassiveCombatSkill {

    private static final float CHANCE = 0.20f;
    private static final float BONUS = 8.0f;
    private static final float BONUS_MASTERED = 8.8f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled() || attackerRef == null) return proc;

        if (Math.random() <= CHANCE) {
            float bonus = mastered ? BONUS_MASTERED : BONUS;
            damage.setAmount(damage.getAmount() + bonus);
            proc = true;

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:purple>Frappe Arcanique: +8 de degats </color>", NotificationStyle.Success);
                }
            }
        }
        return proc;
    }
}