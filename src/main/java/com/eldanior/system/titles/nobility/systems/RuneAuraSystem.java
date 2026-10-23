package com.eldanior.system.titles.nobility.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aura de Dignite portee par la Rune d'Aura (toggle canalise).
 *
 * - Activation / desactivation : appui sur la rune (marqueur transitoire Ability_Aura_Toggle).
 * - Tant qu'elle est active : marqueur infini Ability_Aura_Buff (visuel + indicateur combat),
 *   consommation de 5 mana/seconde, coupure automatique a court de mana.
 * - Degats infliges +% / degats recus -% (scales dignite) : appliques par CombatStatsSystem.
 * - Paliers normaux (dignite < 1000) : ralentissement des joueurs proches de dignite plus faible.
 * - Palier DRAGON (dignite >= 1000) : zone de domination draconique sur joueurs ET monstres
 *   (voir {@link #applyDragonZone}) — degats de chaleur croissants a l'approche, overlay ecran
 *   sombre, et barriere de repulsion a 7 blocs ; les monstres faibles sont repousses au bord (fuite).
 */
public class RuneAuraSystem extends EntityTickingSystem<EntityStore> {

    public static final String BUFF_ID = "Ability_Aura_Buff";
    private static final String TOGGLE_ID = "Ability_Aura_Toggle";
    /** Visuel supplementaire du palier Dragon (foudre + aura noire menacante) sur le porteur. */
    private static final String DRAGON_VISUAL_ID = "Ability_Aura_Dragon";
    /** Visuels colores de l'aura, un par palier (indices 0..6 = paliers 5/15/30/50/75/100/1000). */
    private static final String[] VISUAL_IDS = {
            "Ability_Aura_Visual_1", "Ability_Aura_Visual_2", "Ability_Aura_Visual_3",
            "Ability_Aura_Visual_4", "Ability_Aura_Visual_5", "Ability_Aura_Visual_6",
            "Ability_Aura_Visual_7"
    };

    private static final String SLOW_LIGHT = "Rune_Aura_Slow_Light";
    private static final String SLOW_MEDIUM = "Rune_Aura_Slow_Medium";
    private static final String SLOW_HEAVY = "Rune_Aura_Slow_Heavy";

    private static final String HEAT_LOW = "Rune_Aura_Dragon_Heat_Low";
    private static final String HEAT_MID = "Rune_Aura_Dragon_Heat_Mid";
    private static final String HEAT_HIGH = "Rune_Aura_Dragon_Heat_High";

    /** Dignite a partir de laquelle le palier Dragon s'active (classe Dragon = plancher 1000). */
    public static final int DRAGON_DIGNITY = 1000;
    /** Rayon de la zone draconique (chaleur / visuel). */
    public static final double DRAGON_ZONE_RADIUS = 30.0;
    /** Distance de la barriere de repulsion : impossible d'avancer plus pres. */
    public static final double BARRIER_DIST = 7.0;

    private static final float MANA_PER_SECOND = 5f;
    private static final int MIN_DIGNITY = 5;

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    /** Joueurs dont l'aura (rune) est actuellement active (= emetteurs). */
    private static final java.util.Set<UUID> activeAura = ConcurrentHashMap.newKeySet();
    /** Dernier prelevement de mana (ms) par joueur. */
    private static final java.util.Map<UUID, Long> lastDrainMs = new ConcurrentHashMap<>();
    /** Dernier palier d'aura connu par joueur (-1 = aucun), pour l'alerte de changement de palier. */
    private static final java.util.Map<UUID, Integer> lastTier = new ConcurrentHashMap<>();

    // ===== Scaling des degats/reduction (identique a l'ancien passif) =====

    public static float getDamageBonus(int dignity) {
        if (dignity >= 100) return 0.25f;
        if (dignity >= 75) return 0.18f;
        if (dignity >= 50) return 0.12f;
        if (dignity >= 30) return 0.08f;
        if (dignity >= 15) return 0.05f;
        if (dignity >= 5) return 0.02f;
        return 0f;
    }

    public static float getDamageReduction(int dignity) {
        if (dignity >= 100) return 0.15f;
        if (dignity >= 75) return 0.12f;
        if (dignity >= 50) return 0.08f;
        if (dignity >= 30) return 0.05f;
        if (dignity >= 15) return 0.03f;
        if (dignity >= 5) return 0.01f;
        return 0f;
    }

    private static int getAuraRadius(int dignity) {
        if (dignity >= 1000) return 30;
        if (dignity >= 100) return 25;
        if (dignity >= 75) return 20;
        if (dignity >= 50) return 15;
        if (dignity >= 30) return 10;
        if (dignity >= 15) return 7;
        if (dignity >= 5) return 4;
        return 0;
    }

    private static float getMaxSlowPercent(int dignity) {
        if (dignity >= 100) return 0.90f;
        if (dignity >= 75) return 0.60f;
        if (dignity >= 50) return 0.45f;
        if (dignity >= 30) return 0.27f;
        if (dignity >= 15) return 0.135f;
        if (dignity >= 5) return 0.045f;
        return 0f;
    }

    /** Visuel colore correspondant au palier de dignite. */
    private static String getVisualId(int dignity) {
        if (dignity >= 1000) return VISUAL_IDS[6];
        if (dignity >= 100) return VISUAL_IDS[5];
        if (dignity >= 75) return VISUAL_IDS[4];
        if (dignity >= 50) return VISUAL_IDS[3];
        if (dignity >= 30) return VISUAL_IDS[2];
        if (dignity >= 15) return VISUAL_IDS[1];
        return VISUAL_IDS[0]; // >= 5
    }

    /** Bonus physique (%) de l'aura active : endurance, force, vitesse, saut, agilite (pas la vie, voir StatConfig). */
    public static float getPhysicalBonus(int dignity) {
        if (dignity >= 1000) return 1.00f; // Dragon
        if (dignity >= 100) return 0.75f;  // Roi
        if (dignity >= 75) return 0.50f;   // Marquis
        if (dignity >= 50) return 0.35f;   // Duc
        if (dignity >= 30) return 0.20f;   // Comte
        if (dignity >= 15) return 0.10f;   // Baron
        if (dignity >= MIN_DIGNITY) return 0.05f; // Chevalier
        return 0f;
    }

    /** Index du palier d'aura (0..6 = paliers 5/15/30/50/75/100/1000), -1 sous la dignite minimale. */
    private static int getTierIndex(int dignity) {
        if (dignity >= 1000) return 6;
        if (dignity >= 100) return 5;
        if (dignity >= 75) return 4;
        if (dignity >= 50) return 3;
        if (dignity >= 30) return 2;
        if (dignity >= 15) return 1;
        if (dignity >= MIN_DIGNITY) return 0;
        return -1;
    }

    /** Nom de l'aura debloquee selon la dignite (affiche a la place de "Aura de Dignite"). */
    public static String getAuraName(int dignity) {
        if (dignity >= 1000) return "Aura Draconique";
        if (dignity >= 100) return "Aura Absolue";
        if (dignity >= 75) return "Aura Royale";
        if (dignity >= 50) return "Aura Imposante";
        if (dignity >= 30) return "Aura Noble";
        if (dignity >= 15) return "Aura Mineure";
        if (dignity >= 5) return "Aura";
        return "Aura";
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 0.5f) {
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef selfPlayerRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (selfPlayerRef == null) return;
        UUID selfUUID;
        try { selfUUID = UUIDExtractor.getUUID(selfPlayerRef); }
        catch (Exception e) { return; }
        if (selfUUID == null) return;

        PlayerLevelData selfData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (selfData == null) return;
        int selfDignity = selfData.getDignity();

        // === 1. Detection de l'appui (toggle) ===
        if (EffectsManager.hasEffect(playerRef, TOGGLE_ID, store)) {
            EffectsManager.removeEffect(playerRef, TOGGLE_ID, store); // consommer l'appui

            if (activeAura.contains(selfUUID)) {
                deactivate(selfUUID, playerRef, store);
                NotificationHelper.sendWarning(selfPlayerRef,
                        "<color:gray>" + getAuraName(selfDignity) + " dissipee.</color>");
            } else if (selfDignity < MIN_DIGNITY) {
                NotificationHelper.sendWarning(selfPlayerRef,
                        "<color:red>Dignite insuffisante pour invoquer l'aura (" + MIN_DIGNITY + " min).</color>");
            } else if (readMana(playerRef, store) < MANA_PER_SECOND) {
                NotificationHelper.sendWarning(selfPlayerRef, "<color:red>Pas assez de mana pour activer l'aura.</color>");
            } else {
                activeAura.add(selfUUID);
                lastDrainMs.put(selfUUID, System.currentTimeMillis());
                EffectsManager.applyInfiniteEffect(playerRef, BUFF_ID, store);
                NotificationHelper.sendSuccess(selfPlayerRef,
                        "<color:gold>" + getAuraName(selfDignity) + " activee !</color>");
            }
        }

        // === 1b. Alerte de changement de palier (premier passage = reference silencieuse) ===
        int tier = getTierIndex(selfDignity);
        Integer previousTier = lastTier.put(selfUUID, tier);
        if (previousTier != null && previousTier != tier) {
            if (tier > previousTier) {
                NotificationHelper.showEventTitle(selfPlayerRef, "NOUVELLE AURA", getAuraName(selfDignity), tier == 6);
            } else if (tier >= 0) {
                NotificationHelper.sendWarning(selfPlayerRef,
                        "<color:gray>Votre aura faiblit : " + getAuraName(selfDignity) + ".</color>");
            } else {
                NotificationHelper.sendWarning(selfPlayerRef,
                        "<color:gray>Dignite insuffisante : vous ne pouvez plus invoquer d'aura.</color>");
            }
        }

        // === 1c. Bonus physique : recalcul des stats quand l'aura s'allume/s'eteint ou change de palier ===
        float physicalBonus = activeAura.contains(selfUUID) ? getPhysicalBonus(selfDignity) : 0f;
        if (Math.abs(selfData.getAuraPhysicalBonus() - physicalBonus) > 0.0001f) {
            selfData.setAuraPhysicalBonus(physicalBonus);
            final PlayerLevelData statData = selfData;
            commandBuffer.run(deferredStore -> StatCalculator.updatePlayerStats(playerRef, deferredStore, statData));
        }

        // === 2. Entretien de l'aura active : buff + visuel dragon + drain de mana ===
        if (activeAura.contains(selfUUID)) {
            if (!EffectsManager.hasEffect(playerRef, BUFF_ID, store)) {
                EffectsManager.applyInfiniteEffect(playerRef, BUFF_ID, store);
            }

            // Visuel colore du palier courant (retire les autres si la dignite change de palier)
            String visual = getVisualId(selfDignity);
            if (!EffectsManager.hasEffect(playerRef, visual, store)) {
                for (String v : VISUAL_IDS) {
                    if (!v.equals(visual)) EffectsManager.removeEffect(playerRef, v, store);
                }
                EffectsManager.applyInfiniteEffect(playerRef, visual, store);
            }

            // Visuel supplementaire du palier Dragon (foudre + aura noire)
            boolean dragonTier = selfDignity >= DRAGON_DIGNITY;
            boolean hasDragonVisual = EffectsManager.hasEffect(playerRef, DRAGON_VISUAL_ID, store);
            if (dragonTier && !hasDragonVisual) {
                EffectsManager.applyInfiniteEffect(playerRef, DRAGON_VISUAL_ID, store);
            } else if (!dragonTier && hasDragonVisual) {
                EffectsManager.removeEffect(playerRef, DRAGON_VISUAL_ID, store);
            }

            long now = System.currentTimeMillis();
            long last = lastDrainMs.getOrDefault(selfUUID, now);
            if (now - last >= 1000) {
                ComponentType<EntityStore, EntityStatMap> statMapType = EntityStatsModule.get().getEntityStatMapComponentType();
                EntityStatMap statMap = store.getComponent(playerRef, statMapType);
                float mana = statMap != null ? statMap.get(DefaultEntityStatTypes.getMana()).get() : 0f;

                if (statMap == null || mana < MANA_PER_SECOND) {
                    deactivate(selfUUID, playerRef, store);
                    NotificationHelper.sendDanger(selfPlayerRef, "<color:red>Mana epuise : aura dissipee.</color>");
                } else {
                    statMap.setStatValue(DefaultEntityStatTypes.getMana(), mana - MANA_PER_SECOND);
                    commandBuffer.putComponent(playerRef, statMapType, statMap);
                    lastDrainMs.put(selfUUID, now);
                }
            }
        }

        // === 3. Effets subis par CE joueur (emetteurs = auras actives) ===
        if (activeAura.isEmpty()) return;

        TransformComponent selfTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (selfTransform == null) return;
        Vector3d selfPos = selfTransform.getPosition();
        int selfLevel = selfData.getLevel();

        // 3a. Zone draconique prioritaire (chaleur + barriere + overlay). Les joueurs ne fuient pas.
        Vector3d dragonEmitter = dragonEmitterPosAffecting(selfPos, selfUUID);
        if (dragonEmitter != null) {
            applyDragonZone(playerRef, selfPos, dragonEmitter, store);
            return;
        }

        // 3b. Sinon : ralentissement classique depuis les emetteurs de dignite superieure.
        float maxSlowReceived = 0f;
        for (UUID emitterUUID : activeAura) {
            if (emitterUUID.equals(selfUUID)) continue;

            Integer emitterDignity = PlayerPositionTracker.PLAYER_DIGNITY.get(emitterUUID);
            if (emitterDignity == null || emitterDignity < MIN_DIGNITY) continue;
            if (emitterDignity <= selfDignity) continue;

            Vector3d emitterPos = PlayerPositionTracker.PLAYER_POSITIONS.get(emitterUUID);
            if (emitterPos == null) continue;

            int auraRadius = getAuraRadius(emitterDignity);
            if (auraRadius <= 0) continue;

            double distSq = distanceSquared(selfPos, emitterPos);
            double auraRadiusSq = (double) auraRadius * auraRadius;
            if (distSq > auraRadiusSq) continue;

            Integer emitterLevel = PlayerPositionTracker.PLAYER_LEVELS.get(emitterUUID);
            if (emitterLevel == null) emitterLevel = 1;

            int levelGap = selfLevel - emitterLevel;
            float levelRatio;
            if (levelGap <= 0) {
                levelRatio = 1.0f;
            } else if (levelGap <= 50) {
                levelRatio = 1.0f - (levelGap / 50.0f);
            } else {
                continue;
            }

            float emitterMaxSlow = getMaxSlowPercent(emitterDignity);
            int dignityGap = emitterDignity - selfDignity;
            float effectRatio = Math.min(1.0f, dignityGap / (float) Math.max(1, emitterDignity));

            double dist = Math.sqrt(distSq);
            float distanceRatio = Math.max(0f, 1.0f - (float) (dist / auraRadius));

            float slowFromThisEmitter = emitterMaxSlow * effectRatio * distanceRatio * levelRatio;
            maxSlowReceived = Math.max(maxSlowReceived, slowFromThisEmitter);
        }

        String slowId = tierFor(maxSlowReceived);
        if (slowId != null) {
            EffectsManager.applyCustomEffect(playerRef, slowId, 1.2f, OverlapBehavior.OVERWRITE, store);
        }
    }

    // ===== Zone draconique (partagee avec DragonicFearSystem pour les monstres) =====

    /**
     * Applique la domination draconique a une entite (joueur ou monstre) situee a {@code victimPos},
     * dominee par un porteur d'aura Dragon a {@code emitterPos} :
     *  - degats de chaleur croissants a l'approche (effets Rune_Aura_Dragon_Heat_*),
     *  - barriere de repulsion : impossible d'avancer en-deca de {@link #BARRIER_DIST} blocs,
     *  - {@code fleeToEdge} : si vrai (monstres faibles), repousse jusqu'au bord de la zone (fuite).
     */
    public static void applyDragonZone(Ref<EntityStore> ref, Vector3d victimPos,
                                       Vector3d emitterPos, Store<EntityStore> store) {
        double dist3d = Math.sqrt(distanceSquared(victimPos, emitterPos));
        String heat = dist3d <= 10 ? HEAT_HIGH : (dist3d <= 15 ? HEAT_MID : HEAT_LOW);
        EffectsManager.applyCustomEffect(ref, heat, 1.2f, OverlapBehavior.OVERWRITE, store);

        // Pres du coeur de l'aura : ralentissement ecrasant (chaleur / gravite oppressante).
        // Pas de teleportation : la chaleur intense + ce ralentissement servent de dissuasion realiste.
        double dx = victimPos.x - emitterPos.x;
        double dz = victimPos.z - emitterPos.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < BARRIER_DIST) {
            EffectsManager.applyCustomEffect(ref, SLOW_HEAVY, 1.2f, OverlapBehavior.OVERWRITE, store);
        }
    }

    /**
     * Position du porteur d'aura Dragon (dignite >= 1000) le plus proche de {@code pos} et a portee,
     * ou null si aucun. {@code excludeUUID} exclut l'entite elle-meme (le porteur ne se brule pas).
     */
    public static Vector3d dragonEmitterPosAffecting(Vector3d pos, UUID excludeUUID) {
        if (pos == null || activeAura.isEmpty()) return null;
        Vector3d best = null;
        double bestDistSq = Double.MAX_VALUE;
        double rSq = DRAGON_ZONE_RADIUS * DRAGON_ZONE_RADIUS;
        for (UUID emitterUUID : activeAura) {
            if (excludeUUID != null && emitterUUID.equals(excludeUUID)) continue;
            Integer dig = PlayerPositionTracker.PLAYER_DIGNITY.get(emitterUUID);
            if (dig == null || dig < DRAGON_DIGNITY) continue;
            Vector3d ep = PlayerPositionTracker.PLAYER_POSITIONS.get(emitterUUID);
            if (ep == null) continue;
            double dsq = distanceSquared(pos, ep);
            if (dsq <= rSq && dsq < bestDistSq) {
                bestDistSq = dsq;
                best = ep;
            }
        }
        return best;
    }

    private void deactivate(UUID uuid, Ref<EntityStore> playerRef, Store<EntityStore> store) {
        activeAura.remove(uuid);
        lastDrainMs.remove(uuid);
        EffectsManager.removeEffect(playerRef, BUFF_ID, store);
        EffectsManager.removeEffect(playerRef, DRAGON_VISUAL_ID, store);
        for (String v : VISUAL_IDS) EffectsManager.removeEffect(playerRef, v, store);
    }

    private float readMana(Ref<EntityStore> playerRef, Store<EntityStore> store) {
        ComponentType<EntityStore, EntityStatMap> statMapType = EntityStatsModule.get().getEntityStatMapComponentType();
        EntityStatMap statMap = store.getComponent(playerRef, statMapType);
        return statMap != null ? statMap.get(DefaultEntityStatTypes.getMana()).get() : 0f;
    }

    private static String tierFor(float slow) {
        if (slow >= 0.55f) return SLOW_HEAVY;
        if (slow >= 0.30f) return SLOW_MEDIUM;
        if (slow >= 0.10f) return SLOW_LIGHT;
        return null;
    }

    private static double distanceSquared(Vector3d a, Vector3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Nettoie l'etat d'un joueur qui se deconnecte. */
    public static void handleDisconnect(UUID uuid) {
        if (uuid == null) return;
        activeAura.remove(uuid);
        lastDrainMs.remove(uuid);
        lastTier.remove(uuid);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
