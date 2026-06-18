package com.eldanior.system.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.server.core.modules.entity.component.DynamicLight;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Détecte les sauts et la distance parcourue pour la progression des skills de mouvement.
 * - Saut : ELDANIOR_SUPPLENESS — 1 proc tous les 2 sauts (0.005% par saut)
 * - Sprint : WIND_STEP, ATHLETICISM — 1 proc tous les 10 blocks parcourus
 */
public class MovementTrackingSystem extends EntityTickingSystem<EntityStore> {

    private static final int JUMPS_PER_PROC = 2;
    private static final double BLOCKS_PER_PROC = 10.0;

    private static final String[] JUMP_SKILLS = { "ELDANIOR_SUPPLENESS" };
    private static final String[] SPRINT_SKILLS = { "WIND_STEP", "ATHLETICISM" };
    private static final String[] NIGHT_SKILLS = { "NIGHT_VISION" };
    private static final String[] STAT_SYNC_SKILLS = { "TIRELESS_BREATH", "AWAKENED_MIND" };
    private static final String[] MANA_REGEN_SKILLS = { "MANA_FONT" };
    private static final String[] HP_REGEN_SKILLS = { "NATURAL_RECOVERY" };
    private static final float MANA_REGEN_PER_PROC = 100f;
    private static final float HP_REGEN_PER_PROC = 50f; // 50 HP régénéré = 1 proc
    private static final long NIGHT_PROC_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<UUID, Double> lastY = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> wasRising = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> jumpCounters = new ConcurrentHashMap<>();
    private final Map<UUID, double[]> lastXZ = new ConcurrentHashMap<>();
    private final Map<UUID, Double> distanceAccum = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastNightProcTime = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastMana = new ConcurrentHashMap<>();
    private final Map<UUID, Float> manaRegenAccum = new ConcurrentHashMap<>();
    private final Map<UUID, Float> lastHP = new ConcurrentHashMap<>();
    private final Map<UUID, Float> hpRegenAccum = new ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        // Chercher quels skills de mouvement sont actifs
        boolean hasJumpSkill = false;
        boolean hasSprintSkill = false;
        boolean hasNightSkill = false;
        boolean hasStatSyncSkill = false;
        boolean hasManaRegenSkill = false;
        boolean hasHPRegenSkill = false;
        for (var passive : data.getActivePassives()) {
            String name = passive.name();
            for (String s : JUMP_SKILLS) { if (name.equals(s)) { hasJumpSkill = true; break; } }
            for (String s : SPRINT_SKILLS) { if (name.equals(s)) { hasSprintSkill = true; break; } }
            for (String s : NIGHT_SKILLS) { if (name.equals(s)) { hasNightSkill = true; break; } }
            for (String s : STAT_SYNC_SKILLS) { if (name.equals(s)) { hasStatSyncSkill = true; break; } }
            for (String s : MANA_REGEN_SKILLS) { if (name.equals(s)) { hasManaRegenSkill = true; break; } }
            for (String s : HP_REGEN_SKILLS) { if (name.equals(s)) { hasHPRegenSkill = true; break; } }
        }
        // Si NightVision désactivé, retirer le DynamicLight s'il existe
        if (!hasNightSkill) {
            DynamicLight existingLight = store.getComponent(ref, DynamicLight.getComponentType());
            if (existingLight != null) {
                commandBuffer.removeComponent(ref, DynamicLight.getComponentType());
            }
        }
        boolean changed = false;

        // Synchro stat → procs (1 point = 10 procs = 0.1%)
        if (hasStatSyncSkill) {
            for (var passive : data.getActivePassives()) {
                String name = passive.name();
                if ("TIRELESS_BREATH".equals(name)) {
                    int target = data.getEndurance() * 10;
                    if (data.getSkillProcs(name) != target) { data.setSkillProcs(name, target); changed = true; }
                } else if ("AWAKENED_MIND".equals(name)) {
                    int target = data.getIntelligence() * 10;
                    if (data.getSkillProcs(name) != target) { data.setSkillProcs(name, target); changed = true; }
                }
            }
        }
        if (!hasJumpSkill && !hasSprintSkill && !hasNightSkill && !hasStatSyncSkill && !hasManaRegenSkill && !hasHPRegenSkill) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;
        UUID uuid = playerRef.getUuid();

        double cx = transform.getPosition().x;
        double cy = transform.getPosition().y;
        double cz = transform.getPosition().z;

        // === SAUT ===
        if (hasJumpSkill) {
            double prevY = lastY.getOrDefault(uuid, cy);
            boolean isRising = cy > prevY + 0.1;
            boolean wasUp = wasRising.getOrDefault(uuid, false);

            if (isRising && !wasUp) {
                int count = jumpCounters.getOrDefault(uuid, 0) + 1;
                if (count >= JUMPS_PER_PROC) {
                    for (var passive : data.getActivePassives()) {
                        for (String s : JUMP_SKILLS) {
                            if (passive.name().equals(s)) { data.addSkillProc(s); changed = true; }
                        }
                    }
                    count = 0;
                }
                jumpCounters.put(uuid, count);
            }
            lastY.put(uuid, cy);
            wasRising.put(uuid, isRising);
        }

