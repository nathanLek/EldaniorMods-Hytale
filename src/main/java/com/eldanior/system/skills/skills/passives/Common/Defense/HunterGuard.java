package com.eldanior.system.skills.skills.passives.Common.Defense;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HunterGuard implements IPassiveCombatSkill {

    @Override
    public void onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || !attackerRef.isValid()) return;

        // On vérifie si l'attaquant possède le composant "Mob" (donc ce n'est pas un joueur)
        boolean isMob = store.getComponent(attackerRef, EldaniorSystem.get().getMobLevelDataType()) != null;

        if (isMob) {
            // -10% de dégâts contre les monstres
            float newDamage = damage.getAmount() * 0.90f;
            damage.setAmount(newDamage);
        }
    }
}