package com.eldanior.system.skills.skills.passives.Divin.Ange;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skills.passives.Divin.PouvoirsDivins;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Charite (Gabriel, Eglise) : degats recus divises par 2,5 ; quand le porteur est touche, les membres blesses de son groupe
 * a moins de 15 blocs recuperent 3 % de leur vie max (une fois par seconde). */
public class Charite implements IPassiveCombatSkill {
    private static final Map<UUID, Long> LAST_HEAL = new ConcurrentHashMap<>();

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData data, Store<EntityStore> store, Ref<EntityStore> attackerRef,
                            Ref<EntityStore> victimRef, int rank, boolean mastered) {
        if (damage.isCancelled()) return false;
        damage.setAmount(damage.getAmount() / 2.5f);
        UUID self = PouvoirsDivins.uuid(victimRef, store);
        long now = System.currentTimeMillis();
        if (self != null && now - LAST_HEAL.getOrDefault(self, 0L) >= 1000L) {
            LAST_HEAL.put(self, now);
            for (Ref<EntityStore> ally : PouvoirsDivins.alliesNear(victimRef, store, 15)) PouvoirsDivins.healPercent(ally, store, 0.03f);
        }
        return true;
    }
}
