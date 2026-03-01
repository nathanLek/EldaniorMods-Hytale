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

public class DeepSlash implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float FLAT_BONUS = 1.0f;

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        float originalDamage = damage.getAmount();
        float newDamage = originalDamage + FLAT_BONUS;

        damage.setAmount(newDamage);

        if (attackerRef != null) {
            PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef, "<color:white>Entaille Profonde : +1 dégâts</color>", NotificationStyle.Success);
            }
        }

        // Log discret en Info pour ne pas flood la console, mais garder une trace du calcul
        LOGGER.atInfo().log("[Skill] DEEP_SLASH : " + originalDamage + " -> " + newDamage + " (+1)");
    }
}