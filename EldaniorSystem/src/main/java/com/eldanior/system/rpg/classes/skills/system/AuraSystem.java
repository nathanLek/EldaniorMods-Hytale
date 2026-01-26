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

    // UUID Joueur -> (ID Skill -> Timestamp début)
    private final Map<UUID, Map<String, Long>> activationTimes = new HashMap<>();
    // UUID Joueur -> (ID Skill -> Timestamp fin cooldown)
    private final Map<UUID, Map<String, Long>> cooldownTimes = new HashMap<>();
    // UUID Joueur -> (ID Skill -> Timestamp prochaine consommation de mana)
    private final Map<UUID, Map<String, Long>> nextResourceTick = new HashMap<>();

    private float timer = 0;

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        timer += dt;
        // On garde un tick rate assez rapide (0.2s) pour que les dégâts soient réactifs
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
            // On évite que le joueur se tape lui-même
            if (sourceRef == null || sourceRef.equals(entityRef) || !sourceRef.isValid()) continue;

            PlayerLevelData pData = store.getComponent(sourceRef, EldaniorSystem.get().getPlayerLevelDataType());
            if (pData == null || pData.getPlayerClassId() == null) continue;

            ClassModel classModel = ClassManager.get(pData.getPlayerClassId());
            if (classModel == null) continue;

            for (Object skillObj : classModel.getPassiveSkills()) {
                if (!(skillObj instanceof SkillModel skill)) continue;

                if (skill.getRadius() <= 0) continue;

                handleAuraLogic(player, sourceRef, entityRef, store, commandBuffer, skill, entityPos, pData);
            }
        }
    }

    private void handleAuraLogic(PlayerRef player, Ref<EntityStore> sourceRef, Ref<EntityStore> targetRef,
                                 Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer, SkillModel skill, Vector3d targetPos, PlayerLevelData pData) {

        UUID pid = player.getUuid();
        String skillId = skill.getId();
        long now = System.currentTimeMillis();

        activationTimes.putIfAbsent(pid, new HashMap<>());
        cooldownTimes.putIfAbsent(pid, new HashMap<>());
        nextResourceTick.putIfAbsent(pid, new HashMap<>());

        // 1. Vérif Cooldown
        long cdEnd = cooldownTimes.get(pid).getOrDefault(skillId, 0L);
        if (now < cdEnd) return;

        // 2. Gestion Activation Initiale
        long start = activationTimes.get(pid).getOrDefault(skillId, 0L);

        if (start == 0 || (now > cdEnd && start < cdEnd)) {
            // Tentative d'activation : on paye le premier coût
            if (consumeResource(sourceRef, store, skill)) {
                activationTimes.get(pid).put(skillId, now);
                // Prochaine consommation dans 1 seconde
                nextResourceTick.get(pid).put(skillId, now + 1000L);
                EffectManager.sendBuffFeedback(player, "§a⚡ " + skill.getName() + " activée !");
                start = now;
            } else {
                return; // Pas assez de mana pour démarrer
            }
        }

        // 3. Vérif Durée Totale
        long durationMs = (long) (skill.getActivationTime() * 1000);
        if (now > start + durationMs) {
            stopSkill(player, pid, skillId, now, skill.getCooldown());
            return;
        }

        // 4. Consommation périodique (CORRECTIF ICI)
        // On ne consomme que si le temps est écoulé, indépendamment du nombre d'ennemis
        long nextConsum = nextResourceTick.get(pid).getOrDefault(skillId, 0L);

        if (now >= nextConsum) {
            if (!consumeResource(sourceRef, store, skill)) {
                stopSkill(player, pid, skillId, now, skill.getCooldown());
                return;
            }
            // On repousse la prochaine consommation de 1s
            nextResourceTick.get(pid).put(skillId, now + 1000L);
        }

        // --- APPLICATION DE L'EFFET (Indépendant de la consommation) ---
        TransformComponent sourceTrans = store.getComponent(sourceRef, TransformComponent.getComponentType());
        if (sourceTrans == null) return;

        // Effet visuel sur le lanceur (ne le jouer que si on est proche du prochain tick de ressource pour éviter le spam ?)
        // Ou on le laisse à chaque tick (0.2s) pour que ce soit fluide.
        skill.onTick(sourceRef, store, commandBuffer, pData);

        // Effets sur les cibles (Dégâts)
        double dist = EffectManager.getDistance(targetPos, sourceTrans.getPosition());
        if (dist <= skill.getRadius()) {
            skill.onAuraTick(sourceRef, targetRef, store, commandBuffer, dist);
        }
    }

    private boolean consumeResource(Ref<EntityStore> source, Store<EntityStore> store, SkillModel skill) {
        EntityStatMap statMap = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;

        int resourceIndex = skill.getResourceType().equals("Mana") || skill.getResourceType().equals("Dragon")
                ? DefaultEntityStatTypes.getMana()
                : DefaultEntityStatTypes.getStamina();

        EntityStatValue resource = statMap.get(resourceIndex);

        // CORRECTION : Si le coût est de 0 (ex: passif gratuit), on return true direct
        if (skill.getResourceCostPerSecond() <= 0) return true;

        if (resource == null || resource.get() < skill.getResourceCostPerSecond()) return false;

        statMap.setStatValue(resourceIndex, resource.get() - skill.getResourceCostPerSecond());
        return true;
    }

    private void stopSkill(PlayerRef player, UUID pid, String skillId, long now, float cooldown) {
        activationTimes.get(pid).remove(skillId);
        nextResourceTick.get(pid).remove(skillId); // Nettoyage
        cooldownTimes.get(pid).put(skillId, now + (long)(cooldown * 1000));
        EffectManager.sendCombatFeedback(player, "§7 Fin de compétence...", false);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TransformComponent.getComponentType(), EntityStatMap.getComponentType());
    }
}