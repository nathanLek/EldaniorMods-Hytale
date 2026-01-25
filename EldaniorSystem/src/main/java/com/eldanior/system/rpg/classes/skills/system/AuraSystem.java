package com.eldanior.system.rpg.classes.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.ClassManager;
import com.eldanior.system.rpg.classes.ClassModel;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.eldanior.system.rpg.classes.skills.system.effects.EffectManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.*;

public class AuraSystem extends EntityTickingSystem<EntityStore> {

    // UUID Joueur -> (ID Skill -> Timestamp)
    private final Map<UUID, Map<String, Long>> activationTimes = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldownTimes = new HashMap<>();

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        timer += dt;
        if (timer < 0.2f) return;
        if (index == 0) timer = 0;

        Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
        if (!entityRef.isValid()) return;

        TransformComponent transform = store.getComponent(entityRef, TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d entityPos = transform.getPosition();

        Collection<PlayerRef> players = Universe.get().getPlayers();

        for (PlayerRef player : players) {
            var sourceRef = player.getReference();
            if (sourceRef == null || sourceRef.equals(entityRef) || !sourceRef.isValid()) continue;

            PlayerLevelData pData = store.getComponent(sourceRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (pData == null || pData.getPlayerClassId() == null) continue;

            ClassModel classModel = ClassManager.get(pData.getPlayerClassId());
            if (classModel == null) continue;

            // On itère sur les skills
            for (Object skillObj : classModel.getPassiveSkills()) {
                if (!(skillObj instanceof SkillModel skill)) continue;

                if (skill.getRadius() <= 0) continue;

                // CORRECTION : On passe pData ici !
                handleAuraLogic(player, sourceRef, entityRef, store, skill, entityPos, pData);
            }
        }
    }

    // CORRECTION : Ajout du paramètre PlayerLevelData pData dans la signature
    private void handleAuraLogic(PlayerRef player, Ref<EntityStore> sourceRef, Ref<EntityStore> targetRef,
                                 Store<EntityStore> store, SkillModel skill, Vector3d targetPos, PlayerLevelData pData) {

        UUID pid = player.getUuid();
        String skillId = skill.getId();
        long now = System.currentTimeMillis();

        activationTimes.putIfAbsent(pid, new HashMap<>());
        cooldownTimes.putIfAbsent(pid, new HashMap<>());

        // 1. Vérif Cooldown
        long cdEnd = cooldownTimes.get(pid).getOrDefault(skillId, 0L);
        if (now < cdEnd) return;

        // 2. Gestion Activation & Consommation
        long start = activationTimes.get(pid).getOrDefault(skillId, 0L);

        if (start == 0 || (now > cdEnd && start < cdEnd)) {
            // Tentative de démarrage : Vérifier les ressources
            if (consumeResource(sourceRef, store, skill)) {
                activationTimes.get(pid).put(skillId, now);
                EffectManager.sendBuffFeedback(player, "§a⚡ " + skill.getName() + " activée !");
                start = now;
            } else {
                return; // Pas assez de ressources pour activer
            }
        }

        // 3. Vérif Durée & Consommation continue
        long durationMs = (long) (skill.getActivationTime() * 1000);

        if (now > start + durationMs) {
            stopSkill(player, pid, skillId, now, skill.getCooldown());
            return;
        }

        // Consommation par tick (1s)
        if (!consumeResource(sourceRef, store, skill)) {
            stopSkill(player, pid, skillId, now, skill.getCooldown());
            return;
        }

        // --- APPLICATION DE L'EFFET ---
        TransformComponent sourceTrans = store.getComponent(sourceRef, TransformComponent.getComponentType());
        if (sourceTrans == null) return;

        // 1. EFFET VISUEL SUR LE LANCEUR (Le Dragon)
        // Maintenant pData est accessible ici !
        skill.onTick(sourceRef, store, pData);

        // 2. EFFETS SUR LES CIBLES
        double dist = EffectManager.getDistance(targetPos, sourceTrans.getPosition());
        if (dist <= skill.getRadius()) {
            skill.onAuraTick(sourceRef, targetRef, store, dist);
        }
    }

    /**
     * Gère la consommation automatique de Mana ou d'Endurance.
     */
    private boolean consumeResource(Ref<EntityStore> source, Store<EntityStore> store, SkillModel skill) {
        EntityStatMap statMap = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        // Note : Vérifie bien que "Dragon" est renvoyé par getResourceType() dans ton SkillModel si tu veux que ça marche pour le Dragon.
        // Sinon ça tombera sur "Endurance" par défaut (ce qui est correct pour ta classe Dragon aussi).
        int resourceIndex = skill.getResourceType().equals("Mana") || skill.getResourceType().equals("Dragon")
                ? DefaultEntityStatTypes.getMana()
                : DefaultEntityStatTypes.getStamina();

        EntityStatValue resource = statMap.get(resourceIndex);
        if (resource == null || resource.get() < skill.getResourceCostPerSecond()) return false;

        statMap.setStatValue(resourceIndex, resource.get() - skill.getResourceCostPerSecond());
        return true;
    }

    private void stopSkill(PlayerRef player, UUID pid, String skillId, long now, float cooldown) {
        activationTimes.get(pid).remove(skillId);
        cooldownTimes.get(pid).put(skillId, now + (long)(cooldown * 1000));
        EffectManager.sendCombatFeedback(player, "§7 Fin de compétence...", false);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TransformComponent.getComponentType(), EntityStatMap.getComponentType());
    }
}