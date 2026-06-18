package com.eldanior.system.skills.skills.passives.Common.Attack;

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

import java.util.Objects;

public class PressurePoint implements IPassiveCombatSkill {

    private static final float BONUS = 1.15f;
    private static final float BONUS_MASTERED = 1.165f;
    private static final float HP_THRESHOLD = 0.90f;

    private boolean lastProc = false;

    @Override
    public boolean didProc() { return lastProc; }

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef, boolean mastered) {
        lastProc = false;

        EntityStatMap victimStats = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStats == null) return;

        float currentHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).get();
        float maxHealth = Objects.requireNonNull(victimStats.get(DefaultEntityStatTypes.getHealth())).getMax();

        if (currentHealth >= (maxHealth * HP_THRESHOLD)) {
            lastProc = true;
            float multiplier = mastered ? BONUS_MASTERED : BONUS;
            damage.setAmount(damage.getAmount() * multiplier);

            int percent = mastered ? 16 : 15;
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:white>Point de Pression : +" + percent + "% dégâts</color>", NotificationStyle.Success);
                }
            }
        }
    }
}
