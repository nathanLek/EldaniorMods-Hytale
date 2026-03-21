package com.eldanior.system.skills.skills.passives.Common.Defense;

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

public class IronResolve implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final float FLAT_REDUCTION = 3.0f;

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled()) return;

        float currentDamage = damage.getAmount();
        float newDamage = currentDamage - FLAT_REDUCTION;

        // On s'assure que les dégâts ne tombent pas en dessous de 1 (on n'est pas invincible !)
        if (newDamage < 1.0f) newDamage = 1.0f;

        damage.setAmount(newDamage);

        // On ne spamme pas les notifications pour une réduction fixe, on le garde juste dans les logs (ou on l'enlève pour l'optimisation)
        // LOGGER.atInfo().log("[Skill] IRON_RESOLVE activé : -" + FLAT_REDUCTION + " dégâts.");
    }
}