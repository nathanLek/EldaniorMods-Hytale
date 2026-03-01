package com.eldanior.system.skills.skills.passives;

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

// 🌟 Import pour notre système de mémorisation des coups
import java.util.Map;
import java.util.WeakHashMap;

public class DetectionOfVitalPoints implements IPassiveCombatSkill {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Ce "dictionnaire" retient le nombre de coups de chaque joueur.
    // WeakHashMap est génial : si le joueur quitte le serveur, sa donnée s'efface toute seule !
    private final Map<PlayerLevelData, Integer> hitCounters = new WeakHashMap<>();

    @Override
    public void onAttack(Damage damage, PlayerLevelData attackerData, Store<EntityStore> store, Ref<EntityStore> victimRef) {

        // 1. On récupère le nombre de coups actuels du joueur (0 si c'est son premier coup)
        int currentHits = hitCounters.getOrDefault(attackerData, 0);

        // On ajoute le coup qu'il vient de donner
        currentHits++;

        // 2. Si on atteint le 3ème coup
        if (currentHits >= 5) {

            // On remet son compteur à Zéro pour le prochain combo !
            hitCounters.put(attackerData, 0);

            // --- 🌟 APPLICATION DU COUP CRITIQUE ---
            float currentDamage = damage.getAmount();
            damage.setAmount(currentDamage * 2f); // x1.5 dégâts (ou plus selon ton envie !)

            LOGGER.atInfo().log("[Skill] DETECTION_OF_VITAL_POINTS activé ! Point vital touché (3ème coup) !");

            // --- EFFET VISUEL SUR LA CIBLE ---
            if (victimRef != null) {
                TransformComponent transform = store.getComponent(victimRef, TransformComponent.getComponentType());
                if (transform != null) {
                    Vector3d pos = transform.getPosition().add(0, 1.0, 0);
                    ParticleUtil.spawnParticleEffect("Critical_Hit", pos, store);
                }
            }

            // --- NOTIFICATION POUR L'ATTAQUANT ---
            Damage.Source source = damage.getSource();
            if (source instanceof Damage.EntitySource entitySource) {
                Ref<EntityStore> attackerRef = entitySource.getRef();
                PlayerRef playerRef = store.getComponent(attackerRef, PlayerRef.getComponentType());

                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:red>Point Vital : Coup Critique !</color>", NotificationStyle.Warning);
                }
            }

        } else {
            // Si ce n'est pas encore le 3ème coup, on sauvegarde simplement son nouveau total
            hitCounters.put(attackerData, currentHits);
        }
    }
}