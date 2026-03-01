package com.eldanior.system.skills.skills.passives.Common.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;

import java.util.Objects;


public class PressurePoint implements IPassiveCombatSkill {

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        // 1. On récupère les stats de la VICTIME
        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return;

        // 2. On vérifie sa vie actuelle et max
        float currentHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).get();
        float maxHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).getMax();

        // 3. Si la cible est à plus de 90% de sa vie
        if (currentHealth >= (maxHealth * 0.90f)) {
            float oldDamage = damage.getAmount();
            damage.setAmount(oldDamage * 1.15f); // +15% de bonus

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Point de Pression : +15% dégâts</color>", NotificationStyle.Success);
                }
            }
        }
    }
}