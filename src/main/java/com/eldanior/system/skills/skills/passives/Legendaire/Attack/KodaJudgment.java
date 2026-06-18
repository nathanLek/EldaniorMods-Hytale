package com.eldanior.system.skills.skills.passives.Legendaire.Attack;

import com.eldanior.system.EldaniorSystem;
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

public class KodaJudgment implements IPassiveCombatSkill {

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {
        if (damage.isCancelled() || attackerRef == null || victimRef == null) return;

        // --- 1. VÉRIFICATION DE LA VIE (Doit être à 100%) ---
        EntityStatMap victimStatMap = store.getComponent(victimRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (victimStatMap == null) return;

        EntityStatValue victimHealthStat = victimStatMap.get(StatConfig.VITALITY.getStatId());
        if (victimHealthStat == null) return;

        if (victimHealthStat.get() < victimHealthStat.getMax()) {
            return;
        }

        // --- 2. VÉRIFICATION DE L'ENDURANCE (Doit être >= 90%) ---
        EntityStatMap attackerStatMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        if (attackerStatMap == null) return;

        EntityStatValue enduranceStat = attackerStatMap.get(StatConfig.ENDURANCE.getStatId());
        if (enduranceStat == null) return;

        if (enduranceStat.get() < (enduranceStat.getMax() * 0.90f)) {
            return;
        }

        // --- 3. RÉCUPÉRATION DES NIVEAUX ---
        int attackerLevel = attackerData.getLevel();
        int targetLevel = 1;

        PlayerLevelData victimPlayerData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimPlayerData != null) {
            targetLevel = victimPlayerData.getLevel();
        } else {
            var victimMobData = store.getComponent(victimRef, EldaniorSystem.get().getMobLevelDataType());
            if (victimMobData != null) {
                targetLevel = victimMobData.getLevel();
            }
        }

        // --- 4. CALCUL DES PROBABILITÉS ---
        int levelDiff = attackerLevel - targetLevel;

        if (levelDiff <= -100) {
            return;
        }

        float chance = 0.12f;

        if (levelDiff < 0) {
            // Cible plus forte : la chance descend
            chance = 0.12f * (100f + levelDiff) / 100f;
        } else if (levelDiff > 0) {
            // Cible plus faible : la chance monte, mais plafonne à 33% maximum (0.33f)
            chance = Math.min(0.33f, 0.12f + (levelDiff * 0.01f));
        }

        // --- 5. EXÉCUTION ---
        if (Math.random() <= chance) {
            damage.setAmount(999999.0f);

            PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationHelper.sendNotification(playerRef,
                        "<color:gold>Jugement de Kodha ! Exécution instantanée !</color>",
                        NotificationStyle.Warning);
            }

            TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
            if (transform != null) {
                Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                ParticleUtil.spawnParticleEffect("Lightning_Strike_Gold", pos, store);
            }
        }
    }
}