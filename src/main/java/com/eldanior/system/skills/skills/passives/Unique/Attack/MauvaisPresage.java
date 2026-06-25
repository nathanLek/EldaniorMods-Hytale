package com.eldanior.system.skills.skills.passives.Unique.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MauvaisPresage implements IPassiveCombatSkill {

    @Override
    public boolean onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || victimRef == null) return false;

        // 10% de chance de déclencher la malédiction
        if (Math.random() <= 0.15f) {

            EntityStatMap victimStatMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (victimStatMap == null) return false;

            // --- 1. CHOC TOXIQUE (Dégâts bonus basés sur les PV Max) ---
            EntityStatValue victimHealthStat = victimStatMap.get(StatConfig.VITALITY.getStatId());
            if (victimHealthStat != null) {
                float maxTargetHealth = victimHealthStat.getMax();
                float poisonDamage = maxTargetHealth * 0.15f; // 15% de la vie MAX
                damage.setAmount(damage.getAmount() + poisonDamage);
            }

            // --- 2. ÉPUISEMENT (Réduction de l'Endurance à 0) ---
            EntityStatValue enduranceStat = victimStatMap.get(StatConfig.ENDURANCE.getStatId());
            if (enduranceStat != null) {
                // On draine toute l'endurance de la cible instantanément
                victimStatMap.setStatValue(StatConfig.ENDURANCE.getStatId(), 0.0f);
            }

            // --- 3. EFFETS VISUELS & MESSAGES ---
            PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:green>Malédiction : Votre cible est vidée de son endurance !</color>",
                        NotificationStyle.Warning);
            }

            TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                // Une particule verte pour simuler le poison ou la corruption
                ParticleUtil.spawnParticleEffect("Poison_Splash", pos, store);
            }
        }
        return false;
    }
}