        // === SPRINT / DEPLACEMENT ===
        if (hasSprintSkill) {
            double[] prev = lastXZ.getOrDefault(uuid, new double[]{cx, cz});
            double dx = cx - prev[0];
            double dz = cz - prev[1];
            double dist = Math.sqrt(dx * dx + dz * dz);

            // Ignorer les teleportations (>20 blocks en 1 tick)
            if (dist < 20.0 && dist > 0.01) {
                double accum = distanceAccum.getOrDefault(uuid, 0.0) + dist;
                while (accum >= BLOCKS_PER_PROC) {
                    for (var passive : data.getActivePassives()) {
                        for (String s : SPRINT_SKILLS) {
                            if (passive.name().equals(s)) { data.addSkillProc(s); changed = true; }
                        }
                    }
                    accum -= BLOCKS_PER_PROC;
                }
                distanceAccum.put(uuid, accum);
            }
            lastXZ.put(uuid, new double[]{cx, cz});
        }

        // === MANA REGEN (ManaFont) : 100 mana régénéré = 1 proc ===
        if (hasManaRegenSkill) {
            var statMap = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                var manaStat = statMap.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getMana());
                if (manaStat != null) {
                    float currentMana = manaStat.get();
                    float prevMana = lastMana.getOrDefault(uuid, currentMana);
                    if (currentMana > prevMana) {
                        float regenAmount = currentMana - prevMana;
                        float accum = manaRegenAccum.getOrDefault(uuid, 0f) + regenAmount;
                        while (accum >= MANA_REGEN_PER_PROC) {
                            data.addSkillProc("MANA_FONT");
                            accum -= MANA_REGEN_PER_PROC;
                            changed = true;
                        }
                        manaRegenAccum.put(uuid, accum);
                    }
                    lastMana.put(uuid, currentMana);
                }
            }
        }

        // === HP REGEN (NaturalRecovery) : 50 HP régénéré = 1 proc ===
        if (hasHPRegenSkill) {
            var statMap = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule.get().getEntityStatMapComponentType());
            if (statMap != null) {
                var hpStat = statMap.get(com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth());
                if (hpStat != null) {
                    float currentHP = hpStat.get();
                    float prevHP = lastHP.getOrDefault(uuid, currentHP);
                    if (currentHP > prevHP) {
                        float regenAmount = currentHP - prevHP;
                        float accum = hpRegenAccum.getOrDefault(uuid, 0f) + regenAmount;
                        while (accum >= HP_REGEN_PER_PROC) {
                            data.addSkillProc("NATURAL_RECOVERY");
                            accum -= HP_REGEN_PER_PROC;
                            changed = true;
                        }
                        hpRegenAccum.put(uuid, accum);
                    }
                    lastHP.put(uuid, currentHP);
                }
            }
        }

        // === NUIT (NightVision) : 1 proc toutes les 5 min quand il fait nuit ===
        if (hasNightSkill) {
            try {
                var timeResource = store.getResource(WorldTimeResource.getResourceType());
                if (timeResource != null) {
                    int hour = timeResource.getCurrentHour();
                    boolean isNight = hour >= 20 || hour < 6;
                    // Appliquer/retirer DynamicLight fullbright selon nuit/jour
                    DynamicLight currentLight = store.getComponent(ref, DynamicLight.getComponentType());
                    if (isNight) {
                        if (currentLight == null) {
                            // Radius -1 = fullbright (vision nocturne client)
                            commandBuffer.addComponent(ref, DynamicLight.getComponentType(),
                                    new DynamicLight(new ColorLight((byte) -1, (byte) 0, (byte) 0, (byte) 0)));
                        }
                        // Progression toutes les 5 min
                        long now = System.currentTimeMillis();
                        long lastProc = lastNightProcTime.getOrDefault(uuid, 0L);
                        if (now - lastProc >= NIGHT_PROC_INTERVAL_MS) {
                            for (var passive : data.getActivePassives()) {
                                for (String s : NIGHT_SKILLS) {
                                    if (passive.name().equals(s)) { data.addSkillProc(s); changed = true; }
                                }
                            }
                            lastNightProcTime.put(uuid, now);
                        }
                    } else {
                        // Jour : retirer la vision nocturne
                        if (currentLight != null) {
                            commandBuffer.removeComponent(ref, DynamicLight.getComponentType());
                        }
                    }
                }
            } catch (Exception e) { /* skip */ }
        }

        if (changed) {
            commandBuffer.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
