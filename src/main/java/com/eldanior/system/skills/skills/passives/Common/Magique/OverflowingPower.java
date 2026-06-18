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

public class OverflowingPower implements IPassiveCombatSkill {

    private static final float THRESHOLD = 0.85f;
    private static final float BONUS = 1.15f;
    private static final float BONUS_MASTERED = 1.165f;

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        boolean proc = false;
        if (damage.isCancelled() || attackerRef == null) return proc;

        EntityStatMap statMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return proc;

        float currentMana = statMap.get(DefaultEntityStatTypes.getMana()).get();
        float maxMana = statMap.get(DefaultEntityStatTypes.getMana()).getMax();

        if (currentMana >= (maxMana * THRESHOLD)) {
            float mult = mastered ? BONUS_MASTERED : BONUS;
            damage.setAmount(damage.getAmount() * mult);
            proc = true;

            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef,
                            "<color:purple>Puissance Débordante: +15% de degats </color>", NotificationStyle.Success);
                }
            }
        }
        return proc;
    }
}