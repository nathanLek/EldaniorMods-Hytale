package com.eldanior.system.skills.skills.passives.Unique.Attack;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;

public class SoulCrushingPressure implements IPassiveCombatSkill {
    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return false;
        float currentHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).get();
        float maxHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).getMax();
        if (currentHealth >= (maxHealth * 0.90f)) {
            damage.setAmount(damage.getAmount() * 1.55f);
        }
        return false;
    }
}