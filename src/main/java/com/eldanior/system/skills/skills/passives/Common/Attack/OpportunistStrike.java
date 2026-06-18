package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpportunistStrike implements IPassiveCombatSkill {

    private static final float BONUS = 1.12f;
    private static final float BONUS_MASTERED = 1.132f;
    private static final long WINDOW_MS = 2000;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;

        long timeSinceHit = System.currentTimeMillis() - attackerData.getLastDamageTakenTime();
        if (timeSinceHit <= WINDOW_MS) {
            lastProc = true;
            float multiplier = mastered ? BONUS_MASTERED : BONUS;
            damage.setAmount(damage.getAmount() * multiplier);

            int percent = mastered ? 13 : 12;
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Frappe Opportuniste : +" + percent + "% dégâts</color>", NotificationStyle.Success);
                }
            }
        }
    }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        victimData.setLastDamageTakenTime(System.currentTimeMillis());
    }
}
