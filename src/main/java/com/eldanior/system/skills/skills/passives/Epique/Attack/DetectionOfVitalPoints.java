package com.eldanior.system.skills.skills.passives.Epique.Attack;

import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.skillsInteraction.IPassiveCombatSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;
import java.util.WeakHashMap;

public class DetectionOfVitalPoints implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Map<PlayerLevelData, Integer> hitCounters = new WeakHashMap<>();

    @Override // 🌟 Mis à jour avec les 5 arguments pour correspondre à ton interface
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> attackerRef, Ref<EntityStore> victimRef) {

        int currentHits = hitCounters.getOrDefault(attackerData, 0);
        currentHits++;

        // On vérifie si on atteint le 5ème coup
        if (currentHits >= 5) {
            hitCounters.put(attackerData, 0); // Reset le combo

            // --- APPLICATION DU COUP CRITIQUE ---
            float currentDamage = damage.getAmount();
            damage.setAmount(currentDamage * 2f); // Dégâts x2

            LOGGER.atInfo().log("[Skill] DETECTION_OF_VITAL_POINTS : Point vital touché (5ème coup) !");

            // --- EFFET VISUEL SUR LA CIBLE ---
            if (victimRef != null) {
                TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
                if (transform != null) {
                    Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                    // On utilise une particule d'impact puissante
                    ParticleUtil.spawnParticleEffect("VFX_Crit_Hit", pos, store);
                }
            }

            // --- NOTIFICATION POUR L'ATTAQUANT ---
            // 🌟 On utilise directement attackerRef passé en argument !
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:red>☠️ Point Vital : Dégâts Doublés !</color>", NotificationStyle.Warning);
                }
            }

        } else {
            hitCounters.put(attackerData, currentHits);

            // Optionnel : Envoyer une petite notification de progression du combo
            if (attackerRef != null) {
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:yellow>Combo : " + currentHits + "/5</color>", NotificationStyle.Success);
                }
            }
        }
    }
}