package com.eldanior.system.rpg.classes.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.components.PlayerLevelData;
import com.eldanior.system.rpg.classes.skills.Skills.SkillManager; //
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

public class SkillsSystem extends EntityTickingSystem<EntityStore> {

    private final Map<UUID, Map<String, Long>> activationTimes = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldownTimes = new HashMap<>();
    private final Map<UUID, Map<String, Long>> nextResourceTick = new HashMap<>();
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

        for (PlayerRef player : Universe.get().getPlayers()) {
            var sourceRef = player.getReference();
            if (sourceRef == null || sourceRef.equals(entityRef) || !sourceRef.isValid()) continue;

            PlayerLevelData pData = store.getComponent(sourceRef, EldaniorSystem.get().getPlayerLevelDataType());
            // Plus besoin de vérifier le ClassId ici, on vérifie juste si le joueur a des données
            if (pData == null) continue;

            // --- NOUVELLE LOGIQUE : On itère sur les compétences ACQUISES par le joueur ---
            // On suppose que pData.getUnlockedSkills() renvoie List<String>
            for (String skillId : pData.getUnlockedSkills()) {

                // On récupère la définition technique du skill
                SkillModel skill = SkillManager.get(skillId);

                if (skill == null || skill.getRadius() <= 0) continue;

                // --- LOGIQUE DE TÉLÉCOMMANDE ---
                // On ne lance handleSkillLogic QUE si le skill est activé dans le PlayerLevelData (Toggle ON)
                if (pData.isSkillEnabled(skill.getId())) {
                    handleSkillLogic(player, sourceRef, entityRef, store, commandBuffer, skill, entityPos, pData);
                } else {
                    cleanupSkillTimers(player.getUuid(), skill.getId());
                }
            }
        }
    }

    private void handleSkillLogic(PlayerRef player, Ref<EntityStore> sourceRef, Ref<EntityStore> targetRef,
                                  Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
                                  SkillModel skill, Vector3d targetPos, PlayerLevelData pData) {

        UUID pid = player.getUuid();
        String skillId = skill.getId();
        long now = System.currentTimeMillis();

        activationTimes.putIfAbsent(pid, new HashMap<>());
        cooldownTimes.putIfAbsent(pid, new HashMap<>());
        nextResourceTick.putIfAbsent(pid, new HashMap<>());

        if (now < cooldownTimes.get(pid).getOrDefault(skillId, 0L)) return;

        long start = activationTimes.get(pid).getOrDefault(skillId, 0L);
        if (start == 0) {
            if (consumeResource(sourceRef, store, skill)) {
                activationTimes.get(pid).put(skillId, now);
                nextResourceTick.get(pid).put(skillId, now + 1000L);
                String mode = (skill.getActivationTime() <= 0) ? "§6[Permanent]" : "§e[" + skill.getActivationTime() + "s]";
                EffectManager.sendBuffFeedback(player, "§a⚡ " + skill.getName() + " " + mode + " activée !");
                start = now;
            } else return;
        }

        // Vérification durée (si > 0)
        if (skill.getActivationTime() > 0 && now > start + (long)(skill.getActivationTime() * 1000)) {
            stopSkill(player, pid, skillId, now, skill.getCooldown());
            return;
        }

        // Consommation Mana
        if (now >= nextResourceTick.get(pid).getOrDefault(skillId, 0L)) {
            if (!consumeResource(sourceRef, store, skill)) {
                stopSkill(player, pid, skillId, now, skill.getCooldown());
                return;
            }
            nextResourceTick.get(pid).put(skillId, now + 1000L);
        }

        TransformComponent sourceTrans = store.getComponent(sourceRef, TransformComponent.getComponentType());
        if (sourceTrans == null) return;

        skill.onTick(sourceRef, store, commandBuffer, pData);

        double dist = EffectManager.getDistance(targetPos, sourceTrans.getPosition());
        if (dist <= skill.getRadius()) {
            skill.onAuraTick(sourceRef, targetRef, store, commandBuffer, dist);
        }
    }

    private boolean consumeResource(Ref<EntityStore> source, Store<EntityStore> store, SkillModel skill) {
        EntityStatMap statMap = store.getComponent(source, EntityStatsModule.get().getEntityStatMapComponentType());
        if (statMap == null) return false;
        int resIdx = (skill.getResourceType().equals("Mana") || skill.getResourceType().equals("Dragon")) ? DefaultEntityStatTypes.getMana() : DefaultEntityStatTypes.getStamina();
        EntityStatValue res = statMap.get(resIdx);
        if (skill.getResourceCostPerSecond() <= 0) return true;
        if (res == null || res.get() < skill.getResourceCostPerSecond()) return false;
        statMap.setStatValue(resIdx, res.get() - skill.getResourceCostPerSecond());
        return true;
    }

    private void stopSkill(PlayerRef player, UUID pid, String skillId, long now, float cooldown) {
        cleanupSkillTimers(pid, skillId);
        cooldownTimes.get(pid).put(skillId, now + (long)(cooldown * 1000));
        EffectManager.sendCombatFeedback(player, "§7 Fin de compétence : " + skillId, false);

        var ref = player.getReference();
        if (ref != null) {
            PlayerLevelData data = ref.getStore().getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data != null && data.isSkillEnabled(skillId)) {
                data.toggleSkill(skillId); // OFF
                ref.getStore().putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);
            }
        }
    }

    private void cleanupSkillTimers(UUID pid, String skillId) {
        if (activationTimes.containsKey(pid)) activationTimes.get(pid).remove(skillId);
        if (nextResourceTick.containsKey(pid)) nextResourceTick.get(pid).remove(skillId);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(TransformComponent.getComponentType(), EntityStatMap.getComponentType());
    }
}