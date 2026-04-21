package com.eldanior.system.titles.nobility.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DignityAuraSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;

    // Joueurs actuellement ralentis (UUID -> slow actuel applique)
    private static final Map<UUID, Float> slowedPlayers = new ConcurrentHashMap<>();

    // Cooldown de paralysie (emetteur:cible -> timestamp)
    private final Map<String, Long> paralysisCD = new HashMap<>();
    private static final long PARALYSIS_COOLDOWN_MS = 30000;

    private static int getAuraRadius(int dignity) {
        if (dignity >= 100) return 25;
        if (dignity >= 75) return 20;
        if (dignity >= 50) return 15;
        if (dignity >= 30) return 10;
        if (dignity >= 15) return 7;
        if (dignity >= 5) return 4;
        return 0;
    }

    private static float getMaxSlowPercent(int dignity) {
        return Math.min(0.90f, dignity * 0.009f);
    }

    private static float getMaxParalysisDuration(int dignity) {
        if (dignity < 50) return 0f;
        return Math.min(10.0f, (dignity - 50) * 0.18f + 1.0f);
    }

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        updateTimer += dt;
        if (updateTimer < 1.0f) return;
        if (index == 0) updateTimer = 0;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        UUID selfUUID = getPlayerUUID(player);
        if (selfUUID == null) return;

        // === VERIFIER SI CE JOUEUR EST AFFECTE PAR UNE AURA ===
        // On recalcule le slow max que ce joueur subit de toutes les auras actives
        PlayerLevelData selfData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (selfData == null) return;
        int selfDignity = selfData.getDignity();

        // === EMETTEUR : TRACKER ARME EN MAIN ===
        // Mettre a jour le tracker pour que DignityAuraMobSystem sache si l'aura est active
        if (selfDignity >= 5) {
            Inventory inv = player.getInventory();
            ItemStack heldItem = (inv != null) ? inv.getItemInHand() : null;

            if (heldItem != null && !heldItem.isEmpty() && isWeapon(heldItem.getItemId().toLowerCase())) {
                setEmitterActive(selfUUID);
            } else {
                setEmitterInactive(selfUUID);
            }
        } else {
            setEmitterInactive(selfUUID);
        }

        float maxSlowReceived = 0f;

        // Verifier toutes les auras des autres joueurs
        for (Map.Entry<UUID, Vector3d> emitterEntry : PlayerPositionTracker.PLAYER_POSITIONS.entrySet()) {
            UUID emitterUUID = emitterEntry.getKey();
            if (emitterUUID.equals(selfUUID)) continue;

            Integer emitterDignity = PlayerPositionTracker.PLAYER_DIGNITY.get(emitterUUID);
            if (emitterDignity == null || emitterDignity < 5) continue;
            if (emitterDignity <= selfDignity) continue;

            // Verifier que l'emetteur a une arme en main
            if (!isEmitterActive(emitterUUID)) continue;

            TransformComponent selfTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
            if (selfTransform == null) continue;

            Vector3d selfPos = selfTransform.getPosition();
            Vector3d emitterPos = emitterEntry.getValue();

            int auraRadius = getAuraRadius(emitterDignity);
            double distSq = distanceSquared(selfPos, emitterPos);
            double auraRadiusSq = (double) auraRadius * auraRadius;

            if (distSq > auraRadiusSq) continue;

            // Check du level : si la cible est +50 levels au-dessus de l'emetteur, pas d'effet
            Integer emitterLevel = PlayerPositionTracker.PLAYER_LEVELS.get(emitterUUID);
            int selfLevel = selfData.getLevel();
            if (emitterLevel == null) emitterLevel = 1;

            int levelGap = selfLevel - emitterLevel;
            float levelRatio;
            if (levelGap <= 0) {
                levelRatio = 1.0f;
            } else if (levelGap <= 50) {
                levelRatio = 1.0f - (levelGap / 50.0f);
            } else {
                continue; // Joueur cible trop haut level
            }

            float emitterMaxSlow = getMaxSlowPercent(emitterDignity);
            int dignityGap = emitterDignity - selfDignity;
            float effectRatio = Math.min(1.0f, dignityGap / (float) Math.max(1, emitterDignity));

            double dist = Math.sqrt(distSq);
            float distanceRatio = Math.max(0f, 1.0f - (float) (dist / auraRadius));

            float slowFromThisEmitter = emitterMaxSlow * effectRatio * distanceRatio * levelRatio;
            maxSlowReceived = Math.max(maxSlowReceived, slowFromThisEmitter);
        }

        // === APPLIQUER OU RESTAURER LA VITESSE ===
        Float currentSlow = slowedPlayers.get(selfUUID);
        if (currentSlow == null) currentSlow = 0f;

        // Seulement si le slow a change de plus de 1%
        if (Math.abs(maxSlowReceived - currentSlow) > 0.01f) {
            applySpeedModifier(playerRef, store, selfData, maxSlowReceived);
            if (maxSlowReceived > 0.01f) {
                slowedPlayers.put(selfUUID, maxSlowReceived);
            } else {
                slowedPlayers.remove(selfUUID);
            }
        }

        // === PARALYSIE (pas de particules pour le moment) ===
        if (maxSlowReceived >= 0.60f) {
            UUID strongestEmitter = null;
            int strongestDignity = 0;
            for (Map.Entry<UUID, Integer> dEntry : PlayerPositionTracker.PLAYER_DIGNITY.entrySet()) {
                if (!dEntry.getKey().equals(selfUUID) && dEntry.getValue() > strongestDignity && dEntry.getValue() > selfDignity) {
                    strongestDignity = dEntry.getValue();
                    strongestEmitter = dEntry.getKey();
                }
            }

            if (strongestEmitter != null && isEmitterActive(strongestEmitter)) {
                String cdKey = strongestEmitter + ":" + selfUUID;
                long now = System.currentTimeMillis();
                Long lastP = paralysisCD.get(cdKey);
                if (lastP == null || (now - lastP) > PARALYSIS_COOLDOWN_MS) {
                    paralysisCD.put(cdKey, now);
                    applySpeedModifier(playerRef, store, selfData, 0.99f);
                }
            }
        }
    }

    private void applySpeedModifier(Ref<EntityStore> playerRef, Store<EntityStore> store,
                                     PlayerLevelData data, float slowPercent) {
        MovementManager manager = store.getComponent(playerRef, MovementManager.getComponentType());
        if (manager == null) return;
        MovementSettings settings = manager.getSettings();
        if (settings == null) return;

        // Calculer la vitesse de base du joueur (sans aura)
        ClassModel model = ClassManager.get(data.getPlayerClassId());
        float baseSpeed = StatConfig.AGILITY_SPEED.getFinalValue(data, model);

        // Appliquer le ralentissement
        float slowedSpeed = baseSpeed * (1.0f - slowPercent);
        slowedSpeed = Math.max(0.1f, slowedSpeed); // Minimum 10% de vitesse

        settings.forwardSprintSpeedMultiplier = slowedSpeed;
        settings.strafeRunSpeedMultiplier = slowedSpeed;

        store.putComponent(playerRef, MovementManager.getComponentType(), manager);
        syncMovement(store, playerRef, manager);
    }

    private void syncMovement(Store<EntityStore> store, Ref<EntityStore> entityRef, MovementManager manager) {
        try {
            UUID uuid = getUUIDFromRef(store, entityRef);
            if (uuid == null) return;

            PlayerRef playerRef = Universe.get().getPlayer(uuid);
            if (playerRef == null) return;

            Method getPacketHandlerMethod = playerRef.getClass().getMethod("getPacketHandler");
            Object packetHandler = getPacketHandlerMethod.invoke(playerRef);
            for (Method updateMethod : manager.getClass().getMethods()) {
                if (updateMethod.getName().equals("update") && updateMethod.getParameterCount() == 1) {
                    updateMethod.invoke(manager, packetHandler);
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private UUID getUUIDFromRef(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        try {
            var uuidComp = store.getComponent(entityRef, com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType());
            if (uuidComp != null) return uuidComp.getUuid();
        } catch (Exception ignored) {}
        return null;
    }

    private boolean isWeapon(String itemId) {
        return itemId.contains("sword") || itemId.contains("axe") || itemId.contains("spear")
                || itemId.contains("bow") || itemId.contains("dagger") || itemId.contains("mace")
                || itemId.contains("staff") || itemId.contains("spellbook") || itemId.contains("club")
                || itemId.contains("gun") || itemId.contains("rifle") || itemId.contains("shield");
    }

    private double distanceSquared(Vector3d a, Vector3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private UUID getPlayerUUID(Player player) {
        try {
            for (Method m : player.getClass().getMethods()) {
                if (m.getReturnType().equals(UUID.class) && m.getParameterCount() == 0) {
                    return (UUID) m.invoke(player);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Tracker statique : les emetteurs actifs (ceux qui ont une arme en main)
    // Mis a jour par un second passage
    private static final Set<UUID> activeEmitters = ConcurrentHashMap.newKeySet();

    public static boolean isEmitterActive(UUID uuid) { return activeEmitters.contains(uuid); }
    public static void setEmitterActive(UUID uuid) { activeEmitters.add(uuid); }
    public static void setEmitterInactive(UUID uuid) { activeEmitters.remove(uuid); }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
