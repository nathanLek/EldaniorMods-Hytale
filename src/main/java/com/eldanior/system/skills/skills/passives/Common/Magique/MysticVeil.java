package com.eldanior.system.skills.skills.passives.Common.Magique;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MysticVeil implements IPassiveCombatSkill {

    private static final float CHANCE = 0.10f;
    private static final float REDUCTION = 0.75f;
    private static final float REDUCTION_MASTERED = 0.725f;
    private static final float MANA_COST = 20.0f;

    @Override
    public boolean onDefend(Damage damage, PlayerLevelData victimData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled() || victimRef == null) return proc;

        if (Math.random() <= CHANCE) {
            EntityStatMap statMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                float currentMana = statMap.get(DefaultEntityStatTypes.getMana()).get();
                if (currentMana >= MANA_COST) {
                    statMap.setStatValue(DefaultEntityStatTypes.getMana(), currentMana - MANA_COST);
                    float mult = mastered ? REDUCTION_MASTERED : REDUCTION;
                    damage.setAmount(damage.getAmount() * mult);
                    proc = true;

                    if (attackerRef != null) {
                        PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                        if (playerRef != null) {
                            NotificationHelper.sendNotification(playerRef,
                                    "<color:purple>Voile Mystic: -25% de degats </color>", NotificationStyle.Success);
                        }
                    }
                }
            }
        }
        return proc;
    }
}