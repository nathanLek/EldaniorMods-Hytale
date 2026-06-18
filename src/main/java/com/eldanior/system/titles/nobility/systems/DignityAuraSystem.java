package com.eldanior.system.titles.nobility.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import org.joml.Vector3d;
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

    // Aura levels: DIGNITY_AURA_1 to DIGNITY_AURA_5
    private static final String[] AURA_IDS = {
            "DIGNITY_AURA_1", "DIGNITY_AURA_2", "DIGNITY_AURA_3", "DIGNITY_AURA_4", "DIGNITY_AURA_5"
    };
    private static final int[] AURA_THRESHOLDS = { 5, 20, 50, 100, 1000 };
    private static final String[] AURA_NAMES = {
            "AURA MINEURE", "AURA DE DIGNITE", "AURA IMPOSANTE", "AURA ABSOLUE", "AURA DRACONIQUE"
    };
    private static final String[] AURA_DESCS = {
            "Une faible aura emane de vous...",
            "Votre aura s'intensifie !",
            "Votre presence impose le respect !",
            "Une aura ecrasante vous entoure !",
            "La puissance d'un dragon coule en vous !"
    };

    private static int getAuraLevel(int dignity) {
        for (int i = AURA_THRESHOLDS.length - 1; i >= 0; i--) {
            if (dignity >= AURA_THRESHOLDS[i]) return i + 1;
        }
        return 0;
    }

    private static String getAuraId(int level) {
        return level > 0 && level <= AURA_IDS.length ? AURA_IDS[level - 1] : null;
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

        // === MIGRATION : Nettoyer l'ancien DIGNITY_AURA ===
        if (selfData.getUnlockedSkills().contains("DIGNITY_AURA")) {
            selfData.removeSkill("DIGNITY_AURA");
        }

        // === Classe Dragon : dignité minimum 1000 ===
        if ("dragon".equalsIgnoreCase(selfData.getPlayerClassId()) && selfDignity < 1000) {
            selfData.setDignity(1000);
            selfDignity = 1000;
        }

        // === AUTO-UNLOCK : Aura de Dignite (5 niveaux) ===
        int newAuraLevel = getAuraLevel(selfDignity);
        String newAuraId = getAuraId(newAuraLevel);

        // Trouver l'aura actuelle du joueur
        String currentAuraId = null;
        int currentAuraLevel = 0;
        for (int i = AURA_IDS.length - 1; i >= 0; i--) {
            if (selfData.getUnlockedSkills().contains(AURA_IDS[i]) || selfData.isSkillEnabled(AURA_IDS[i])) {
                currentAuraId = AURA_IDS[i];
                currentAuraLevel = i + 1;
                break;
            }
        }

        boolean hasAnyAura = currentAuraId != null;
        boolean auraEnabled = currentAuraId != null && selfData.isSkillEnabled(currentAuraId);

        if (newAuraLevel > 0 && newAuraLevel != currentAuraLevel) {
            // Retirer l'ancienne aura si elle existe
            if (currentAuraId != null) {
                selfData.removeSkill(currentAuraId);
            }
            // Ajouter la nouvelle aura
            selfData.learnSkill(newAuraId);
            // Activer par défaut sauf si l'ancienne était désactivée manuellement
            boolean wasDisabled = currentAuraId != null && selfData.getDisabledSkills().contains(currentAuraId);
            if (!wasDisabled) {
                selfData.enableSkill(newAuraId);
            }
            // Nettoyer l'ancien disabled si on change de niveau
            if (currentAuraId != null) {
                selfData.getDisabledSkills().remove(currentAuraId);
            }
            try {
                PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                if (pRef != null) {
                    com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRef,
                            AURA_NAMES[newAuraLevel - 1], AURA_DESCS[newAuraLevel - 1], true);
                }
            } catch (Exception ignored) {}
            auraEnabled = selfData.isSkillEnabled(newAuraId);
        } else if (newAuraLevel == 0 && hasAnyAura) {
            // Dignité trop basse : retirer toute aura
            if (currentAuraId != null) {
                selfData.removeSkill(currentAuraId);
            }
            try {
                PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
                if (pRef != null) {
                    com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pRef,
                            "AURA DISSIPEE", "Votre dignite est trop faible...", false);
                }
            } catch (Exception ignored) {}
            auraEnabled = false;
        } else if (newAuraLevel > 0 && newAuraId != null) {
            // Même niveau, vérifier juste si activée
            auraEnabled = selfData.isSkillEnabled(newAuraId);
        }

        // === EMETTEUR : TRACKER ARME EN MAIN ===
        // L'aura ne fonctionne que si elle est activée
        if (selfDignity >= 5 && auraEnabled) {
            Inventory inv = player.getInventory();
            ItemStack heldItem = (inv != null) ? inv.getActiveHotbarItem() : null;

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

        // === SPEED MODIFIER DESACTIVE — cause des bugs de vol (Jump in location) ===
        // TODO: reimplementer avec un systeme compatible tick (sans putComponent/syncMovement)

        // === PARALYSIE DESACTIVEE (meme raison que speed modifier) ===
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
        } catch (Exception e) { EldaniorLogger.error("DignityAuraSystem", e); }
    }

    private UUID getUUIDFromRef(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        try {
            var uuidComp = store.getComponent(entityRef, com.hypixel.hytale.server.core.entity.UUIDComponent.getComponentType());
            if (uuidComp != null) return uuidComp.getUuid();
        } catch (Exception e) { EldaniorLogger.error("DignityAuraSystem", e); }
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
        } catch (Exception e) { EldaniorLogger.error("DignityAuraSystem", e); }
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
