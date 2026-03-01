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

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        long lastTimeTakenDamage = attackerData.getLastDamageTakenTime();
        long currentTime = System.currentTimeMillis();

        // Si le dernier dégât reçu date de moins de 2000ms (2 secondes)
        if (currentTime - lastTimeTakenDamage <= 2000) {
            float currentDmg = damage.getAmount();
            damage.setAmount(currentDmg * 1.12f);

            // On peut reset pour éviter que le bonus s'applique sur 50 coups en 2 sec
            // attackerData.setLastDamageTakenTime(0);
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Frappe Opportuniste : +12% dégâts</color>", NotificationStyle.Success);
                }
            }
        }
    }

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        // C'est ici qu'on enregistre le moment où le joueur prend un coup !
        victimData.setLastDamageTakenTime(System.currentTimeMillis());
    }
}