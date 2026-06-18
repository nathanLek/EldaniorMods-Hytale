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
    private static final float STACK_BONUS = 0.03f;           // +3% par stack
    private static final float STACK_BONUS_MASTERED = 0.033f;  // +3.3% par stack si maîtrisé
    private static final int MAX_STACKS = 5;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;

        UUIDComponent victimUUIDComp = store.getComponent(victimRef, UUIDComponent.getComponentType());
        if (victimUUIDComp == null) return;
        UUID currentVictimUUID = victimUUIDComp.getUuid();

        float bonus = mastered ? STACK_BONUS_MASTERED : STACK_BONUS;

        if (currentVictimUUID.equals(attackerData.getLastVictimUUID())) {
            int stacks = attackerData.getHauntingThrustStacks();
            if (stacks < MAX_STACKS) {
                stacks++;
                attackerData.setHauntingThrustStacks(stacks);
            }

            float bonusMultiplier = 1.0f + (stacks * bonus);
            damage.setAmount(damage.getAmount() * bonusMultiplier);
            lastProc = true;

            int percentDisplay = (int)(stacks * bonus * 100);
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Estocade Obsédante : (+" + percentDisplay + "%) dégâts</color>", NotificationStyle.Success);
                }
            }
            LOGGER.atInfo().log("[Skill] Estocade Obsédante : Stack " + stacks + " (+" + percentDisplay + "%)");
        } else {
            attackerData.setLastVictimUUID(currentVictimUUID);
            attackerData.setHauntingThrustStacks(1);
        }
    }
}
