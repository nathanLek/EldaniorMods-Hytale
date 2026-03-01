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
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.UUID;

public class HauntingThrust implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        // 1. Récupérer l'UUID de la victime
        UUIDComponent victimUUIDComp = store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;
        UUID currentVictimUUID = victimUUIDComp.getUuid();

        // 2. Vérifier si c'est la même cible
        if (currentVictimUUID.equals(attackerData.getLastVictimUUID())) {

            int stacks = attackerData.getHauntingThrustStacks();
            if (stacks < 5) {
                stacks++;
                attackerData.setHauntingThrustStacks(stacks);
            }

            float bonusMultiplier = 1.0f + (stacks * 0.03f);
            damage.setAmount(damage.getAmount() * bonusMultiplier);

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Estocade Obsédante : (+ " + (stacks * 3) + "%) dégâts</color>", NotificationStyle.Success);
                }
            }

            LOGGER.atInfo().log("[Skill] Estocade Obsédante : Stack " + stacks + " (+" + (stacks * 3) + "%)");

        } else {
            // Nouvelle cible : on reset les stacks à 1
            attackerData.setLastVictimUUID(currentVictimUUID);
            attackerData.setHauntingThrustStacks(1);
            // On applique quand même le premier stack de 3% ?
            // Ou on reste à 0% pour le premier coup, à toi de choisir l'équilibrage !
        }
    }
}